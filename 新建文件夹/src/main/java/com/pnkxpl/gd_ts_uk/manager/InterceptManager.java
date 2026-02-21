package com.pnkxpl.gd_ts_uk.manager;

import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.utils.CommonUtils;
import com.pnkxpl.gd_ts_uk.delivery.DeliveryManager;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import com.pnkxpl.gd_ts_uk.utils.RareItemGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.network.chat.Component;

import java.util.*;

/**
 * 拦截管理器 - 处理拦截点的劫匪生成和物品管理
 * 优化：简化代码，移除所有箱子相关逻辑
 * 优化：使用公共工具类，减少代码重复
 * 新增：使用国际化翻译键，支持多语言
 * 修正：修复PendingManagementManager方法调用参数不匹配的问题
 * 修改：移除箱子相关代码，劫匪生成在拦截点正上方，物品管理交给EntityDeathListener
 * 新增：稀有快递支持，为稀有快递添加稀有物品
 * 修改：方法名从spawnChestAndBandits改为spawnBandits，更符合实际功能
 * 修改：劫匪种类数量可以自定义
 * 新增：修改流浪商人交易价格 - 使用TradePriceManager中存储的交易需求
 * 修改：从TradePriceManager读取玩家选择的交易需求
 */
public class InterceptManager {
    // 存储劫匪掉落物数据：劫匪ID -> 掉落物列表
    private final Map<Integer, List<ItemStack>> banditLootMap = new HashMap<>();

    public InterceptManager() {}

    /**
     * 生成流浪商人
     * 修改：使用TradePriceManager中存储的交易需求
     */
    public void spawnWanderingTrader(DeliveryManager.DeliveryData delivery) {
        ServerLevel level = getPlayerLevel(delivery.playerId);
        if (level == null || level.isClientSide()) {
            return;
        }

        WanderingTrader trader = new WanderingTrader(EntityType.WANDERING_TRADER, level);
        trader.setPos(delivery.destination.getX(), delivery.destination.getY(), delivery.destination.getZ());

        addTradeItemToTrader(trader, delivery);
        level.addFreshEntity(trader);

        sendArrivalMessage(delivery.playerId);
        trader.setDespawnDelay((int) (Config.getHalfDeliveryDisappearTime()*20*60));
    }


    /**
     * 生成劫匪（移除箱子相关代码）
     * 修改：劫匪生成在拦截点正上方，没有偏移
     * 修改：方法名从spawnChestAndBandits改为spawnBandits，更符合实际功能
     * @param delivery 快递数据
     */
    public void spawnBandits(DeliveryManager.DeliveryData delivery) {
        ServerLevel level = getPlayerLevel(delivery.playerId);
        if (level == null) {
            LoggerUtil.error("无法获取玩家世界: 玩家={}", delivery.playerId);
            return;
        }

        if (delivery.interceptPoint == null) {
            LoggerUtil.error("拦截点为null，无法生成劫匪: 玩家={}", delivery.playerId);
            return;
        }

        // 记录生成前的MOB池状态（用于调试）
        if (LoggerUtil.isDebugEnabled()) {
            LoggerUtil.debug("生成劫匪前的MOB池状态: {}", MobConfigManager.getMobPoolInfo());
        }

        // 使用配置的数量范围
        int minCount = Config.getBanditMinCount();
        int maxCount = Config.getBanditMaxCount();
        int banditCount = minCount + new Random().nextInt(maxCount - minCount + 1);

        LoggerUtil.info("生成劫匪: 玩家={}, 拦截点={}, 数量={}-{}->{}, 稀有快递={}, 重载配置={}",
                delivery.playerId, delivery.interceptPoint, minCount, maxCount, banditCount,
                delivery.isRareDelivery, Config.shouldReloadMobPoolOnSpawn());

        if (level.isLoaded(delivery.interceptPoint)) {
            handleLoadedInterceptPoint(delivery, level, banditCount);
        } else {
            handleUnloadedInterceptPoint(delivery, level);
        }
    }

