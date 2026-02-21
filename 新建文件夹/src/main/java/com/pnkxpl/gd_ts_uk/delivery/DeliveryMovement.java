package com.pnkxpl.gd_ts_uk.delivery;

import com.pnkxpl.gd_ts_uk.utils.EnvironmentChecker;
import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.utils.CommonUtils;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.network.chat.Component;

import java.util.Random;
import java.util.UUID;

/**
 * 快递移动类 - 专门处理快递的移动逻辑
 * 优化：简化移动计算，改进到达检测
 * 优化：使用公共工具类，减少代码重复
 * 修正：调整移动速度范围，速度3时最小3最大5，速度2各减少1，速度1再减少1
 * 新增：使用国际化翻译键，支持多语言
 * 修正：快递时间计算问题 - 使用开始时间和相对消失时间进行正确的过期检查
 * 修正：移动速度逻辑 - 根据到达点的生物群系注册名判断
 *         恶劣地形有70%概率速度-1，非恶劣地形有30%概率速度-1
 */
public class DeliveryMovement {

    // 随机数生成器
    private static final Random RANDOM = new Random();

    // 速度影响概率配置
    private static final double HARSH_BIOME_SLOW_PROBABILITY = 0.70; // 恶劣生物群系70%概率减速
    private static final double NORMAL_BIOME_SLOW_PROBABILITY = 0.30; // 普通生物群系30%概率减速

    /**
     * 移动快递到下一个位置
     */
    public static void moveDelivery(DeliveryManager.DeliveryData delivery) {
        // 基础检查
        if (!isDeliveryValid(delivery)) {
            return;
        }

        // 更新移动速度 - 修正：基于到达点生物群系，区分恶劣和普通地形概率
        updateMoveSpeed(delivery);

        // 计算并移动到新位置
        BlockPos newPosition = calculateNextPosition(delivery);
        if (newPosition != null) {
            delivery.currentPosition = newPosition;
            delivery.lastMoveTime = getCurrentWorldTime();

            // 检查到达情况
            if (!checkArrival(delivery)) {
                sendMoveMessage(delivery);
            }
        }
    }

    /**
     * 检查快递是否有效
     * 修正：使用开始时间和相对消失时间计算是否过期
     */
    private static boolean isDeliveryValid(DeliveryManager.DeliveryData delivery) {
        if (!WanderingTraderExpressDelivery.deliveryManager.activeDeliveries.containsKey(delivery.playerId)) {
            return false;
        }

        long currentTime = getCurrentWorldTime();
        // 使用开始时间和相对消失时间计算是否过期
        if (delivery.isExpired(currentTime)) {
            LoggerUtil.info("快递已过期: 玩家={}, 开始时间={}, 相对消失时长={}, 当前时间={}, 过期时间={}",
                    delivery.playerId, delivery.generationTime, delivery.relativeDisappearTicks, currentTime, delivery.getDisappearTime());
            WanderingTraderExpressDelivery.deliveryManager.removePlayerDelivery(delivery.playerId);
            return false;
        }

        return true;
    }

    /**
     * 更新移动速度
     * 修正：根据到达点的生物群系注册名判断
     *       恶劣地形有70%概率速度-1，非恶劣地形有30%概率速度-1
     * 注意：在拦截情况下，快递的最终目的地是拦截点（到达点）
     */
    private static void updateMoveSpeed(DeliveryManager.DeliveryData delivery) {
        ServerLevel level = getPlayerLevel(delivery.playerId);
        if (level == null) {
            delivery.moveSpeed = 3;
            return;
        }

        int adjustedSpeed = 3;

        // 目的地天气影响 - 固定减速
        if (EnvironmentChecker.isRaining(level, delivery.destination)) {
            adjustedSpeed--;
            LoggerUtil.debug("天气影响: 玩家={}, 速度-1", delivery.playerId);
        }

        // 目的地生物群系影响 - 修正：区分恶劣和普通地形概率
        boolean isHarshBiome = isDestinationHarshBiome(level, delivery.destination);
        double slowProbability = isHarshBiome ? HARSH_BIOME_SLOW_PROBABILITY : NORMAL_BIOME_SLOW_PROBABILITY;

        if (RANDOM.nextDouble() < slowProbability) {
            adjustedSpeed--;
            String biomeType = isHarshBiome ? "恶劣" : "普通";
            LoggerUtil.debug("{}地形影响: 玩家={}, 速度-1 (概率={})",
                    biomeType, delivery.playerId, slowProbability);
        } else {
            String biomeType = isHarshBiome ? "恶劣" : "普通";
            LoggerUtil.debug("{}地形但未影响速度: 玩家={} (概率={})",
                    biomeType, delivery.playerId, slowProbability);
        }

        // 确保速度在有效范围内
        delivery.moveSpeed = Math.max(1, Math.min(3, adjustedSpeed));

        LoggerUtil.debug("最终移动速度: 玩家={}, 速度={}, 目的地={}",
                delivery.playerId, delivery.moveSpeed, delivery.destination);
    }

