package com.pnkxpl.gd_ts_uk.manager;

import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.utils.CommonUtils;
import com.pnkxpl.gd_ts_uk.delivery.DeliveryManager;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtAccounter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.pnkxpl.gd_ts_uk.utils.RareItemGenerator.generateRareItem;

/**
 * 交易管理器 - 处理交易物品列表生成和GUI
 * 优化：极致简化代码结构，去除所有冗余，提高可读性和维护性
 * 修正：确保交易列表冷却机制正确运行
 * 修改：移除消失时间保存，由 PendingDisappearanceManager 统一管理所有消失事件
 * 新增：在GUI中显示交易物品的需求信息（LORE）
 * 新增：存储交易价格信息到TradePriceManager
 * 修改：生成交易列表时直接将信息写入Lore，玩家选取时解析Lore，不使用NBT
 */
public class TradeManager {
    // 数据存储
    private final Map<UUID, TradeListData> playerTradeLists = new HashMap<>();
    private final Set<String> usedTradeListIds = new HashSet<>();

    // 常量配置
    private static final int GUI_SLOT_COUNT = 54;
    private static final int CLOCK_SLOT = 53;
    private static final int MAX_TRADE_ITEMS = CLOCK_SLOT; // 53个物品槽位

    public TradeManager() {}

    /**
     * 交易列表数据类 - 封装冷却时间计算逻辑
     */
    public static class TradeListData {
        public final List<ItemStack> tradeItems;
        public final long generationTime;   // 开始时间（绝对时间）
        public final long cooldownTicks;    // 冷却时长（相对时间）
        public final String tradeListId;    // 交易列表唯一ID

        public TradeListData(List<ItemStack> tradeItems, long generationTime, long cooldownTicks, String tradeListId) {
            this.tradeItems = tradeItems;
            this.generationTime = generationTime;
            this.cooldownTicks = cooldownTicks;
            this.tradeListId = tradeListId;
        }

        /** 计算过期时间 */
        public long getExpireTime() {
            return generationTime + cooldownTicks;
        }

        /** 获取剩余时间 */
        public long getRemainingTime(long currentTime) {
            return getExpireTime() - currentTime;
        }

        /** 获取相对消失时间（用于保存） */
        public long getRelativeDisappearTime(long currentTime) {
            return getRemainingTime(currentTime);
        }
    }

    // ==================== 主要业务方法 ====================

    /**
     * 打开交易选择GUI - 主入口方法
     */
    public void openTradeSelectionGUI(Player player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (hasActiveDelivery(playerId)) {
            sendPlayerMessage(player, "message.wandering_trader_express_delivery.active_delivery", net.minecraft.ChatFormatting.RED);
            return;
        }

        processTradeListForPlayer(playerId, player);
    }

    /**
     * 处理玩家的交易列表逻辑
     */
    private void processTradeListForPlayer(UUID playerId, Player player) {
        long currentTime = CommonUtils.getCurrentWorldTime();
        TradeListData tradeData = playerTradeLists.get(playerId);

        if (tradeData == null || isTradeListExpired(playerId, currentTime)) {
            generateNewTradeList(playerId, player, currentTime);
            tradeData = playerTradeLists.get(playerId);
        }

        if (tradeData != null) {
            openTradeGUI(player, tradeData, currentTime);
        } else {
            sendPlayerMessage(player, "message.wandering_trader_express_delivery.trade_list_failed", net.minecraft.ChatFormatting.RED);
        }
    }

    /**
     * 生成新的交易列表
     */
    private void generateNewTradeList(UUID playerId, Player player, long currentTime) {
        removePlayerTradeList(playerId); // 清理旧数据

        List<ItemStack> tradeItems = generateTradeList(player);
        long cooldownTicks = Config.minutesToTicks(Config.getHalfDeliveryDisappearTime());
        String tradeListId = generateUniqueTradeListId();

        TradeListData tradeData = new TradeListData(tradeItems, currentTime, cooldownTicks, tradeListId);
        playerTradeLists.put(playerId, tradeData);

        // 注册消失事件 - 类型1（交易列表过期）
        long expireTime = tradeData.getExpireTime();
        WanderingTraderExpressDelivery.pendingDisappearanceManager.addPendingDisappearance(playerId, expireTime, 1);

        LoggerUtil.info("生成新交易列表: 玩家={}, 物品={}, 冷却={} ticks",
                playerId, tradeItems.size(), cooldownTicks);
    }