    /**
     * 处理已加载的拦截点
     * 修改：移除箱子生成，只生成劫匪
     * @param delivery 快递数据
     * @param level 服务器世界实例
     */
    private void handleLoadedInterceptPoint(DeliveryManager.DeliveryData delivery, ServerLevel level, int banditCount) {
        // 生成劫匪在拦截点正上方
        BlockPos spawnPos = delivery.interceptPoint.above();

        // 在生成前强制重载MOB池配置
        if (Config.shouldReloadMobPoolOnSpawn()) {
            LoggerUtil.debug("根据配置重载MOB池");
            MobConfigManager.safeReloadMobPool();
        }

        List<Integer> banditIds = spawnBandits(level, spawnPos, delivery, banditCount);

        // 为劫匪设置掉落物
        setupBanditLoot(banditIds, delivery);

        registerBanditsDisappearance(delivery.playerId, level.getGameTime(), banditIds);

        // 记录生成后的MOB池状态（用于调试）
        if (LoggerUtil.isDebugEnabled()) {
            LoggerUtil.debug("生成劫匪后的MOB池状态: {}", MobConfigManager.getMobPoolInfo());
        }
    }

    /**
     * 处理未加载的拦截点
     * 修正：修复PendingManagementManager方法调用参数，添加位置参数和稀有状态
     * @param delivery 快递数据
     * @param level 服务器世界实例
     */
    private void handleUnloadedInterceptPoint(DeliveryManager.DeliveryData delivery, ServerLevel level) {
        LoggerUtil.warn("拦截点未加载，延迟生成: 玩家={}, 拦截点={}, 稀有快递={}",
                delivery.playerId, delivery.interceptPoint, delivery.isRareDelivery);

        // 存储拦截点数据
        WanderingTraderExpressDelivery.pendingManagementManager.storeInterceptPoint(
                delivery.playerId, delivery.interceptPoint);

        // 注册延迟生成事件
        registerDelayedGeneration(delivery.playerId, level.getGameTime(), delivery.interceptPoint, delivery.isRareDelivery, delivery.selectedItem);
    }

    /**
     * 注册延迟生成事件
     * 修正：添加位置参数以匹配PendingManagementManager的方法签名
     * 新增：传递稀有快递状态
     * @param playerId 玩家UUID
     * @param currentTime 当前时间（绝对时间）
     * @param interceptPoint 拦截点位置
     * @param isRareDelivery 稀有快递状态
     */
    private void registerDelayedGeneration(UUID playerId, long currentTime, BlockPos interceptPoint, boolean isRareDelivery, ItemStack selectedItem) {
        double disappearTimeMinutes = Config.getHalfDeliveryDisappearTime();
        long disappearTime = currentTime + Config.minutesToTicks(disappearTimeMinutes);

        // 修正：添加位置参数、稀有状态参数和选择物品参数
        WanderingTraderExpressDelivery.pendingManagementManager.addPendingManagement(
                playerId, disappearTime, 2, interceptPoint, isRareDelivery, selectedItem
        );

        LoggerUtil.info("注册延迟生成事件: 玩家={}, 类型=2, 位置={}, 超时时间={} ticks, 稀有快递={}, 物品={}",
                playerId, interceptPoint, Config.minutesToTicks(disappearTimeMinutes), isRareDelivery, selectedItem.getDescriptionId());
    }

