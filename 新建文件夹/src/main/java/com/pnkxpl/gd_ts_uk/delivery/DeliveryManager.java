package com.pnkxpl.gd_ts_uk.delivery;

import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.utils.CommonUtils;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 快递管理器 - 处理快递的创建、移动和到达
 * 优化：简化位置生成逻辑，移除冗余代码
 * 修复：修改拦截消息格式
 * 修正：改进拦截点判断逻辑，确保固体方块是露天的且有足够上方空间
 * 新增：添加数据保存和加载功能，支持快递状态持久化
 * 修正：改进时间管理策略，使用相对时间而不是绝对时间
 * 优化：使用公共工具类，减少代码重复
 * 新增：使用国际化翻译键，支持多语言
 * 修正：重写保存机制，只保存玩家ID和相对消失时间，影响GUI访问
 * 修正：快递时间计算问题 - 保存开始时间（绝对时间）和相对消失时间，但只保存相对消失时间到文件
 * 修正：数据加载验证 - 确保过期的数据不会在加载时重新激活
 * 新增：稀有快递机制和状态保存
 * 修改：移除消失时间保存，由 PendingDisappearanceManager 统一管理所有消失事件
 */
public class DeliveryManager {
    /** 活跃快递数据映射：玩家UUID -> 快递数据 */
    public final Map<UUID, DeliveryData> activeDeliveries = new HashMap<>();

    public DeliveryManager() {}

    /**
     * 快递数据类
     * 修正：保存开始时间（绝对时间）和相对消失时间，但只保存相对消失时间到文件
     * 新增：generationTime字段记录快递生成时间（绝对时间）
     * 新增：relativeDisappearTicks字段记录相对消失时间（游戏刻）
     * 新增：isRareDelivery字段记录是否为稀有快递
     */
    public static class DeliveryData {
        /** 玩家UUID，标识快递所属玩家 */
        public final UUID playerId;
        /** 选择的交易物品 */
        public final ItemStack selectedItem;
        /** 目的地位置，快递的最终目标位置 */
        public final BlockPos destination;
        /** 开始时间（绝对时间，游戏刻），快递生成的时间点 */
        public final long generationTime;
        /** 相对消失时间（游戏刻），从开始时间到消失的时间长度 */
        public final long relativeDisappearTicks;
        /** 是否被拦截，true=被劫匪拦截，false=正常到达 */
        public final boolean isIntercepted;
        /** 是否为稀有快递，true=稀有快递，false=普通快递 */
        public final boolean isRareDelivery;

        /** 当前位置，快递移动过程中的当前位置 */
        public BlockPos currentPosition;
        /** 拦截点位置，如果被拦截，劫匪生成的位置 */
        public BlockPos interceptPoint;
        /** 移动速度等级，1=最慢，2=中等，3=最快 */
        public int moveSpeed = 3;
        /** 最后移动时间（绝对时间，游戏刻），上次移动的时间点 */
        public long lastMoveTime;

        /**
         * 构造函数
         * @param playerId 玩家UUID
         * @param selectedItem 选择的交易物品
         * @param destination 目的地位置
         * @param generationTime 开始时间（绝对时间）
         * @param relativeDisappearTicks 相对消失时间（游戏刻）
         * @param isIntercepted 是否被拦截
         * @param isRareDelivery 是否为稀有快递
         */
        public DeliveryData(UUID playerId, ItemStack selectedItem, BlockPos destination,
                            long generationTime, long relativeDisappearTicks, boolean isIntercepted,
                            boolean isRareDelivery) {
            this.playerId = playerId;
            this.selectedItem = selectedItem;
            this.destination = destination;
            this.generationTime = generationTime;
            this.relativeDisappearTicks = relativeDisappearTicks;
            this.isIntercepted = isIntercepted;
            this.isRareDelivery = isRareDelivery;
            this.lastMoveTime = CommonUtils.getCurrentWorldTime();
        }

        /**
         * 计算实际的消失时间
         * 修正：使用开始时间 + 相对消失时间计算
         * @return 消失时间（绝对时间）
         */
        public long getDisappearTime() {
            return generationTime + relativeDisappearTicks;
        }

        /**
         * 检查是否已过期
         * 修正：使用剩余时间进行过期检查
         * @param currentTime 当前时间（绝对时间）
         * @return 是否过期
         */
        public boolean isExpired(long currentTime) {
            return getRemainingTime(currentTime) <= 0;
        }