    /**
     * 生成交易物品列表
     * 修改：为稀有快递添加稀有物品
     * 修改：为交易物品添加LORE显示需求信息
     */
    private List<ItemStack> generateTradeList(Player player) {
        if (player.level().isClientSide()) return Collections.emptyList();

        ServerLevel level = (ServerLevel) player.level();
        Set<String> uniqueItemIds = new HashSet<>();
        List<ItemStack> uniqueItems = new ArrayList<>();

        // 生成8个流浪商人获取交易物品
        for (int i = 0; i < 8; i++) {
            processTraderOffers(level, uniqueItemIds, uniqueItems);
        }

        // 为稀有快递添加稀有物品
        if (WanderingTraderExpressDelivery.deliveryManager.activeDeliveries.containsKey(player.getUUID())) {
            DeliveryManager.DeliveryData delivery = WanderingTraderExpressDelivery.deliveryManager.activeDeliveries.get(player.getUUID());
            if (delivery.isRareDelivery) {
                ItemStack rareItem = generateRareItem();
                if (!rareItem.isEmpty()) {
                    // 为稀有物品添加默认交易信息
                    uniqueItems.add(rareItem);
                    LoggerUtil.info("为稀有快递添加稀有交易物品: {}", rareItem.getItem().getDescriptionId());
                }
            }
        }

        // 按物品ID排序
        uniqueItems.sort(Comparator.comparing(stack -> stack.getItem().getDescriptionId()));

        LoggerUtil.debug("交易列表生成: {}个物品", uniqueItems.size());
        return uniqueItems;
    }

    /**
     * 处理单个商人的交易物品
     */
    private void processTraderOffers(ServerLevel level, Set<String> uniqueItemIds, List<ItemStack> uniqueItems) {
        WanderingTrader trader = new WanderingTrader(EntityType.WANDERING_TRADER, level);
        trader.setPos(0, 100, 0); // 使用固定位置，反正马上移除

        try {
            level.addFreshEntity(trader);
            extractUniqueOffers(trader, uniqueItemIds, uniqueItems);
        } finally {
            trader.discard();
        }
    }

    /**
     * 提取商人的唯一交易物品
     * 修改：为物品添加交易需求的LORE信息
     */
    private void extractUniqueOffers(WanderingTrader trader, Set<String> uniqueItemIds, List<ItemStack> uniqueItems) {
        MerchantOffers offers = trader.getOffers();
        if (offers == null) return;

        for (MerchantOffer offer : offers) {
            ItemStack result = offer.getResult();
            if (!result.isEmpty()) {
                String itemId = result.getItem().getDescriptionId();
                if (uniqueItemIds.add(itemId)) {
                    ItemStack displayStack = result.copy();
                    // 添加交易需求信息到LORE
                    addTradeRequirementsToLore(displayStack, offer);
                    uniqueItems.add(displayStack);
                }
            }
        }
    }

    /**
     * 添加交易需求信息到物品LORE
     * 严格按照以下格式：
     * 最低价格：（黄色）
     * 需求:{数量}x {物品名字} ({物品注册1ID})（绿色）
     * 和: {数量}x {物品名字} ({物品注册2ID})（绿色）
     * 最大交易: {次数}（蓝色）
     */
    private void addTradeRequirementsToLore(ItemStack stack, MerchantOffer offer) {
        List<net.minecraft.network.chat.Component> lorelist = new ArrayList<>();

        // 第一行：最低价格（黄色）
        lorelist.add(Component.translatable("gui.wandering_trader_express_delivery.minimum_price")
                .withStyle(net.minecraft.ChatFormatting.YELLOW));

        // 第一个交易需求
        ItemStack cost1 = offer.getBaseCostA();
        if (!cost1.isEmpty()) {
            String itemName = cost1.getHoverName().getString();
            String itemId = BuiltInRegistries.ITEM.getKey(cost1.getItem()).toString();
            String costText = cost1.getCount() + "x " + itemName + " (" + itemId + ")";
            lorelist.add(Component.translatable("gui.wandering_trader_express_delivery.trade_requirement", costText)
                    .withStyle(net.minecraft.ChatFormatting.GREEN));
        }

        // 第二个交易需求（如果有）
        ItemStack cost2 = offer.getCostB();
        if (cost2 != null && !cost2.isEmpty()) {
            String itemName = cost2.getHoverName().getString();
            String itemId = BuiltInRegistries.ITEM.getKey(cost2.getItem()).toString();
            String costText = cost2.getCount() + "x " + itemName + " (" + itemId + ")";
            lorelist.add(Component.translatable("gui.wandering_trader_express_delivery.trade_requirement_and", costText)
                    .withStyle(net.minecraft.ChatFormatting.GREEN));
        }

        // 最大交易次数（蓝色）
        lorelist.add(Component.translatable("gui.wandering_trader_express_delivery.trade_max_uses", offer.getMaxUses())
                .withStyle(net.minecraft.ChatFormatting.BLUE));

        // 设置LORE到物品
        if (!lorelist.isEmpty()) {
            net.minecraft.world.item.component.ItemLore lore = new net.minecraft.world.item.component.ItemLore(lorelist);
            stack.set(DataComponents.LORE, lore);
        }
    }