    /**
     * 检查目的地是否为恶劣生物群系
     * 修正：基于生物群系注册名进行判断，即使位置未加载也能工作
     *
     * @param level 服务器世界
     * @param destination 目的地位置
     * @return 如果是恶劣生物群系返回true，普通生物群系返回false
     */
    private static boolean isDestinationHarshBiome(ServerLevel level, BlockPos destination) {
        // 如果目的地已加载，直接检查生物群系
        if (level.isLoaded(destination)) {
            return EnvironmentChecker.isHarshBiome(level, destination);
        }

        // 如果目的地未加载，尝试通过生物群系注册表判断
        // 注意：这种方法不如直接检查位置准确，但可以在位置未加载时工作
        try {
            // 获取生物群系注册表
            var biomeRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);

            // 获取目的地的生物群系（如果位置已加载这会正常工作）
            var biome = level.getBiome(destination).value();
            var biomeKey = biomeRegistry.getKey(biome);

            if (biomeKey != null) {
                String biomePath = biomeKey.getPath().toLowerCase();

                // 恶劣生物群系关键词
                String[] harshBiomeKeywords = {
                        "snow", "ice", "frozen", "cold", "taiga",
                        "desert", "badlands", "arid", "mesa",
                        "ocean", "deep_ocean", "beach", "river",
                        "waste", "mushroom", "stony", "cave", "void"
                };

                // 检查是否包含恶劣生物群系关键词
                for (String keyword : harshBiomeKeywords) {
                    if (biomePath.contains(keyword)) {
                        LoggerUtil.debug("检测到恶劣生物群系: {} -> {}", biomeKey, keyword);
                        return true;
                    }
                }

                // 如果没有匹配恶劣关键词，则是普通生物群系
                LoggerUtil.debug("检测到普通生物群系: {}", biomeKey);
                return false;
            }
        } catch (Exception e) {
            LoggerUtil.warn("检查目的地生物群系时出错: 位置={}", destination, e);
        }