        /**
         * 获取剩余时间
         * 修正：使用开始时间和相对消失时间计算
         * @param currentTime 当前时间（绝对时间）
         * @return 剩余时间（游戏刻），0或负数表示已过期
         */
        public long getRemainingTime(long currentTime) {
            return getDisappearTime() - currentTime;
        }

        /**
         * 获取相对消失时间（用于保存）
         * 新增：计算当前时间到消失时间的剩余时间
         * @param currentTime 当前时间（绝对时间）
         * @return 相对消失时间（游戏刻）
         */
        public long getRelativeDisappearTime(long currentTime) {
            return getDisappearTime() - currentTime;
        }
    }

    /**
     * 开始快递
     * @param player 玩家实例
     * @param selectedItem 选择的交易物品
     */
    public void startDelivery(Player player, ItemStack selectedItem) {
        if (player.level().isClientSide()) {
            LoggerUtil.warn("startDelivery在客户端调用!");
            return;
        }

        // 验证条件
        if (!validateDeliveryConditions(player)) {
            return;
        }

        // 创建快递数据
        DeliveryData deliveryData = createDeliveryData(player, selectedItem);
        if (deliveryData == null) {
            // 使用国际化翻译键
            player.displayClientMessage(
                    Component.translatable("message.wandering_trader_express_delivery.no_start_position"),
                    false
            );
            return;
        }

        // 注册快递
        registerDelivery(deliveryData, player);
    }

    /**
     * 验证快递条件
     * @param player 玩家实例
     * @return 是否满足快递条件
     */
    private boolean validateDeliveryConditions(Player player) {
        // 检查主世界
        if (!player.level().dimension().equals(Level.OVERWORLD)) {
            // 使用国际化翻译键
            player.displayClientMessage(
                    Component.translatable("message.wandering_tder_express_delivery.only_overworld"),
                    false
            );
            return false;
        }

        // ✅ 修改：检查自定义消耗物品（非创造模式）
        if (!player.isCreative() && !consumeCustomCostItem(player)) {
            // 不再在这里发送消息，消息已在consumeCustomCostItem中发送
            return false;
        }

        return true;
    }

    /**
     * ✅ 修改：消耗自定义配置的物品
     * 从配置中读取GUI_COST_ITEMS，检查玩家主手或副手是否持有配置中的任意一种物品
     * 如果持有，消耗相应数量；如果不持有，发送详细需求消息
     * @param player 玩家实例
     * @return 是否成功消耗物品
     */
    private boolean consumeCustomCostItem(Player player) {
        // 获取配置中的消耗物品列表
        List<? extends String> costItemsConfig = Config.getGuiCostItems();
        if (costItemsConfig.isEmpty()) {
            LoggerUtil.warn("GUI消耗物品配置为空，使用默认绿宝石");
            return consumeDefaultEmerald(player);
        }

        // 解析配置并检查玩家手持物品
        for (String configEntry : costItemsConfig) {
            try {
                // 解析格式: modid:item_id,数量
                String[] parts = configEntry.split(",");
                if (parts.length != 2) {
                    LoggerUtil.warn("无效的GUI消耗物品配置格式: {}，应为'modid:item_id,数量'", configEntry);
                    continue;
                }

                String itemId = parts[0].trim();
                int requiredCount = Integer.parseInt(parts[1].trim());

                // 获取物品
                ResourceLocation itemRes = ResourceLocation.tryParse(itemId);
                if (itemRes == null) {
                    LoggerUtil.warn("无效的物品ID格式: {}", itemId);
                    continue;
                }

                Item item = BuiltInRegistries.ITEM.get(itemRes);
                if (item == null) {
                    LoggerUtil.warn("未找到物品: {}", itemId);
                    continue;
                }

                // 检查主手
                ItemStack mainHand = player.getMainHandItem();
                if (mainHand.getItem() == item && mainHand.getCount() >= requiredCount) {
                    mainHand.shrink(requiredCount);
                    LoggerUtil.info("消耗 {} 个 {} (主手)", requiredCount, itemId);
                    return true;
                }

                // 检查副手
                ItemStack offHand = player.getOffhandItem();
                if (offHand.getItem() == item && offHand.getCount() >= requiredCount) {
                    offHand.shrink(requiredCount);
                    LoggerUtil.info("消耗 {} 个 {} (副手)", requiredCount, itemId);
                    return true;
                }

            } catch (Exception e) {
                LoggerUtil.warn("解析GUI消耗物品配置失败: {}，错误: {}", configEntry, e.getMessage());
            }
        }

        // 没有匹配的配置物品，发送详细需求消息
        String requiredItems = Config.getRequiredCostItemsDisplayString();
        player.displayClientMessage(
                Component.translatable("message.wandering_trader_express_delivery.need_cost_item_detail", requiredItems),
                false
        );

        LoggerUtil.debug("玩家未持有配置中指定的消耗物品，需要: {}", requiredItems);
        return false;
    }