    /**
     * 打开交易GUI
     */
    private void openTradeGUI(Player player, TradeListData tradeData, long currentTime) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        try {
            SimpleContainer container = createTradeContainer(tradeData, currentTime);
            MenuProvider menuProvider = new SimpleMenuProvider(
                    (containerId, playerInventory, playerEntity) ->
                            new TradeMenu(containerId, playerInventory, container, player.getUUID(), this),
                    Component.translatable("gui.wandering_trader_express_delivery.trade_selection")
            );

            serverPlayer.openMenu(menuProvider);
            LoggerUtil.info("打开交易GUI: {}", player.getName().getString());
        } catch (Exception e) {
            LoggerUtil.error("打开交易GUI失败: {}", player.getName().getString(), e);
            sendPlayerMessage(player, "message.wandering_trader_express_delivery.trade_gui_failed", net.minecraft.ChatFormatting.RED);
        }
    }

    /**
     * 创建交易容器
     */
    private SimpleContainer createTradeContainer(TradeListData tradeData, long currentTime) {
        SimpleContainer container = new NonInteractiveContainer(GUI_SLOT_COUNT);

        // 添加交易物品
        addTradeItemsToContainer(container, tradeData.tradeItems);

        // 添加信息时钟
        container.setItem(CLOCK_SLOT, createInfoClock(tradeData, currentTime));

        return container;
    }

    /**
     * 添加交易物品到容器
     */
    private void addTradeItemsToContainer(SimpleContainer container, List<ItemStack> tradeItems) {
        for (int i = 0; i < Math.min(tradeItems.size(), MAX_TRADE_ITEMS); i++) {
            ItemStack stack = tradeItems.get(i).copy();
            stack.setCount(1);
            container.setItem(i, stack);
        }
    }

    /**
     * 创建信息时钟
     */
    private ItemStack createInfoClock(TradeListData tradeData, long currentTime) {
        ItemStack clock = new ItemStack(Items.CLOCK);
        long remainingTime = tradeData.getRemainingTime(currentTime);

        Component displayText = (remainingTime <= 0) ?
                Component.translatable("gui.wandering_trader_express_delivery.trade_list_ready") :
                createCooldownMessage(remainingTime);

        clock.set(DataComponents.CUSTOM_NAME, displayText);
        return clock;
    }

    /**
     * 创建冷却时间消息
     */
    private Component createCooldownMessage(long remainingTime) {
        long remainingSeconds = remainingTime / 20;
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        return Component.translatable("gui.wandering_trader_express_delivery.cooldown_remaining", minutes, seconds);
    }

    /**
     * 处理GUI物品点击
     * 修改：从Lore解析交易信息并存储
     * 修改：传递物品时不传递Lore
     */
    public void handleTradeItemClick(Player player, int slot, ItemStack clickedItem) {
        if (player.level().isClientSide() || slot == CLOCK_SLOT || clickedItem.isEmpty()) {
            return;
        }

        if (hasActiveDelivery(player.getUUID())) {
            sendPlayerMessage(player, "message.wandering_trader_express_delivery.active_delivery_new", net.minecraft.ChatFormatting.RED);
            return;
        }

        // 从Lore解析交易信息并存储
        parseAndStoreTradeInfoFromLore(player, clickedItem);

        // 创建不带Lore的物品副本用于快递
        ItemStack deliveryItem = createDeliveryItemWithoutLore(clickedItem);

        startDelivery(player, deliveryItem);
    }

    /**
     * 创建不带Lore的快递物品
     * 新增：移除物品的Lore信息，只保留基本属性
     */
    private ItemStack createDeliveryItemWithoutLore(ItemStack originalItem) {
        ItemStack deliveryItem = originalItem.copy();

        // 移除Lore组件
        deliveryItem.remove(DataComponents.LORE);

        // 可选：移除其他不需要的组件，如自定义名称等
        // deliveryItem.remove(DataComponents.CUSTOM_NAME);

        LoggerUtil.debug("创建不带Lore的快递物品: {} -> {}",
                originalItem.getDescriptionId(), deliveryItem.getDescriptionId());

        return deliveryItem;
    }

    /**
     * 从Lore解析交易信息并存储 - 通用解析方法，支持多语言
     */
    private void parseAndStoreTradeInfoFromLore(Player player, ItemStack clickedItem) {
        net.minecraft.world.item.component.ItemLore itemLore = clickedItem.get(DataComponents.LORE);
        List<net.minecraft.network.chat.Component> lore = itemLore != null ? itemLore.lines() : null;

        if (lore == null || lore.isEmpty()) {
            LoggerUtil.warn("物品没有Lore信息，使用默认价格: 玩家={}, 物品={}",
                    player.getUUID(), clickedItem.getDescriptionId());
            storeDefaultTradePrice(player, clickedItem);
            return;
        }

        ItemStack costItem1 = ItemStack.EMPTY;
        int costCount1 = 0;
        ItemStack costItem2 = ItemStack.EMPTY;
        int costCount2 = 0;
        int maxUses = 5;

        try {
            // 使用通用的解析方法，不依赖特定语言的关键词
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i).getString();

                // 检查是否是需求行（包含"x"和"("字符）
                if (line.contains("x") && line.contains("(") && line.contains(")")) {
                    Object[] result = parseUniversalCostLine(line);
                    if (result != null) {
                        if (costItem1.isEmpty()) {
                            costItem1 = (ItemStack) result[0];
                            costCount1 = (Integer) result[1];
                        } else {
                            costItem2 = (ItemStack) result[0];
                            costCount2 = (Integer) result[1];
                        }
                    }
                }
                // 检查是否是最大交易次数行（包含数字）
                else if (containsNumber(line)) {
                    maxUses = extractNumberFromLine(line);
                }
            }

        } catch (Exception e) {
            LoggerUtil.error("解析Lore失败，使用默认价格: 玩家={}, 物品={}",
                    player.getUUID(), clickedItem.getDescriptionId(), e);
            storeDefaultTradePrice(player, clickedItem);
            return;
        }

        // 验证解析结果
        if (costItem1.isEmpty() || costCount1 <= 0) {
            LoggerUtil.warn("解析交易需求失败，使用默认价格: 玩家={}, 物品={}",
                    player.getUUID(), clickedItem.getDescriptionId());
            storeDefaultTradePrice(player, clickedItem);
            return;
        }

        // 存储到TradePriceManager
        WanderingTraderExpressDelivery.tradePriceManager.storePlayerTradePrice(
                player.getUUID(), clickedItem,
                costItem1, costCount1,
                costItem2.isEmpty() ? null : costItem2, costCount2,
                maxUses,
                CommonUtils.getCurrentWorldTime()
        );

        LoggerUtil.info("从Lore解析并存储交易价格: 玩家={}, 物品={}, 成本1={}x{}, 成本2={}x{}, 次数={}",
                player.getUUID(), clickedItem.getDescriptionId(),
                costItem1.getDescriptionId(), costCount1,
                costItem2.isEmpty() ? "无" : costItem2.getDescriptionId(), costCount2,
                maxUses);
    }

    /**
     * 通用成本行解析方法 - 不依赖特定语言
     * 格式: {数量}x {物品名字} ({物品注册ID})
     */
    private Object[] parseUniversalCostLine(String line) {
        try {
            // 提取数量 - 找到第一个"x"字符
            int xIndex = line.indexOf('x');
            if (xIndex == -1) return null;

            // 提取x之前的数字
            String beforeX = line.substring(0, xIndex).trim();
            int count = extractFirstNumber(beforeX);
            if (count <= 0) return null;

            // 提取物品ID - 在括号中
            int startBracket = line.indexOf('(');
            int endBracket = line.indexOf(')');
            if (startBracket == -1 || endBracket == -1) return null;

            String itemId = line.substring(startBracket + 1, endBracket).trim();
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemId));
            if (item != null) {
                ItemStack costItem = new ItemStack(item, 1);
                return new Object[]{costItem, count};
            }
        } catch (Exception e) {
            LoggerUtil.error("解析通用成本行失败: {}", line, e);
        }
        return null;
    }

    /**
     * 从字符串中提取第一个数字
     */
    private int extractFirstNumber(String text) {
        try {
            // 移除所有非数字字符，只保留数字
            String numberOnly = text.replaceAll("[^0-9]", "");
            if (!numberOnly.isEmpty()) {
                return Integer.parseInt(numberOnly);
            }
        } catch (Exception e) {
            LoggerUtil.error("提取数字失败: {}", text, e);
        }
        return 0;
    }

    /**
     * 检查字符串是否包含数字
     */
    private boolean containsNumber(String text) {
        return text.matches(".*\\d+.*");
    }

    /**
     * 从行中提取数字（通用方法）
     */
    private int extractNumberFromLine(String line) {
        try {
            return extractFirstNumber(line);
        } catch (Exception e) {
            LoggerUtil.error("提取数字失败: {}", line, e);
            return 5; // 默认值
        }
    }

    /**
     * 存储默认交易价格
     */
    private void storeDefaultTradePrice(Player player, ItemStack selectedItem) {
        WanderingTraderExpressDelivery.tradePriceManager.storePlayerTradePrice(
                player.getUUID(), selectedItem,
                new ItemStack(Items.EMERALD, 5), 5, // 默认5个绿宝石
                null, 0, // 没有第二个需求
                5, // 默认5次交易
                CommonUtils.getCurrentWorldTime()
        );
    }

    /**
     * 开始快递
     */
    private void startDelivery(Player player, ItemStack selectedItem) {
        try {
            WanderingTraderExpressDelivery.deliveryManager.startDelivery(player, selectedItem);
            player.closeContainer();
        } catch (Exception e) {
            LoggerUtil.error("开始快递失败", e);
            sendPlayerMessage(player, "message.wandering_trader_express_delivery.delivery_failed", net.minecraft.ChatFormatting.RED);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 检查玩家是否有未过期的快递（包括活跃快递和冷却中的快递）
     * 修正：同时检查活跃快递和快递冷却状态
     */
    private boolean hasActiveDelivery(UUID playerId) {
        // 检查是否有活跃快递
        boolean hasActive = WanderingTraderExpressDelivery.deliveryManager.activeDeliveries.containsKey(playerId);

        // 检查是否有快递冷却（类型2消失事件）
        boolean hasCooling = hasDeliveryCooling(playerId);

        if (hasActive || hasCooling) {
            LoggerUtil.debug("玩家有活跃快递或处于冷却期: 玩家={}, 活跃={}, 冷却={}",
                    playerId, hasActive, hasCooling);
            return true;
        }

        return false;
    }

    /**
     * 检查玩家是否有快递冷却（类型2消失事件）
     * 新增：通过检查待消失事件管理器判断是否处于快递冷却期
     */
    private boolean hasDeliveryCooling(UUID playerId) {
        try {
            // 获取类型2（快递冷却）的消失事件队列
            var disappearanceQueues = WanderingTraderExpressDelivery.pendingDisappearanceManager.getDisappearanceQueues();
            var queue2 = disappearanceQueues.get(2);

            if (queue2 != null) {
                long currentTime = CommonUtils.getCurrentWorldTime();

                // 检查是否有该玩家的未过期类型2事件
                for (var event : queue2) {
                    if (event.playerId.equals(playerId) && currentTime < event.disappearTime) {
                        LoggerUtil.debug("玩家处于快递冷却期: 玩家={}, 剩余时间={}",
                                playerId, event.disappearTime - currentTime);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("检查快递冷却状态时出错: 玩家={}", playerId, e);
        }

        return false;
    }

    /**
     * 检查玩家是否有交易列表
     */
    public boolean hasTradeList(UUID playerId) {
        TradeListData tradeData = playerTradeLists.get(playerId);
        if (tradeData == null) return false;

        long currentTime = CommonUtils.getCurrentWorldTime();
        long remainingTime = tradeData.getRemainingTime(currentTime);

        if (remainingTime <= 0) {
            removePlayerTradeList(playerId);
            return false;
        }

        return true;
    }

    /**
     * 检查交易列表是否过期
     */
    private boolean isTradeListExpired(UUID playerId, long currentTime) {
        TradeListData tradeData = playerTradeLists.get(playerId);
        if (tradeData == null) return true;

        long remainingTime = tradeData.getRemainingTime(currentTime);
        boolean expired = remainingTime <= 0;

        if (expired) {
            LoggerUtil.info("交易列表过期: 玩家={}, ID={}", playerId, tradeData.tradeListId);
            removePlayerTradeList(playerId);
        }

        return expired;
    }

    /**
     * 生成唯一交易列表ID
     */
    private String generateUniqueTradeListId() {
        String tradeListId;
        do {
            tradeListId = UUID.randomUUID().toString();
        } while (usedTradeListIds.contains(tradeListId));

        usedTradeListIds.add(tradeListId);
        return tradeListId;
    }

    /**
     * 移除玩家的交易列表
     */
    public void removePlayerTradeList(UUID playerId) {
        TradeListData tradeData = playerTradeLists.get(playerId);
        if (tradeData == null) return;

        // 清理所有相关数据
        usedTradeListIds.remove(tradeData.tradeListId);
        playerTradeLists.remove(playerId);
        WanderingTraderExpressDelivery.pendingDisappearanceManager.removePendingDisappearance(playerId, 1);

        LoggerUtil.info("移除交易列表: 玩家={}, ID={}", playerId, tradeData.tradeListId);
    }

    /**
     * 发送玩家消息
     */
    private void sendPlayerMessage(Player player, String translationKey, net.minecraft.ChatFormatting color) {
        player.displayClientMessage(Component.translatable(translationKey).withStyle(color), false);
    }

    /**
     * 更新GUI时钟显示
     */
    public void updateClockDisplay(Player player, SimpleContainer container) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        TradeListData tradeData = playerTradeLists.get(playerId);

        if (tradeData != null) {
            long currentTime = player.level().getGameTime();
            container.setItem(CLOCK_SLOT, createInfoClock(tradeData, currentTime));
        }
    }

    // ==================== 数据持久化方法 ====================

    /**
     * 加载交易列表数据
     * 修改：不再从交易列表数据中加载消失时间
     * 新增：消失时间由 PendingDisappearanceManager 统一加载
     */
    public void loadData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) return;

            Path dataFile = getDataFilePath(overworld);
            if (!isValidDataFile(dataFile)) {
                clearTradeData();
                return;
            }

            CompoundTag rootTag = net.minecraft.nbt.NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
            ListTag playerList = rootTag.getList("playerTradeLists", Tag.TAG_COMPOUND);

            if (playerList.isEmpty()) {
                clearTradeData();
                return;
            }

            loadAllPlayerTradeLists(playerList, overworld.getGameTime(), overworld);
        } catch (IOException e) {
            LoggerUtil.error("加载交易列表数据失败", e);
        }
    }

    /**
     * 获取数据文件路径
     */
    private Path getDataFilePath(ServerLevel overworld) {
        return CommonUtils.getModDataDir(overworld).resolve("trade_lists.dat");
    }

    /**
     * 检查数据文件是否有效
     */
    private boolean isValidDataFile(Path dataFile) throws IOException {
        return Files.exists(dataFile) && Files.size(dataFile) > 0;
    }

    /**
     * 加载所有玩家交易列表
     * 修正：传递 ServerLevel 参数用于物品加载
     */
    private void loadAllPlayerTradeLists(ListTag playerList, long currentTime, ServerLevel level) {
        int success = 0, expired = 0, duplicate = 0;

        for (int i = 0; i < playerList.size(); i++) {
            LoadResult result = loadSinglePlayerTradeList(playerList.getCompound(i), currentTime, level);
            switch (result) {
                case SUCCESS -> success++;
                case EXPIRED -> expired++;
                case DUPLICATE -> duplicate++;
            }
        }

        LoggerUtil.info("交易列表加载: 成功={}, 过期={}, 重复={}", success, expired, duplicate);
    }

    /**
     * 加载单个玩家交易列表
     * 修改：不再从保存的数据中恢复消失时间
     * 新增：消失时间由 PendingDisappearanceManager 统一管理
     * 修正：使用正确的物品加载方法
     */
    private LoadResult loadSinglePlayerTradeList(CompoundTag playerTag, long currentTime, ServerLevel level) {
        UUID playerId = playerTag.getUUID("playerId");
        // 不再从交易列表数据中加载消失时间
        String tradeListId = playerTag.getString("tradeListId");

        if (usedTradeListIds.contains(tradeListId)) {
            LoggerUtil.debug("🔄 跳过重复的交易列表ID: {}", tradeListId);
            return LoadResult.DUPLICATE;
        }

        List<ItemStack> tradeItems = loadTradeItems(playerTag, level);

        // 注意：这里不再从保存的数据中恢复消失时间
        // 交易列表的消失时间应该由游戏逻辑重新计算或通过 PendingDisappearanceManager 恢复

        // 创建新的交易列表数据，使用默认的冷却时间
        long cooldownTicks = Config.minutesToTicks(Config.getHalfDeliveryDisappearTime());
        TradeListData tradeData = new TradeListData(tradeItems, currentTime, cooldownTicks, tradeListId);

        playerTradeLists.put(playerId, tradeData);
        usedTradeListIds.add(tradeListId);

        // 消失事件将由 PendingDisappearanceManager 统一加载和管理
        LoggerUtil.debug("📥 加载交易列表数据: 玩家={}, 交易列表ID={}, 物品数量={}",
                playerId, tradeListId, tradeItems.size());

        return LoadResult.SUCCESS;
    }

    /**
     * 加载交易物品
     * 修正：使用正确的物品解析方法
     */
    private List<ItemStack> loadTradeItems(CompoundTag playerTag, ServerLevel level) {
        List<ItemStack> items = new ArrayList<>();
        ListTag itemsTag = playerTag.getList("tradeItems", Tag.TAG_COMPOUND);

        for (int i = 0; i < itemsTag.size(); i++) {
            items.add(createItemStack(itemsTag.getCompound(i), level));
        }
        return items;
    }

    private ItemStack createItemStack(CompoundTag itemTag, ServerLevel level) {
        try {
            // 使用 ItemStack 的 parse 方法，传入 RegistryAccess
            Optional<ItemStack> stack = ItemStack.parse(level.registryAccess(), itemTag);
            if (stack.isPresent()) {
                LoggerUtil.debug("成功加载交易物品: {} x{}", stack.get().getDescriptionId(), stack.get().getCount());
                return stack.get();
            } else {
                LoggerUtil.error("物品堆栈解析失败: {}", itemTag);
                return new ItemStack(Items.STONE, 1);
            }
        } catch (Exception e) {
            LoggerUtil.error("加载交易物品失败: {}", itemTag, e);
            return new ItemStack(Items.STONE, 1);
        }
    }

    /**
     * 保存交易列表数据
     * 修改：不再保存消失时间
     * 新增：消失时间由 PendingDisappearanceManager 统一管理
     * 修正：使用正确的物品保存方法
     */
    public void saveData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) {
                LoggerUtil.error("无法获取主世界，跳过交易列表保存");
                return;
            }

            Path dataFile = getDataFilePath(overworld);
            LoggerUtil.info("交易列表保存路径: {}", dataFile);

            // 确保目录存在
            Files.createDirectories(dataFile.getParent());

            CompoundTag rootTag = new CompoundTag();
            ListTag saveData = createSaveData(overworld.getGameTime(), overworld);
            rootTag.put("playerTradeLists", saveData);

            LoggerUtil.info("开始写入交易列表数据: 文件大小={} bytes", saveData.toString().length());
            net.minecraft.nbt.NbtIo.writeCompressed(rootTag, dataFile);
            LoggerUtil.info("交易列表保存成功: {}个玩家", playerTradeLists.size());
        } catch (IOException e) {
            LoggerUtil.error("保存交易列表数据失败 - IO错误", e);
        } catch (Exception e) {
            LoggerUtil.error("保存交易列表数据时发生未知错误", e);
        }
    }

    /**
     * 创建保存数据
     * 修正：传递 ServerLevel 参数用于物品保存
     */
    private ListTag createSaveData(long currentTime, ServerLevel level) {
        ListTag playerList = new ListTag();

        for (Map.Entry<UUID, TradeListData> entry : playerTradeLists.entrySet()) {
            CompoundTag playerTag = createPlayerSaveTag(entry.getKey(), entry.getValue(), currentTime, level);
            if (playerTag != null) {
                playerList.add(playerTag);
            }
        }

        return playerList;
    }

    /**
     * 创建玩家保存标签
     * 修改：不再保存消失时间
     * 新增：消失时间由 PendingDisappearanceManager 统一管理
     * 修正：使用正确的物品保存方法
     */
    private CompoundTag createPlayerSaveTag(UUID playerId, TradeListData tradeData, long currentTime, ServerLevel level) {
        // 不再保存消失时间，因为现在由 PendingDisappearanceManager 统一管理
        // 只检查是否过期，过期的数据不保存
        long relativeTime = tradeData.getRelativeDisappearTime(currentTime);
        if (relativeTime <= 0) return null;

        CompoundTag tag = new CompoundTag();
        tag.putUUID("playerId", playerId);
        // 移除：不再保存消失时间 tag.putLong("relativeDisappearTime", relativeTime);
        tag.putString("tradeListId", tradeData.tradeListId);
        tag.put("tradeItems", createItemsSaveTag(tradeData.tradeItems, level));

        LoggerUtil.debug("💾 保存交易列表数据: 玩家={}, 交易列表ID={}, 物品数量={}",
                playerId, tradeData.tradeListId, tradeData.tradeItems.size());

        return tag;
    }

    private ListTag createItemsSaveTag(List<ItemStack> tradeItems, ServerLevel level) {
        ListTag itemsTag = new ListTag();

        for (int i = 0; i < tradeItems.size(); i++) {
            ItemStack stack = tradeItems.get(i);
            try {
                if (!stack.isEmpty()) {
                    // 使用 ItemStack 的 save 方法，传入 RegistryAccess
                    CompoundTag itemTag = (CompoundTag) stack.save(level.registryAccess());
                    itemsTag.add(itemTag);
                    LoggerUtil.debug("成功保存交易物品: {} - {} x{}", i, BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount());
                } else {
                    LoggerUtil.warn("跳过空物品堆栈: 索引={}", i);
                }
            } catch (Exception e) {
                LoggerUtil.error("保存交易物品失败: 索引={}, 物品={}", i, stack.getDescriptionId(), e);
            }
        }

        LoggerUtil.debug("交易物品保存完成: 总物品数={}, 成功保存={}", tradeItems.size(), itemsTag.size());
        return itemsTag;
    }

    /**
     * 清理交易数据
     */
    public void clearTradeData() {
        playerTradeLists.clear();
        usedTradeListIds.clear();
        LoggerUtil.info("清空交易列表数据");
    }

    /**
     * 清理损坏的数据文件
     */
    public void cleanupCorruptedData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) return;

            Path dataFile = getDataFilePath(overworld);
            if (!Files.exists(dataFile)) return;

            try {
                net.minecraft.nbt.NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
                LoggerUtil.info("数据文件验证成功");
            } catch (Exception e) {
                LoggerUtil.warn("数据文件损坏，删除重建");
                Files.deleteIfExists(dataFile);
                clearTradeData();
            }
        } catch (IOException e) {
            LoggerUtil.error("清理损坏数据失败", e);
        }
    }

    // ==================== 内部类和枚举 ====================

    /**
     * 非交互式容器 - 防止玩家移动物品
     */
    private static class NonInteractiveContainer extends SimpleContainer {
        public NonInteractiveContainer(int size) {
            super(size);
        }

        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }
        @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
        @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
    }

    /**
     * 加载结果枚举
     */
    private enum LoadResult { SUCCESS, EXPIRED, DUPLICATE }

    /**
     * 交易菜单类 - 防止物品转移
     */
    public static class TradeMenu extends ChestMenu {
        private final UUID playerId;
        private final TradeManager tradeManager;
        private final net.minecraft.world.entity.player.Inventory playerInventoryRef;
        private long lastUpdateTime = 0;
        private static final long UPDATE_INTERVAL = 20;

        public TradeMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory,
                         SimpleContainer container, UUID playerId, TradeManager tradeManager) {
            super(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
            this.playerId = playerId;
            this.tradeManager = tradeManager;
            this.playerInventoryRef = playerInventory;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
            if (slotId >= 0 && slotId < CLOCK_SLOT && !getSlot(slotId).getItem().isEmpty()) {
                if (clickType == net.minecraft.world.inventory.ClickType.PICKUP && button == 0) {
                    WanderingTraderExpressDelivery.tradeManager.handleTradeItemClick(player, slotId, getSlot(slotId).getItem());
                }
                return;
            }
            super.clicked(slotId, button, clickType, player);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
            return false;
        }

        @Override
        public boolean canDragTo(Slot slot) {
            return false;
        }

        @Override
        public void broadcastChanges() {
            super.broadcastChanges();
            updateClockDisplay();
        }

        /**
         * 更新时钟显示
         */
        private void updateClockDisplay() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime > UPDATE_INTERVAL * 50) {
                if (tradeManager != null && playerInventoryRef != null) {
                    Player player = playerInventoryRef.player;
                    if (player != null) {
                        tradeManager.updateClockDisplay(player, (SimpleContainer) getSlot(0).container);
                    }
                }
                lastUpdateTime = currentTime;
            }
        }
    }
}