    /**
     * 生成劫匪
     * 修改：劫匪生成在指定位置正上方，没有偏移
     * 修改：劫匪数量种类均可自定义
     * @param level 服务器世界实例
     * @param spawnPos 生成位置
     * @param delivery 快递数据
     * @return 劫匪ID列表
     */
    private List<Integer> spawnBandits(ServerLevel level, BlockPos spawnPos, DeliveryManager.DeliveryData delivery, int count) {
        Random random = new Random();
        List<Integer> banditIds = new ArrayList<>();

        // 记录生成的MOB类型统计
        Map<String, Integer> mobTypeCount = new HashMap<>();

        for (int i = 0; i < count; i++) {
            Mob bandit = createRandomBandit(level, random);
            if (bandit != null) {
                bandit.setPos(
                        spawnPos.getX(),
                        spawnPos.getY(),
                        spawnPos.getZ()
                );

                bandit.setCustomName(Component.translatable("entity.wandering_trader_express_delivery.bandit"));
                bandit.setPersistenceRequired();

                level.addFreshEntity(bandit);
                banditIds.add(bandit.getId());

                // 统计MOB类型
                String mobType = BuiltInRegistries.ENTITY_TYPE.getKey(bandit.getType()).toString();
                mobTypeCount.put(mobType, mobTypeCount.getOrDefault(mobType, 0) + 1);

                LoggerUtil.debug("生成劫匪: ID={}, 类型={}, 位置=({}, {}, {})",
                        bandit.getId(), mobType,
                        spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            } else {
                LoggerUtil.error("创建第 {} 个劫匪失败", i + 1);
            }
        }

        // 记录生成统计
        if (!mobTypeCount.isEmpty()) {
            StringBuilder stats = new StringBuilder("劫匪生成统计: ");
            for (Map.Entry<String, Integer> entry : mobTypeCount.entrySet()) {
                stats.append(entry.getKey()).append("x").append(entry.getValue()).append(", ");
            }
            LoggerUtil.info(stats.substring(0, stats.length() - 2));
        }

        LoggerUtil.info("成功生成 {} 个劫匪，总计 {} 个", banditIds.size(), count);
        return banditIds;
    }

    /**
     * 使用原版生成逻辑创建劫匪
     */
    private Mob createRandomBandit(ServerLevel level, Random random) {
        try {
            MobConfigManager.MobConfig mobConfig = MobConfigManager.getRandomMob(random);
            if (mobConfig == null) {
                LoggerUtil.error("无法获取MOB配置，生成默认掠夺者");
                return createVanillaPillager(level);
            }

            // 使用原版逻辑创建怪物
            Mob mob = createVanillaMob(level, mobConfig.entityType, random);
            if (mob == null) {
                LoggerUtil.error("创建MOB失败: {}", mobConfig.entityId);
                return createVanillaPillager(level);
            }

            // 根据配置添加发光效果
            if (mobConfig.glowing) {
                addGlowingEffectToBandit(mob);
            }

            LoggerUtil.debug("成功创建劫匪: {} (发光: {})", mobConfig.entityId, mobConfig.glowing);
            return mob;

        } catch (Exception e) {
            LoggerUtil.error("创建随机劫匪时出错", e);
            return createVanillaPillager(level);
        }
    }

    /**
     * 使用原版逻辑创建怪物
     */
    private Mob createVanillaMob(ServerLevel level, EntityType<? extends Mob> entityType, Random random) {
        try {
            // 直接创建怪物实例 - 原版会自动处理装备
            Mob mob = entityType.create(level);
            if (mob == null) {
                return null;
            }

            // 调用原版的finalizeSpawn方法，这会触发装备生成
            // 使用EVENT作为生成类型，这是最接近我们使用场景的类型
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                    MobSpawnType.EVENT, null);

            return mob;

        } catch (Exception e) {
            LoggerUtil.error("使用原版逻辑创建怪物时出错: {}", entityType.getDescriptionId(), e);
            return null;
        }
    }

    /**
     * 使用原版逻辑创建掠夺者
     */
    private Pillager createVanillaPillager(ServerLevel level) {
        try {
            Pillager pillager = new Pillager(EntityType.PILLAGER, level);
            // 调用原版的finalizeSpawn方法
            pillager.finalizeSpawn(level, level.getCurrentDifficultyAt(pillager.blockPosition()),
                    MobSpawnType.EVENT, null);
            addGlowingEffectToBandit(pillager);
            return pillager;
        } catch (Exception e) {
            LoggerUtil.error("创建原版掠夺者时出错", e);
            // 最后的回退方案
            return new Pillager(EntityType.PILLAGER, level);
        }
    }