    /**
     * 默认消耗绿宝石（兼容旧配置）
     * @param player 玩家实例
     * @return 是否成功消耗绿宝石
     */
    private boolean consumeDefaultEmerald(Player player) {
        if (player.getMainHandItem().getItem() == net.minecraft.world.item.Items.EMERALD) {
            player.getMainHandItem().shrink(1);
            return true;
        } else if (player.getOffhandItem().getItem() == net.minecraft.world.item.Items.EMERALD) {
            player.getOffhandItem().shrink(1);
            return true;
        }

        // 未持有绿宝石，发送消息
        player.displayClientMessage(
                Component.translatable("message.wandering_trader_express_delivery.need_cost_item_detail", "绿宝石 x1"),
                false
        );
        return false;
    }

    /**
     * 创建快递数据
     * 修正：保存开始时间（绝对时间）和相对消失时间
     * 新增：稀有快递机制
     * @param player 玩家实例
     * @param selectedItem 选择的物品
     * @return 快递数据对象
     */
    private DeliveryData createDeliveryData(Player player, ItemStack selectedItem) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos destination = player.blockPosition();

        long currentTime = level.getGameTime(); // 获取当前时间（绝对时间）

        // 计算消失时间（相对时长）
        double disappearTimeMinutes = Config.getSafeDeliveryDisappearTime();
        long disappearTicks = Config.minutesToTicks(disappearTimeMinutes); // 计算消失时长（相对时间）

        // 判断是否为稀有快递（20%概率）
        boolean isRareDelivery = checkRareDelivery();
        if (isRareDelivery) {
            LoggerUtil.info("生成稀有快递: 玩家={}", player.getUUID());
            // 发送稀有快递消息给玩家
            player.displayClientMessage(
                    Component.translatable("message.wandering_trader_express_delivery.rare_delivery")
                            .withStyle(net.minecraft.ChatFormatting.GOLD),
                    false
            );
        }