        // 默认情况下，如果无法确定，则视为普通生物群系
        LoggerUtil.debug("无法确定生物群系类型，默认视为普通: 位置={}", destination);
        return false;
    }

    /**
     * 计算下一个位置
     */
    private static BlockPos calculateNextPosition(DeliveryManager.DeliveryData delivery) {
        BlockPos current = delivery.currentPosition;
        BlockPos destination = delivery.destination;

        // 计算方向向量
        int dx = destination.getX() - current.getX();
        int dz = destination.getZ() - current.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        // 如果已经很近，直接到达
        if (distance <= delivery.moveSpeed * 16) {
            return destination;
        }

        // 计算移动
        double unitX = dx / distance;
        double unitZ = dz / distance;
        int moveDistance = calculateMoveDistance(delivery.moveSpeed);

        int newX = current.getX() + (int)(unitX * moveDistance);
        int newZ = current.getZ() + (int)(unitZ * moveDistance);

        // 获取地形高度
        ServerLevel level = getPlayerLevel(delivery.playerId);
        if (level != null) {
            int newY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, newX, newZ);
            return new BlockPos(newX, newY, newZ);
        }

        return new BlockPos(newX, current.getY(), newZ);
    }

    /**
     * 计算移动距离
     * 修正：调整移动速度范围
     * - 速度3：最小3区块，最大5区块
     * - 速度2：最小2区块，最大4区块
     * - 速度1：最小1区块，最大3区块
     */
    private static int calculateMoveDistance(int moveSpeed) {
        // 根据速度等级确定移动范围
        int minMove, maxMove;
        switch (moveSpeed) {
            case 1:
                minMove = 1;  // 最小1区块
                maxMove = 3;  // 最大3区块
                break;
            case 2:
                minMove = 2;  // 最小2区块
                maxMove = 4;  // 最大4区块
                break;
            case 3:
            default:
                minMove = 3;  // 最小3区块
                maxMove = 5;  // 最大5区块
                break;
        }

        // 转换为格数（1区块=16格）
        int minMoveBlocks = minMove * 16;
        int maxMoveBlocks = maxMove * 16;

        return minMoveBlocks + RANDOM.nextInt(maxMoveBlocks - minMoveBlocks + 1);
    }

    /**
     * 检查是否到达目的地
     */
    private static boolean checkArrival(DeliveryManager.DeliveryData delivery) {
        double distanceToDestination = calculateDistance(delivery.currentPosition, delivery.destination);

        // 检查是否到达目的地（3区块内）
        if (distanceToDestination <= 3 * 16) {
            triggerArrival(delivery);
            return true;
        }

        // 检查是否越过拦截点
        if (delivery.isIntercepted && delivery.interceptPoint != null &&
                hasPassedInterceptPoint(delivery)) {
            LoggerUtil.info("快递越过拦截点: 玩家={}", delivery.playerId);
            WanderingTraderExpressDelivery.deliveryManager.handleInterceptedArrival(delivery);
            return true;
        }

        return false;
    }

    /**
     * 检查是否越过拦截点
     */
    private static boolean hasPassedInterceptPoint(DeliveryManager.DeliveryData delivery) {
        double currentToDest = calculateDistance(delivery.currentPosition, delivery.destination);
        double interceptToDest = calculateDistance(delivery.interceptPoint, delivery.destination);
        return currentToDest < interceptToDest;
    }

    /**
     * 触发到达事件
     */
    private static void triggerArrival(DeliveryManager.DeliveryData delivery) {
        if (delivery.isIntercepted) {
            WanderingTraderExpressDelivery.deliveryManager.handleInterceptedArrival(delivery);
        } else {
            WanderingTraderExpressDelivery.deliveryManager.handleNormalArrival(delivery);
        }
    }

    /**
     * 发送移动消息 - 使用国际化翻译键
     */
    private static void sendMoveMessage(DeliveryManager.DeliveryData delivery) {
        Player player = getPlayer(delivery.playerId);
        if (player == null) return;

        double distance = calculateDistance(delivery.currentPosition, delivery.destination);
        String distanceStr = String.format("%.1f区块", distance / 16.0);

        Component message = createMoveMessage(delivery, distanceStr);
        player.displayClientMessage(message, false);
    }

    /**
     * 创建移动消息 - 使用国际化翻译键
     */
    private static Component createMoveMessage(DeliveryManager.DeliveryData delivery, String distanceStr) {
        String position = delivery.currentPosition.getX() + ", " + delivery.currentPosition.getZ();

        // 使用国际化翻译键构建消息
        return Component.translatable(
                "message.wandering_trader_express_delivery.current_position",
                getSpeedDescription(delivery.moveSpeed),
                position,
                distanceStr
        ).withStyle(getSpeedColor(delivery.moveSpeed));
    }

    /**
     * 获取速度描述 - 使用国际化翻译键
     * 修正：根据新的速度范围更新描述
     */
    private static Component getSpeedDescription(int speed) {
        String translationKey = switch (speed) {
            case 1 -> "message.wandering_trader_express_delivery.moving_slow";   // 速度1：最慢
            case 2 -> "message.wandering_trader_express_delivery.moving_medium"; // 速度2：中等
            default -> "message.wandering_trader_express_delivery.moving_fast";  // 速度3：最快
        };
        return Component.translatable(translationKey);
    }

    /**
     * 获取速度颜色
     * 修正：根据新的速度范围更新颜色
     */
    private static net.minecraft.ChatFormatting getSpeedColor(int speed) {
        return switch (speed) {
            case 1 -> net.minecraft.ChatFormatting.RED;     // 速度1：红色
            case 2 -> net.minecraft.ChatFormatting.YELLOW;  // 速度2：黄色
            default -> net.minecraft.ChatFormatting.GREEN;  // 速度3：绿色
        };
    }

    /**
     * 计算两点之间的距离
     */
    public static double calculateDistance(BlockPos pos1, BlockPos pos2) {
        int dx = pos1.getX() - pos2.getX();
        int dz = pos1.getZ() - pos2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    // 工具方法 - 使用公共工具类替代
    private static Player getPlayer(UUID playerId) {
        return CommonUtils.getPlayer(playerId);
    }

    private static ServerLevel getPlayerLevel(UUID playerId) {
        return CommonUtils.getPlayerLevel(playerId);
    }

    private static long getCurrentWorldTime() {
        return CommonUtils.getCurrentWorldTime();
    }

    private static ServerLevel getOverworld() {
        return CommonUtils.getOverworld();
    }
}