    /**
     * 为劫匪添加发光效果
     */
    private void addGlowingEffectToBandit(Mob bandit) {
        try {
            MobEffectInstance glowingEffect = new MobEffectInstance(
                    MobEffects.GLOWING,
                    2400,
                    0,
                    false,
                    false,
                    true
            );
            bandit.addEffect(glowingEffect);
            LoggerUtil.debug("为劫匪添加发光效果: 实体ID={}, 类型={}",
                    bandit.getId(), BuiltInRegistries.ENTITY_TYPE.getKey(bandit.getType()));
        } catch (Exception e) {
            LoggerUtil.error("为劫匪添加发光效果时出错: 实体ID={}", bandit.getId(), e);
        }
    }

    /**
     * 为劫匪设置掉落物
     * 新增：生成流浪商人交易物品和玩家选择物品作为劫匪掉落物
     * 修改：为稀有快递添加稀有物品掉落
     * @param banditIds 劫匪ID列表
     * @param delivery 快递数据
     */
    private void setupBanditLoot(List<Integer> banditIds, DeliveryManager.DeliveryData delivery) {
        if (banditIds.isEmpty()) return;

        Random random = new Random();

        // 生成流浪商人交易物品
        List<ItemStack> traderItems = generateTraderItems(delivery, random);

        // 生成玩家选择物品
        List<ItemStack> playerItems = generatePlayerItems(delivery.selectedItem, random);

        // 为稀有快递添加稀有物品
        if (delivery.isRareDelivery) {
            ItemStack rareItem = generateRareItem();
            if (!rareItem.isEmpty()) {
                traderItems.add(rareItem);
                LoggerUtil.info("为稀有快递劫匪添加稀有物品: {}", rareItem.getDescriptionId());
            }
        }

        // 合并所有物品
        List<ItemStack> allLoot = new ArrayList<>();
        allLoot.addAll(traderItems);
        allLoot.addAll(playerItems);

        LoggerUtil.info("总掉落物生成: 交易物品={}, 玩家物品={}, 总计={}, 稀有快递={}",
                traderItems.size(), playerItems.size(), allLoot.size(), delivery.isRareDelivery);

        if (allLoot.isEmpty()) {
            LoggerUtil.warn("没有生成任何掉落物");
            return;
        }

        // 确保所有物品都被分配给劫匪
        distributeLootToBandits(banditIds, allLoot, random);
    }

    /**
     * 分配掉落物给劫匪
     * 修复：确保所有物品都被分配，而不是随机选择部分物品
     * @param banditIds 劫匪ID列表
     * @param allLoot 所有掉落物
     * @param random 随机数生成器
     */
    private void distributeLootToBandits(List<Integer> banditIds, List<ItemStack> allLoot, Random random) {
        Map<Integer, List<ItemStack>> banditLootAssignments = new HashMap<>();
        for (int banditId : banditIds) {
            banditLootAssignments.put(banditId, new ArrayList<>());
        }

        int currentBanditIndex = 0;
        for (ItemStack item : allLoot) {
            int banditId = banditIds.get(currentBanditIndex);
            banditLootAssignments.get(banditId).add(item.copy());

            currentBanditIndex = (currentBanditIndex + 1) % banditIds.size();
        }

        for (Map.Entry<Integer, List<ItemStack>> entry : banditLootAssignments.entrySet()) {
            banditLootMap.put(entry.getKey(), entry.getValue());
            LoggerUtil.info("劫匪掉落物分配: ID={}, 物品数量={}",
                    entry.getKey(), entry.getValue().size());
        }

        LoggerUtil.info("掉落物分配完成: 劫匪数量={}, 总物品数量={}",
                banditIds.size(), allLoot.size());
    }