        // 判断是否被拦截（稀有快递拦截概率+30%）
        boolean isIntercepted = checkInterception(isRareDelivery);
        BlockPos interceptPoint = null;

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            isIntercepted = false;
        }

        // 如果被拦截，生成拦截点
        if (isIntercepted) {
            interceptPoint = generateInterceptPoint(level, destination);

            // 如果拦截点生成失败，发送消息并取消拦截
            if (interceptPoint == null) {
                player.displayClientMessage(
                        Component.translatable("message.wandering_trader_express_delivery.delivery_stolen")
                                .withStyle(net.minecraft.ChatFormatting.BLUE),
                        false
                );
                isIntercepted = false; // 取消拦截状态
            }
        }

        // 生成起始位置
        BlockPos startPosition = generateStartPosition(level, destination, isIntercepted, interceptPoint);
        if (startPosition == null) return null;

        // 创建数据对象 - 保存开始时间（绝对时间）和相对消失时长
        DeliveryData deliveryData = new DeliveryData(player.getUUID(), selectedItem.copy(),
                destination, currentTime, disappearTicks, isIntercepted, isRareDelivery);
        deliveryData.currentPosition = startPosition;
        deliveryData.interceptPoint = interceptPoint;

        return deliveryData;
    }

    /**
     * 注册快递
     * 修正：使用开始时间和相对消失时间计算绝对消失时间
     * 新增：添加类型2消失事件的调试日志
     * @param deliveryData 快递数据
     * @param player 玩家实例
     */
    private void registerDelivery(DeliveryData deliveryData, Player player) {
        activeDeliveries.put(deliveryData.playerId, deliveryData);

        // 计算绝对消失时间（开始时间 + 相对消失时间）
        long absoluteDisappearTime = deliveryData.getDisappearTime();

        // 注册消失事件 - 类型2（快递过期）
        WanderingTraderExpressDelivery.pendingDisappearanceManager.addPendingDisappearance(
                deliveryData.playerId, absoluteDisappearTime, 2
        );

        // 发送开始消息 - 使用国际化翻译键
        Component message = Component.translatable(
                "message.wandering_trader_express_delivery.trader_departed",
                deliveryData.selectedItem.getDisplayName().getString(),
                deliveryData.currentPosition.getX() + ", " + deliveryData.currentPosition.getZ()
        );
        player.displayClientMessage(message, false);

        LoggerUtil.info("📦 注册快递和类型2消失事件: 玩家={}, 开始时间={}, 相对消失时长={} ticks, 绝对消失时间={}, 稀有快递={}",
                deliveryData.playerId, deliveryData.generationTime, deliveryData.relativeDisappearTicks,
                absoluteDisappearTime, deliveryData.isRareDelivery);
    }

    /**
     * 检查是否为稀有快递
     * 新增：20%概率生成稀有快递
     * @return 是否为稀有快递
     */
    private boolean checkRareDelivery() {
        return new Random().nextDouble() < 0.2; // 20%概率
    }

    /**
     * 检查是否被拦截
     * 修改：稀有快递拦截概率+30%
     * @param isRareDelivery 是否为稀有快递
     * @return 是否被拦截
     */
    private boolean checkInterception(boolean isRareDelivery) {
        Random random = new Random();
        int baseChance = Config.getInterceptChance();
        int actualChance = baseChance;

        if (isRareDelivery && baseChance !=0) {
            actualChance = Math.min(100, baseChance + 30); // 拦截概率+30%，不超过100%
            LoggerUtil.info("稀有快递拦截概率: 基础={}%, 实际={}%", baseChance, actualChance);
        }

        return random.nextInt(100) < actualChance;
    }

    /**
     * 生成快递起始位置
     * 修正：添加拦截点参数
     * @param level 服务器世界实例
     * @param destination 目的地位置
     * @param isIntercepted 是否被拦截
     * @param interceptPoint 拦截点位置
     * @return 起始位置
     */
    private BlockPos generateStartPosition(ServerLevel level, BlockPos destination, boolean isIntercepted, BlockPos interceptPoint) {
        Random random = new Random();
        int minDistance = 16 * 16; // 16区块
        int maxDistance = 24 * 16; // 24区块

        if (isIntercepted && interceptPoint != null) {
            // 在拦截点延长线上生成起始点
            return generatePositionOnExtension(destination, interceptPoint, minDistance, maxDistance, random, level);
        } else {
            // 随机方向生成起始点
            return generateRandomPosition(destination, minDistance, maxDistance, random, level);
        }
    }

    /**
     * 在延长线上生成位置
     * @param from 起始点
     * @param to 目标点
     * @param minDist 最小距离
     * @param maxDist 最大距离
     * @param random 随机数生成器
     * @param level 服务器世界实例
     * @return 生成的位置
     */
    private BlockPos generatePositionOnExtension(BlockPos from, BlockPos to, int minDist, int maxDist,
                                                 Random random, ServerLevel level) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();

        double distance = Math.sqrt(dx * dx + dz * dz);
        double unitX = dx / distance;
        double unitZ = dz / distance;

        int randomDistance = minDist + random.nextInt(maxDist - minDist);
        int startX = from.getX() + (int)(-unitX * randomDistance);
        int startZ = from.getZ() + (int)(-unitZ * randomDistance);

        int startY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, startX, startZ);
        return new BlockPos(startX, startY, startZ);
    }

    /**
     * 随机生成位置
     * @param center 中心位置
     * @param minDist 最小距离
     * @param maxDist 最大距离
     * @param random 随机数生成器
     * @param level 服务器世界实例
     * @return 生成的位置
     */
    private BlockPos generateRandomPosition(BlockPos center, int minDist, int maxDist,
                                            Random random, ServerLevel level) {
        double angle = random.nextDouble() * 2 * Math.PI;
        int distance = minDist + random.nextInt(maxDist - minDist);

        int startX = center.getX() + (int)(Math.cos(angle) * distance);
        int startZ = center.getZ() + (int)(Math.sin(angle) * distance);
        int startY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, startX, startZ);

        return new BlockPos(startX, startY, startZ);
    }

    /**
     * 生成拦截点 - 修正：完全符合要求的实现
     * 在玩家在2区块至玩家设置的视野区块减1区块（最小不能小于2）的范围内，随机选取一个点1，点1Y轴与玩家Y轴相同
     * 在点1水平方向+-8格范围内随机选取一个点2，从点2开始在点2的Y轴上搜索第一个露天的固体方块
     * 该方块不能是树叶，该方块上方5格为空气，第一格空气也可以为雪、草、花
     * 点1最多搜索5次，点2最多搜索3次
     * @param level 服务器世界实例
     * @param playerPos 玩家位置
     * @return 拦截点位置
     */
    private BlockPos generateInterceptPoint(ServerLevel level, BlockPos playerPos) {
        Random random = new Random();
        int serverViewDistance = level.getServer().getPlayerList().getViewDistance();
        int playerViewDistance = Math.max(2, serverViewDistance - 1);

        int minDistance = 2 * 16; // 2区块
        int maxDistance = playerViewDistance * 16;

        // 点1最多搜索5次
        for (int point1Attempt = 0; point1Attempt < 5; point1Attempt++) {
            // 生成点1：在范围内随机，Y轴与玩家相同
            BlockPos point1 = generatePoint1(playerPos, minDistance, maxDistance, random);

            // 点2最多搜索3次
            for (int point2Attempt = 0; point2Attempt < 3; point2Attempt++) {
                // 在点1水平方向±8格内随机点2
                BlockPos point2 = generatePoint2(point1, random);

                // 从点2开始向下搜索第一个露天固体方块
                BlockPos interceptPoint = findInterceptPointDownwards(level, point2, playerPos.getY());

                if (interceptPoint != null) {
                    LoggerUtil.info("成功生成拦截点: 玩家位置={}, 拦截点={}", playerPos, interceptPoint);
                    return interceptPoint;
                }
            }
        }

        // 5次点1搜索都失败
        LoggerUtil.warn("拦截点生成失败: 玩家位置={}, 搜索范围={}-{}区块",
                playerPos, minDistance/16, maxDistance/16);
        return null;
    }

    /**
     * 生成点1：在玩家周围随机距离和方向，Y轴与玩家相同
     * @param playerPos 玩家位置
     * @param minDist 最小距离
     * @param maxDist 最大距离
     * @param random 随机数生成器
     * @return 点1位置
     */
    private BlockPos generatePoint1(BlockPos playerPos, int minDist, int maxDist, Random random) {
        double angle = random.nextDouble() * 2 * Math.PI;
        int distance = minDist + random.nextInt(maxDist - minDist + 1);

        int x = playerPos.getX() + (int)(Math.cos(angle) * distance);
        int z = playerPos.getZ() + (int)(Math.sin(angle) * distance);

        // 点1的Y轴与玩家Y轴相同
        return new BlockPos(x, playerPos.getY(), z);
    }

    /**
     * 生成点2：在点1水平方向±8格内随机
     * @param point1 点1位置
     * @param random 随机数生成器
     * @return 点2位置
     */
    private BlockPos generatePoint2(BlockPos point1, Random random) {
        int offsetX = random.nextInt(17) - 8; // -8 到 +8
        int offsetZ = random.nextInt(17) - 8; // -8 到 +8

        return new BlockPos(
                point1.getX() + offsetX,
                point1.getY(), // Y轴保持不变
                point1.getZ() + offsetZ
        );
    }

    /**
     * 从点2向下搜索第一个露天固体方块
     * 修正：确保固体方块是露天的且有足够上方空间
     * @param level 服务器世界实例
     * @param point2 点2位置
     * @param playerY 玩家Y坐标
     * @return 拦截点位置
     */
    private BlockPos findInterceptPointDownwards(ServerLevel level, BlockPos point2, int playerY) {
        // 从点2的Y坐标开始向下搜索，直到世界底部或玩家Y-20（避免搜太深）
        int startY = point2.getY()+30;
        int minY = Math.max(level.getMinBuildHeight(), playerY - 20);

        for (int y = startY; y >= minY; y--) {
            BlockPos currentPos = new BlockPos(point2.getX(), y, point2.getZ());

            // 检查是否是固体方块（非空气、非树叶）且是露天的
            if (isSolidBlock(level, currentPos) &&
                    !level.getBlockState(currentPos).is(net.minecraft.tags.BlockTags.LEAVES) &&
                    isExposedToSky(level, currentPos) && // 检查是否露天
                    isAboveSpaceValid(level, currentPos)) { // 检查上方5格空间
                return currentPos; // 返回固体方块本身作为拦截点
            }
        }

        return null;
    }

    /**
     * 检查方块是否暴露在天空下（露天）
     * 修正：从该方块向上检查直到世界顶部，确保没有非透明方块遮挡
     * @param level 服务器世界实例
     * @param pos 位置
     * @return 是否露天
     */
    private boolean isExposedToSky(ServerLevel level, BlockPos pos) {
        // 从该方块向上检查直到世界顶部，确保没有非透明方块遮挡
        for (int y = pos.getY() + 1; y <= level.getMaxBuildHeight(); y++) {
            BlockPos abovePos = new BlockPos(pos.getX(), y, pos.getZ());
            var blockState = level.getBlockState(abovePos);

            // 如果遇到非透明方块，说明不是露天的
            if (!blockState.isAir() &&
                    !blockState.is(net.minecraft.world.level.block.Blocks.SNOW) &&
                    !blockState.is(net.minecraft.tags.BlockTags.SMALL_FLOWERS) &&
                    !blockState.is(net.minecraft.tags.BlockTags.TALL_FLOWERS) &&
                    blockState.canOcclude()) { // canOcclude() 检查方块是否遮挡光线
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否是固体方块
     * @param level 服务器世界实例
     * @param pos 位置
     * @return 是否是固体方块
     */
    private boolean isSolidBlock(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).isAir() &&
                level.getBlockState(pos).isSolid();
    }

    /**
     * 检查上方5格空间是否符合要求（确保有足够空间生成箱子和劫匪）
     * 修正：第1格可以是空气、雪、草、花，第2-5格必须是空气
     * @param level 服务器世界实例
     * @param solidBlockPos 固体方块位置
     * @return 上方空间是否有效
     */
    private boolean isAboveSpaceValid(ServerLevel level, BlockPos solidBlockPos) {
        // 检查上方5格
        for (int i = 1; i <= 5; i++) {
            BlockPos abovePos = solidBlockPos.above(i);

            if (i == 1) {
                // 第一格可以是空气、雪、草、花
                if (!isValidFirstBlock(level, abovePos)) {
                    return false;
                }
            } else {
                // 其他格必须是空气，确保有足够空间
                if (!level.getBlockState(abovePos).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查第一格是否有效（保持原有逻辑）
     * @param level 服务器世界实例
     * @param pos 位置
     * @return 第一格是否有效
     */
    private boolean isValidFirstBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir() ||
                level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.SNOW) ||
                level.getBlockState(pos).is(net.minecraft.tags.BlockTags.SMALL_FLOWERS) ||
                level.getBlockState(pos).is(net.minecraft.tags.BlockTags.TALL_FLOWERS);
    }

    /**
     * 处理正常到达
     * 修正：确保快递正确进入待移除状态，但保留快递冷却（类型2）
     * 修正：到达点未加载时注册待管理事件
     * 新增：传递稀有快递状态到待生成事件
     */
    public void handleNormalArrival(DeliveryData delivery) {
        ServerLevel level = getPlayerLevel(delivery.playerId);
        if (level == null) return;

        // 移除活跃快递，但快递冷却（类型2）应该继续存在
        activeDeliveries.remove(delivery.playerId);
        LoggerUtil.info("快递正常到达，移除活跃快递但保留冷却: 玩家={}, 稀有快递={}", delivery.playerId, delivery.isRareDelivery);

        // 修正：检查到达点是否加载，如果未加载则注册待管理事件
        if (level.isLoaded(delivery.destination)) {
            // 生成流浪商人
            WanderingTraderExpressDelivery.interceptManager.spawnWanderingTrader(delivery);
        } else {
            // 注册待处理事件 - 修正：使用快递消失时间的一半作为待处理时间，传递稀有状态
            registerPendingManagement(delivery.playerId, level.getGameTime(), 1, delivery.destination, delivery.isRareDelivery, delivery.selectedItem);

            // ✅ 新增：发送等待消息给玩家
            sendWaitingMessage(delivery.playerId, 1, delivery.destination);

            LoggerUtil.info("到达点未加载，注册待生成流浪商人事件: 玩家={}, 位置={}, 稀有快递={}",
                    delivery.playerId, delivery.destination, delivery.isRareDelivery);
        }

        // 注意：这里不调用 removePlayerDelivery，因为快递冷却应该继续存在
        // 快递冷却（类型2）会在过期时由 PendingDisappearanceManager 自动处理
    }

    /**
     * 处理被拦截到达 - 修复：修改拦截消息格式
     * 修正：确保快递被拦截时正确进入待移除状态，但不移除快递冷却（类型2）
     * 修正：拦截点未加载时注册待管理事件
     * 新增：传递稀有快递状态到待生成事件
     */
    public void handleInterceptedArrival(DeliveryData delivery) {
        ServerLevel level = getPlayerLevel(delivery.playerId);
        if (level == null) {
            LoggerUtil.error("无法获取玩家世界: 玩家={}", delivery.playerId);
            return;
        }

        // 移除活跃快递，但快递冷却（类型2）应该继续存在
        activeDeliveries.remove(delivery.playerId);
        LoggerUtil.info("快递被拦截，移除活跃快递但保留冷却: 玩家={}, 稀有快递={}", delivery.playerId, delivery.isRareDelivery);

        // 修正：检查拦截点是否加载，如果未加载则注册待管理事件
        if (level.isLoaded(delivery.interceptPoint)) {
            // 生成劫匪
            WanderingTraderExpressDelivery.interceptManager.spawnBandits(delivery);

            // 发送拦截消息 - 修复：修改消息格式，使用国际化翻译键
            sendInterceptMessage(delivery.playerId, delivery.interceptPoint, false);
        } else {
            // 注册待处理事件 - 修正：使用快递消失时间的一半作为待处理时间，传递稀有状态
            registerPendingManagement(delivery.playerId, level.getGameTime(), 2, delivery.interceptPoint, delivery.isRareDelivery, delivery.selectedItem);

            // ✅ 新增：发送劫匪等待消息给玩家
            sendWaitingMessage(delivery.playerId, 2, delivery.interceptPoint);

            LoggerUtil.info("拦截点未加载，注册待生成箱子和劫匪事件: 玩家={}, 位置={}, 稀有快递={}",
                    delivery.playerId, delivery.interceptPoint, delivery.isRareDelivery);
        }

        LoggerUtil.info("拦截事件处理完成，保留快递冷却: 玩家={}, 稀有快递={}", delivery.playerId, delivery.isRareDelivery);
    }

    /**
     * 注册待处理管理事件
     * 修正：使用快递消失时间的一半作为待处理时间，并存储位置信息
     * 新增：支持存储稀有快递状态用于后续生成
     * 新增：支持存储选择的物品用于后续生成
     * @param playerId 玩家UUID
     * @param currentTime 当前时间（绝对时间）
     * @param type 事件类型（1-2）
     * @param position 生成位置
     * @param isRareDelivery 稀有快递状态
     * @param selectedItem 选择的物品
     */
    private void registerPendingManagement(UUID playerId, long currentTime, int type, BlockPos position, boolean isRareDelivery, ItemStack selectedItem) {
        // 修正：使用快递消失时间的一半作为待处理时间
        double pendingTimeMinutes = Config.getHalfDeliveryDisappearTime();
        long manageTime = currentTime + Config.minutesToTicks(pendingTimeMinutes);

        WanderingTraderExpressDelivery.pendingManagementManager.addPendingManagement(
                playerId, manageTime, type, position, isRareDelivery, selectedItem
        );

        LoggerUtil.info("注册待管理事件: 类型={}, 玩家={}, 位置={}, 处理时间={} ticks ({}分钟), 稀有快递={}, 物品={}",
                type, playerId, position, Config.minutesToTicks(pendingTimeMinutes), pendingTimeMinutes, isRareDelivery, selectedItem.getDescriptionId());
    }

    /**
     * 发送拦截消息 - 修复：修改消息格式，使用国际化翻译键
     * @param playerId 玩家UUID
     * @param interceptPoint 拦截点位置
     * @param isPending 是否待处理
     */
    private void sendInterceptMessage(UUID playerId, BlockPos interceptPoint, boolean isPending) {
        Player player = CommonUtils.getPlayer(playerId);
        if (player == null) return;

        // 使用国际化翻译键
        Component messageComponent = Component.translatable(
                "message.wandering_trader_express_delivery.delivery_intercepted",
                interceptPoint.getX(),
                interceptPoint.getY(),
                interceptPoint.getZ()
        );

        if (isPending) {
            // 拦截点未加载的情况
            messageComponent = messageComponent.copy().withStyle(net.minecraft.ChatFormatting.YELLOW);
        } else {
            // 拦截点已加载的情况
            messageComponent = messageComponent.copy().withStyle(net.minecraft.ChatFormatting.RED);
        }

        player.displayClientMessage(messageComponent, false);
    }

    /**
     * 移除玩家的快递（包括冷却）
     * 修正：这个方法应该在快递过期时由 DeliveryExpirationEvent 调用
     * @param playerId 玩家UUID
     */
    public void removePlayerDelivery(UUID playerId) {
        // 移除活跃快递数据
        activeDeliveries.remove(playerId);
        // 清理交易价格信息
        WanderingTraderExpressDelivery.tradePriceManager.removePlayerTradePrice(playerId);
        LoggerUtil.info("完全移除玩家快递（包括冷却）: 玩家={}", playerId);
    }

    // ==================== 数据持久化方法 ====================

    /**
     * 保存快递数据
     * 修改：不再保存活跃快递，只由 PendingDisappearanceManager 统一管理消失时间
     */
    public void saveData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) return;

            // 使用存档data文件夹
            Path dataDir = CommonUtils.getModDataDir(overworld);
            Files.createDirectories(dataDir);

            Path dataFile = dataDir.resolve("active_deliveries.dat");
            CompoundTag rootTag = new CompoundTag();

            // 🚫 不再保存任何活跃快递数据
            ListTag deliveriesList = new ListTag(); // 空列表

            rootTag.put("activeDeliveries", deliveriesList);

            // 保存空文件到文件
            net.minecraft.nbt.NbtIo.writeCompressed(rootTag, dataFile);

            LoggerUtil.info("✅ 快递数据保存完成 - 不保存活跃快递，只由消失事件管理器管理类型2事件");

        } catch (IOException e) {
            LoggerUtil.error("💥 保存快递数据失败", e);
        }
    }

    /**
     * 清理损坏的数据文件
     * 新增：删除损坏的数据文件，让系统重新生成
     */
    public void cleanupCorruptedData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) return;

            Path dataDir = CommonUtils.getModDataDir(overworld);
            Path dataFile = dataDir.resolve("active_deliveries.dat");

            if (Files.exists(dataFile)) {
                try {
                    // 尝试读取文件，如果失败则删除
                    net.minecraft.nbt.NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
                    LoggerUtil.info("数据文件验证成功: {}", dataFile);
                } catch (Exception e) {
                    LoggerUtil.warn("数据文件损坏，删除并重新生成: {}", dataFile);
                    Files.deleteIfExists(dataFile);
                }
            }
        } catch (IOException e) {
            LoggerUtil.error("清理损坏数据时出错", e);
        }
    }



    /**
     * 取消所有活跃快递的移动
     * 新增：服务器关闭时取消快递移动
     */
    public void cancelAllActiveDeliveries() {
        int cancelledCount = activeDeliveries.size();
        activeDeliveries.clear();
        LoggerUtil.info("取消了 {} 个活跃快递的移动", cancelledCount);
    }

    private ServerLevel getPlayerLevel(UUID playerId) {
        return CommonUtils.getPlayerLevel(playerId);
    }

    /**
     * 发送等待消息给玩家
     * 新增：根据事件类型发送不同的等待消息
     * @param playerId 玩家UUID
     * @param type 事件类型（1=流浪商人，2=劫匪）
     * @param position 等待位置
     */
    private void sendWaitingMessage(UUID playerId, int type, BlockPos position) {
        Player player = CommonUtils.getPlayer(playerId);
        if (player == null) return;

        Component message;
        switch (type) {
            case 1: // 流浪商人
                message = Component.translatable(
                        "message.wandering_trader_express_delivery.trader_waiting",
                        position.getX(), position.getY(), position.getZ()
                ).withStyle(net.minecraft.ChatFormatting.YELLOW);
                break;
            case 2: // 劫匪
                message = Component.translatable(
                        "message.wandering_trader_express_delivery.bandits_waiting",
                        position.getX(), position.getY(), position.getZ()
                ).withStyle(net.minecraft.ChatFormatting.RED);
                break;
            default:
                return;
        }

        player.displayClientMessage(message, false);
        LoggerUtil.info("发送等待消息: 类型={}, 玩家={}, 位置={}", type, playerId, position);
    }
}