    /**
     * 生成流浪商人交易物品
     * 修改：为稀有快递的流浪商人添加稀有物品交易
     * @param delivery 快递数据
     * @param random 随机数生成器
     * @return 交易物品列表
     */
    private List<ItemStack> generateTraderItems(DeliveryManager.DeliveryData delivery, Random random) {
        List<ItemStack> traderItems = new ArrayList<>();

        ServerLevel level = getPlayerLevel(delivery.playerId);
        if (level == null) {
            LoggerUtil.error("无法获取玩家世界，无法生成临时流浪商人: 玩家={}", delivery.playerId);
            return new ArrayList<>();
        }

        WanderingTrader tempTrader = new WanderingTrader(EntityType.WANDERING_TRADER, level);
        MerchantOffers offers = tempTrader.getOffers();

        if (offers != null && !offers.isEmpty()) {
            int types = 2 + random.nextInt(3);
            types = Math.min(types, offers.size());

            LoggerUtil.info("生成交易物品: 种类数量={}, 总交易数={}, 稀有快递={}",
                    types, offers.size(), delivery.isRareDelivery);

            Set<Integer> usedOffers = new HashSet<>();
            for (int i = 0; i < types; i++) {
                int offerIndex;
                do {
                    offerIndex = random.nextInt(offers.size());
                } while (usedOffers.contains(offerIndex) && usedOffers.size() < offers.size());
                usedOffers.add(offerIndex);

                MerchantOffer offer = offers.get(offerIndex);
                ItemStack tradeItem = offer.getResult().copy();

                if (!tradeItem.isEmpty()) {
                    int itemCount = 2 + random.nextInt(3);
                    tradeItem.setCount(itemCount);
                    traderItems.add(tradeItem);

                    LoggerUtil.debug("生成交易物品: {} x{}",
                            tradeItem.getDescriptionId(), itemCount);
                }
            }
        } else {
            LoggerUtil.warn("流浪商人没有交易物品");
        }

        tempTrader.discard();
        return traderItems;
    }

    /**
     * 向流浪商人添加交易物品
     * 修改：从TradePriceManager读取存储的交易价格信息
     * 新增：使用玩家存储的交易需求
     */
    private void addTradeItemToTrader(WanderingTrader trader, DeliveryManager.DeliveryData delivery) {
        MerchantOffers offers = trader.getOffers();

        // 从TradePriceManager获取存储的交易价格信息
        TradePriceManager.TradePriceData priceData =
                WanderingTraderExpressDelivery.tradePriceManager.getPlayerTradePrice(delivery.playerId);

        if (priceData != null) {
            // 使用存储的交易价格信息
            addTradeItemWithStoredPrice(trader, delivery, priceData);
        } else {
            // 回退方案：使用默认价格
            addTradeItemWithDefaultPrice(trader, delivery);
        }

        // 为稀有快递添加稀有物品交易
        if (delivery.isRareDelivery) {
            ItemStack rareItem = generateRareItem();
            if (!rareItem.isEmpty()) {
                net.minecraft.world.item.trading.ItemCost rareCost =
                        new net.minecraft.world.item.trading.ItemCost(Items.EMERALD, 64);
                MerchantOffer rareOffer = new MerchantOffer(rareCost, rareItem.copy(), 1, 100, 0.0f);
                offers.add(rareOffer);

                LoggerUtil.info("为稀有快递流浪商人添加稀有物品交易: {}",
                        rareItem.getDescriptionId());
            }
        }
    }

    /**
     * 使用存储的交易价格添加交易物品
     * 新增：从TradePriceManager读取玩家选择的交易需求
     * 修改：如果物品可堆叠，则增加随机范围（最大不超过原有的1.5倍，且不超过64）
     */
    private void addTradeItemWithStoredPrice(WanderingTrader trader, DeliveryManager.DeliveryData delivery,
                                             TradePriceManager.TradePriceData priceData) {
        MerchantOffers offers = trader.getOffers();

        // 修改：调整成本数量，如果可堆叠则增加随机范围
        int adjustedCostCount1 = adjustCostCount(priceData.costItem1, priceData.costCount1);
        net.minecraft.world.item.trading.ItemCost cost1 =
                new net.minecraft.world.item.trading.ItemCost(priceData.costItem1.getItem(), adjustedCostCount1);

        // 修改：调整第二个成本数量（如果有）
        int adjustedCostCount2 = priceData.hasSecondCost() ?
                adjustCostCount(priceData.costItem2, priceData.costCount2) : 0;

        net.minecraft.world.item.trading.ItemCost cost2 = null;
        if (priceData.hasSecondCost() && adjustedCostCount2 > 0) {
            cost2 = new net.minecraft.world.item.trading.ItemCost(priceData.costItem2.getItem(), adjustedCostCount2);
        }

        // 创建交易
        MerchantOffer offer;
        if (cost2 != null) {
            offer = new MerchantOffer(cost1, Optional.of(cost2), delivery.selectedItem.copy(),
                    priceData.maxUses, 10, 0.0f);
        } else {
            offer = new MerchantOffer(cost1, delivery.selectedItem.copy(),
                    priceData.maxUses, 10, 0.0f);
        }

        offers.add(offer);

        LoggerUtil.info("使用存储价格添加交易: 玩家={}, 物品={}, 成本1={}x{}->{}, 成本2={}x{}->{}, 次数={}",
                delivery.playerId, delivery.selectedItem.getDescriptionId(),
                priceData.costItem1.getDescriptionId(), priceData.costCount1, adjustedCostCount1,
                priceData.hasSecondCost() ? priceData.costItem2.getDescriptionId() : "无",
                priceData.hasSecondCost() ? priceData.costCount2 : 0,
                priceData.hasSecondCost() ? adjustedCostCount2 : 0,
                priceData.maxUses);
    }

    /**
     * 调整成本数量 - 如果物品可堆叠则增加随机范围
     * 新增：最大不超过原有的1.5倍，且不超过64
     * @param costItem 成本物品
     * @param originalCount 原始数量
     * @return 调整后的数量
     */
    private int adjustCostCount(ItemStack costItem, int originalCount) {
        if (costItem.isEmpty() || originalCount <= 0) {
            return originalCount;
        }

        // 检查物品是否可堆叠（最大堆叠数大于1）
        if (costItem.getMaxStackSize() > 1) {
            Random random = new Random();
            // 计算最大可调整数量（不超过原有的2倍，且不超过64）
            int maxAdjusted = Math.min((int) Math.ceil(originalCount * 2), 64);
            // 确保至少为原始数量
            maxAdjusted = Math.max(maxAdjusted, originalCount);

            // 在原始数量和最大调整数量之间随机
            int adjustedCount = originalCount + random.nextInt(maxAdjusted - originalCount + 1);

            LoggerUtil.debug("调整可堆叠成本数量: 物品={}, 原始={}, 调整后={}, 最大堆叠={}",
                    costItem.getDescriptionId(), originalCount, adjustedCount, costItem.getMaxStackSize());
            return adjustedCount;
        }

        // 不可堆叠物品保持原数量
        return originalCount;
    }

    /**
     * 使用默认价格添加交易物品
     * 保留原有逻辑作为回退方案
     * @param trader 流浪商人实例
     * @param delivery 快递数据
     */
    private void addTradeItemWithDefaultPrice(WanderingTrader trader, DeliveryManager.DeliveryData delivery) {
        MerchantOffers offers = trader.getOffers();
        Random random = new Random();

        int price = 4 + random.nextInt(7); // 普通物品4-10个绿宝石
        net.minecraft.world.item.trading.ItemCost cost =
                new net.minecraft.world.item.trading.ItemCost(Items.EMERALD, price);

        MerchantOffer offer = new MerchantOffer(cost, delivery.selectedItem.copy(), 5, 10, 0.0f);
        offers.add(offer);

        LoggerUtil.info("使用默认价格添加交易: 物品={}, 价格={}绿宝石, 稀有快递={}",
                delivery.selectedItem.getDescriptionId(), price, delivery.isRareDelivery);
    }

    /**
     * 生成玩家选择物品
     * 新增：生成2-5个玩家选择的物品
     * 修改：不可堆叠物品数量为1，可堆叠物品数量为2-5
     * @param selectedItem 玩家选择的物品
     * @param random 随机数生成器
     * @return 玩家物品列表
     */
    private List<ItemStack> generatePlayerItems(ItemStack selectedItem, Random random) {
        List<ItemStack> playerItems = new ArrayList<>();

        // 修改：根据物品是否可堆叠决定数量
        int count;
        if (selectedItem.getMaxStackSize() > 1) {
            // 可堆叠物品：2-5个
            count = 2 + random.nextInt(4);
        } else {
            // 不可堆叠物品：永远只有1个
            count = 1;
        }

        for (int i = 0; i < count; i++) {
            ItemStack copy = selectedItem.copy();
            // 修改：对于不可堆叠物品，每个堆叠数量为1；对于可堆叠物品，每个堆叠数量也是1（但会有多个堆叠）
            copy.setCount(1);
            playerItems.add(copy);
        }

        LoggerUtil.debug("生成玩家物品: {} x{} (可堆叠: {}, 最大堆叠: {})",
                selectedItem.getDescriptionId(), count,
                selectedItem.getMaxStackSize() > 1,
                selectedItem.getMaxStackSize());

        return playerItems;
    }

    /**
     * 获取劫匪掉落物
     */
    public List<ItemStack> getBanditLoot(int banditId) {
        List<ItemStack> loot = banditLootMap.get(banditId);
        if (loot != null) {
            banditLootMap.remove(banditId);
            LoggerUtil.info("获取劫匪掉落物: ID={}, 物品数量={}", banditId, loot.size());
        }
        return loot;
    }

    /**
     * 注册劫匪消失事件
     */
    private void registerBanditsDisappearance(UUID playerId, long currentTime, List<Integer> banditIds) {
        double disappearTimeMinutes = Config.getHalfDeliveryDisappearTime();
        long disappearTime = currentTime + Config.minutesToTicks(disappearTimeMinutes);

        for (Integer banditId : banditIds) {
            WanderingTraderExpressDelivery.pendingDisappearanceManager.addPendingDisappearance(
                    playerId, disappearTime, 5
            );
            WanderingTraderExpressDelivery.pendingDisappearanceManager.storeEntityData(
                    playerId, banditId, 5
            );
        }

        LoggerUtil.debug("注册劫匪消失事件: 玩家={}, 消失时间={} ticks ({}分钟), 劫匪数量={}",
                playerId, disappearTime, disappearTimeMinutes, banditIds.size());
    }

    /**
     * 发送到达消息
     */
    private void sendArrivalMessage(UUID playerId) {
        Player player = getPlayer(playerId);
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.wandering_trader_express_delivery.trader_arrived")
                            .withStyle(net.minecraft.ChatFormatting.GOLD),
                    false
            );
        }
    }

    /**
     * 生成稀有物品
     */
    private ItemStack generateRareItem() {
        return RareItemGenerator.generateRareItem();
    }

    /**
     * 获取劫匪掉落物Map（用于外部清理）
     */
    public Map<Integer, List<ItemStack>> getBanditLootMap() {
        return banditLootMap;
    }

    // 工具方法
    private Player getPlayer(UUID playerId) {
        return CommonUtils.getPlayer(playerId);
    }

    private ServerLevel getPlayerLevel(UUID playerId) {
        return CommonUtils.getPlayerLevel(playerId);
    }
}