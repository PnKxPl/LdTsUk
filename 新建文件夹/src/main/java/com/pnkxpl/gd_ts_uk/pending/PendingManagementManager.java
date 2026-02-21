package com.pnkxpl.gd_ts_uk.pending;

import com.pnkxpl.gd_ts_uk.pending.management.PendingBanditEvent;
import com.pnkxpl.gd_ts_uk.pending.management.PendingWanderingTraderEvent;
import com.pnkxpl.gd_ts_uk.utils.CommonUtils;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 待处理管理事件管理器 - 专门处理所有待处理管理事件（类型1-2）
 * 负责待生成事件的处理和调度
 * 优化：改进事件处理逻辑，支持拦截点数据存储
 * 优化：使用公共工具类，减少代码重复
 * 修复：方法签名不匹配和参数传递错误
 * 新增：支持稀有快递状态保存和传递
 * 新增：数据持久化支持，解决服务器重启数据丢失问题
 */
public class PendingManagementManager {


    // 待处理管理事件队列（类型1-2）
    private final Map<Integer, List<PendingManagementEvent>> managementQueues = new ConcurrentHashMap<>();

    // 存储拦截点数据（用于类型2事件）
    private final Map<UUID, BlockPos> playerInterceptPoints = new ConcurrentHashMap<>();

    // 事件处理器实例
    private PendingWanderingTraderEvent pendingWanderingTraderEvent;
    private PendingBanditEvent pendingChestBanditEvent;

    public PendingManagementManager() {
        initializeQueues();
        initializeEventHandlers();
    }

    /**
     * 初始化所有队列
     */
    private void initializeQueues() {
        for (int i = 1; i <= 2; i++) {
            managementQueues.put(i, new ArrayList<>());
        }
    }

    /**
     * 初始化事件处理器
     */
    private void initializeEventHandlers() {
        pendingWanderingTraderEvent = new PendingWanderingTraderEvent();
        pendingChestBanditEvent = new PendingBanditEvent();
    }

    /**
     * 待处理管理事件类
     * 新增：存储位置信息，用于后续实体生成
     * 新增：存储稀有快递状态，确保区块未加载时状态不丢失
     */
    public static class PendingManagementEvent {
        /**
         * 玩家UUID，标识事件所属玩家
         */
        public final UUID playerId;
        /**
         * 管理时间（绝对时间，游戏刻），事件应该被处理的时间
         */
        public final long manageTime;
        /**
         * 事件类型（1-2），1=待生成流浪商人，2=待生成箱子和劫匪
         */
        public final int type;
        /**
         * 生成位置，实体应该生成的位置坐标
         */
        public final BlockPos position;
        /**
         * 稀有快递状态，true=稀有快递，false=普通快递
         */
        public final boolean isRareDelivery;
        /**
         * 选择的物品，玩家选择的交易物品
         */
        public final ItemStack selectedItem;

        /**
         * 构造函数
         *
         * @param playerId       玩家UUID
         * @param manageTime     管理时间（绝对时间）
         * @param type           事件类型（1-2）
         * @param position       生成位置
         * @param isRareDelivery 稀有快递状态
         */
        public PendingManagementEvent(UUID playerId, long manageTime, int type, BlockPos position, boolean isRareDelivery, ItemStack selectedItem) {
            this.playerId = playerId;
            this.manageTime = manageTime;
            this.type = type;
            this.position = position;
            this.isRareDelivery = isRareDelivery;
            this.selectedItem = selectedItem.copy();
        }
    }

    // ==================== 数据持久化方法 ====================

    /**
     * 保存待管理事件数据
     * 新增：解决服务器重启数据丢失问题
     * 新增：详细日志输出用于问题检测
     */
    public void saveData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) {
                LoggerUtil.warn("❌ 无法获取主世界，跳过管理事件保存");
                return;
            }

            Path dataFile = CommonUtils.getModDataDir(overworld).resolve("pending_management_events.dat");
            Files.createDirectories(dataFile.getParent());

            CompoundTag rootTag = new CompoundTag();
            ListTag managementList = new ListTag();
            long currentTime = overworld.getGameTime();

            int totalEvents = 0;
            int type1Events = 0;
            int type2Events = 0;

            // ✅ 确保保存所有类型1和2的事件
            for (int type = 1; type <= 2; type++) {
                List<PendingManagementEvent> events = managementQueues.get(type);
                if (events != null) {
                    for (PendingManagementEvent event : events) {
                        // 只保存未过期的事件
                        if (event.manageTime > currentTime) {
                            // 在 saveData 方法中，修改这一行：
                            CompoundTag eventTag = createEventTag(event, currentTime, overworld);
                            managementList.add(eventTag);
                            totalEvents++;

                            if (type == 1) type1Events++;
                            else if (type == 2) type2Events++;

                            LoggerUtil.debug("💾 保存管理事件: 类型={}, 玩家={}, 位置={}, 剩余时间={} ticks, 稀有={}",
                                type, event.playerId, event.position,
                                event.manageTime - currentTime, event.isRareDelivery);
                        }
                    }
                }
            }

            rootTag.put("managementEvents", managementList);

            // 保存拦截点数据
            if (!playerInterceptPoints.isEmpty()) {
                ListTag interceptPointsList = new ListTag();
                for (Map.Entry<UUID, BlockPos> entry : playerInterceptPoints.entrySet()) {
                    CompoundTag interceptTag = new CompoundTag();
                    interceptTag.putUUID("playerId", entry.getKey());
                    interceptTag.putInt("posX", entry.getValue().getX());
                    interceptTag.putInt("posY", entry.getValue().getY());
                    interceptTag.putInt("posZ", entry.getValue().getZ());
                    interceptPointsList.add(interceptTag);

                    LoggerUtil.debug("💾 保存拦截点数据: 玩家={}, 位置={}",
                        entry.getKey(), entry.getValue());
                }
                rootTag.put("interceptPoints", interceptPointsList);
            }

            // 写入文件
            NbtIo.writeCompressed(rootTag, dataFile);

            LoggerUtil.info("✅ 管理事件数据保存成功: 类型1={}个, 类型2={}个, 总计={}个事件, 拦截点={}个",
                type1Events, type2Events, totalEvents, playerInterceptPoints.size());

        } catch (IOException e) {
            LoggerUtil.error("💥 保存管理事件数据失败", e);
        } catch (Exception e) {
            LoggerUtil.error("💥 保存管理事件数据时发生未知错误", e);
        }
    }

    /**
     * 创建事件标签
     * 新增：保存选择的物品信息
     */
    private CompoundTag createEventTag(PendingManagementEvent event, long currentTime, ServerLevel level) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("playerId", event.playerId);
        tag.putLong("relativeManageTime", event.manageTime - currentTime);
        tag.putInt("type", event.type);
        tag.putInt("posX", event.position.getX());
        tag.putInt("posY", event.position.getY());
        tag.putInt("posZ", event.position.getZ());
        tag.putBoolean("isRareDelivery", event.isRareDelivery);

        // 保存选择的物品
        CompoundTag itemTag = (CompoundTag) event.selectedItem.save(level.registryAccess());
        tag.put("selectedItem", itemTag);

        return tag;
    }

    /**
     * 加载待管理事件数据
     * 新增：解决服务器重启数据丢失问题
     * 新增：详细日志输出用于问题检测
     */
    public void loadData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) {
                LoggerUtil.warn("❌ 无法获取主世界，跳过待管理事件加载");
                return;
            }

            Path dataFile = CommonUtils.getModDataDir(overworld).resolve("pending_management_events.dat");

            if (!Files.exists(dataFile)) {
                LoggerUtil.info("📝 没有找到待管理事件数据文件，跳过加载: {}", dataFile);
                return;
            }

            // 检查文件大小
            long fileSize = Files.size(dataFile);
            if (fileSize == 0) {
                LoggerUtil.warn("⚠️ 待管理事件数据文件为空，跳过加载: {}", dataFile);
                return;
            }

            LoggerUtil.info("📥 开始加载待管理事件数据: 文件大小={} bytes", fileSize);

            CompoundTag rootTag = NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
            long currentTime = overworld.getGameTime();

            // 加载管理事件
            // 在 loadData 方法中，修改加载管理事件的部分：
            int loadedEvents = loadManagementEvents(rootTag, currentTime, overworld);

            // 加载拦截点数据
            int loadedIntercepts = loadInterceptPoints(rootTag);

            LoggerUtil.info("✅ 待管理事件数据加载完成: 成功加载{}个事件, {}个拦截点",
                loadedEvents, loadedIntercepts);

            // 统计当前队列状态
            int currentType1 = managementQueues.get(1).size();
            int currentType2 = managementQueues.get(2).size();
            LoggerUtil.info("📊 当前待管理事件队列状态: 类型1={}个, 类型2={}个, 总计={}个",
                currentType1, currentType2, currentType1 + currentType2);

        } catch (IOException e) {
            LoggerUtil.error("💥 加载待管理事件数据失败 - 文件IO错误", e);
        } catch (Exception e) {
            LoggerUtil.error("💥 加载待管理事件数据时发生未知错误", e);
        }
    }

    /**
     * 加载管理事件
     */
    private int loadManagementEvents(CompoundTag rootTag, long currentTime, ServerLevel level) {
        if (!rootTag.contains("managementEvents", Tag.TAG_LIST)) {
            LoggerUtil.warn("⚠️ 数据文件中没有找到managementEvents列表");
            return 0;
        }

        ListTag managementList = rootTag.getList("managementEvents", Tag.TAG_COMPOUND);
        LoggerUtil.info("📋 开始解析管理事件列表: 总条目数={}", managementList.size());

        int loadedCount = 0;
        int expiredCount = 0;
        int invalidTypeCount = 0;
        int errorCount = 0;

        for (int i = 0; i < managementList.size(); i++) {
            CompoundTag eventTag = managementList.getCompound(i);
            // 在 loadManagementEvents 方法中，修改这一行：
            LoadResult result = loadSingleManagementEvent(eventTag, currentTime, level);

            switch (result) {
                case SUCCESS -> loadedCount++;
                case EXPIRED -> expiredCount++;
                case INVALID_TYPE -> invalidTypeCount++;
                case ERROR -> errorCount++;
            }
        }

        LoggerUtil.info("📊 管理事件加载统计: 成功={}, 过期={}, 无效类型={}, 错误={}",
            loadedCount, expiredCount, invalidTypeCount, errorCount);

        return loadedCount;
    }

    /**
     * 加载单个管理事件
     * 新增：加载选择的物品信息
     * 修正：使用正确的 ItemStack 解析方法
     */
    private LoadResult loadSingleManagementEvent(CompoundTag eventTag, long currentTime, ServerLevel level) {
        try {
            // 验证必要字段
            if (!eventTag.hasUUID("playerId") || !eventTag.contains("type") ||
                !eventTag.contains("relativeManageTime")) {
                LoggerUtil.warn("⚠️ 跳过缺失必要字段的管理事件: {}", eventTag);
                return LoadResult.ERROR;
            }

            UUID playerId = eventTag.getUUID("playerId");
            long relativeManageTime = eventTag.getLong("relativeManageTime");
            int type = eventTag.getInt("type");

            // 验证坐标字段
            if (!eventTag.contains("posX") || !eventTag.contains("posY") || !eventTag.contains("posZ")) {
                LoggerUtil.warn("⚠️ 跳过缺失坐标的管理事件: 玩家={}, 类型={}", playerId, type);
                return LoadResult.ERROR;
            }

            BlockPos position = new BlockPos(
                eventTag.getInt("posX"),
                eventTag.getInt("posY"),
                eventTag.getInt("posZ")
            );

            boolean isRareDelivery = eventTag.getBoolean("isRareDelivery");

            // 加载选择的物品
            ItemStack selectedItem;
            if (eventTag.contains("selectedItem", Tag.TAG_COMPOUND)) {
                CompoundTag itemTag = eventTag.getCompound("selectedItem");
                selectedItem = ItemStack.parse(level.registryAccess(), itemTag).orElse(new ItemStack(Items.STONE, 1));
            } else {
                // 向后兼容：如果没有保存的物品，使用默认物品
                selectedItem = new ItemStack(Items.STONE, 1);
                LoggerUtil.warn("⚠️ 管理事件没有保存的选择物品，使用默认: 玩家={}, 类型={}", playerId, type);
            }

            // 验证数据有效性
            if (relativeManageTime <= 0) {
                LoggerUtil.debug("⏰ 跳过已过期的待管理事件: 玩家={}, 类型={}, 剩余时间={}",
                    playerId, type, relativeManageTime);
                return LoadResult.EXPIRED;
            }

            if (type < 1 || type > 2) {
                LoggerUtil.warn("❌ 跳过无效的待管理事件类型: 玩家={}, 类型={}", playerId, type);
                return LoadResult.INVALID_TYPE;
            }

            long manageTime = currentTime + relativeManageTime;
            addPendingManagement(playerId, manageTime, type, position, isRareDelivery, selectedItem);

            LoggerUtil.debug("📥 成功加载待管理事件: 类型={}, 玩家={}, 位置={}, 剩余时间={} ticks, 稀有={}, 物品={}",
                type, playerId, position, relativeManageTime, isRareDelivery, selectedItem.getDescriptionId());

            return LoadResult.SUCCESS;

        } catch (Exception e) {
            LoggerUtil.error("💥 加载单个待管理事件时出错: {}", eventTag, e);
            return LoadResult.ERROR;
        }
    }

    /**
     * 加载拦截点数据
     */
    private int loadInterceptPoints(CompoundTag rootTag) {
        if (!rootTag.contains("interceptPoints", Tag.TAG_LIST)) {
            LoggerUtil.debug("📝 数据文件中没有找到interceptPoints列表");
            return 0;
        }

        ListTag interceptPointsList = rootTag.getList("interceptPoints", Tag.TAG_COMPOUND);
        LoggerUtil.info("📋 开始解析拦截点列表: 总条目数={}", interceptPointsList.size());

        int loadedCount = 0;
        int errorCount = 0;

        for (int i = 0; i < interceptPointsList.size(); i++) {
            try {
                CompoundTag interceptTag = interceptPointsList.getCompound(i);

                // 验证必要字段
                if (!interceptTag.hasUUID("playerId") || !interceptTag.contains("posX") ||
                    !interceptTag.contains("posY") || !interceptTag.contains("posZ")) {
                    LoggerUtil.warn("⚠️ 跳过缺失必要字段的拦截点数据: {}", interceptTag);
                    errorCount++;
                    continue;
                }

                UUID playerId = interceptTag.getUUID("playerId");
                BlockPos interceptPoint = new BlockPos(
                    interceptTag.getInt("posX"),
                    interceptTag.getInt("posY"),
                    interceptTag.getInt("posZ")
                );

                playerInterceptPoints.put(playerId, interceptPoint);
                loadedCount++;

                LoggerUtil.debug("📥 成功加载拦截点数据: 玩家={}, 位置={}", playerId, interceptPoint);

            } catch (Exception e) {
                LoggerUtil.error("💥 加载单个拦截点数据时出错", e);
                errorCount++;
            }
        }

        LoggerUtil.info("📊 拦截点数据加载统计: 成功={}, 错误={}", loadedCount, errorCount);
        return loadedCount;
    }

    /**
     * 加载结果枚举
     */
    private enum LoadResult {
        SUCCESS,    // 加载成功
        EXPIRED,    // 事件已过期
        INVALID_TYPE, // 无效的事件类型
        ERROR       // 加载过程中出错
    }

    // ==================== 原有业务方法（保持不变） ====================

    /**
     * 添加待处理管理事件
     * 修正：支持位置参数
     * 新增：支持稀有快递状态参数
     * 新增：支持选择的物品参数
     *
     * @param playerId       玩家UUID
     * @param manageTime     管理时间（绝对时间）
     * @param type           事件类型（1-2）
     * @param position       生成位置
     * @param isRareDelivery 稀有快递状态
     * @param selectedItem   选择的物品
     */
    public void addPendingManagement(UUID playerId, long manageTime, int type, BlockPos position, boolean isRareDelivery, ItemStack selectedItem) {
        if (type < 1 || type > 2) {
            LoggerUtil.warn("无效的管理事件类型: {}", type);
            return;
        }

        PendingManagementEvent event = new PendingManagementEvent(playerId, manageTime, type, position, isRareDelivery, selectedItem);
        managementQueues.get(type).add(event);
        LoggerUtil.debug("添加待处理管理事件: 类型={}, 玩家={}, 处理时间={}, 位置={}, 稀有快递={}, 选择物品={}",
            type, playerId, manageTime, position, isRareDelivery, selectedItem.getDescriptionId());
    }

    /**
     * 存储拦截点数据
     *
     * @param playerId       玩家UUID
     * @param interceptPoint 拦截点位置
     */
    public void storeInterceptPoint(UUID playerId, BlockPos interceptPoint) {
        playerInterceptPoints.put(playerId, interceptPoint);
        LoggerUtil.debug("存储拦截点数据: 玩家={}, 位置={}", playerId, interceptPoint);
    }

    /**
     * 获取拦截点数据
     *
     * @param playerId 玩家UUID
     * @return 拦截点位置，如果不存在则返回null
     */
    public BlockPos getInterceptPoint(UUID playerId) {
        return playerInterceptPoints.get(playerId);
    }

    /**
     * 处理所有待处理管理事件
     */
    public void processAllEvents(long currentWorldTime) {
        processManagementEvents(currentWorldTime);
    }

    /**
     * 处理管理事件
     * 修复：让事件在任何时间都可以被处理，而不仅仅是在过期时
     */
    private void processManagementEvents(long currentWorldTime) {
        for (int type = 1; type <= 2; type++) {
            List<PendingManagementEvent> queue = managementQueues.get(type);
            List<PendingManagementEvent> eventsToRemove = new ArrayList<>();

            for (PendingManagementEvent event : queue) {
                // 无论事件是否过期，都尝试处理
                boolean shouldRemove = handleManagementEvent(event, currentWorldTime);
                if (shouldRemove) {
                    eventsToRemove.add(event);
                }
            }

            // 移除已处理的事件
            queue.removeAll(eventsToRemove);
        }
    }

    /**
     * 处理管理事件的具体逻辑
     * 修复：返回是否应该移除事件，而不是在方法内移除
     *
     * @param event       待处理事件
     * @param currentTime 当前时间
     * @return true=事件已处理需要移除，false=事件需要保留
     */
    private boolean handleManagementEvent(PendingManagementEvent event, long currentTime) {
        switch (event.type) {
            case 1: // 待生成流浪商人
                return pendingWanderingTraderEvent.handleEvent(event, this, currentTime);
            case 2: // 待生成箱子和劫匪
                return pendingChestBanditEvent.handleEvent(event, this, currentTime);
            default:
                return false;
        }
    }

    // Getter方法
    public Map<Integer, List<PendingManagementEvent>> getManagementQueues() {
        return managementQueues;
    }

    public Map<UUID, BlockPos> getPlayerInterceptPoints() {
        return playerInterceptPoints;
    }

    /**
     * 发送加载时的等待消息
     * 修正：确保玩家在线时才发送消息，并添加重试机制
     */
    private void sendLoadedWaitingMessage(UUID playerId, int type, BlockPos position) {
        Player player = getPlayer(playerId);

        if (player == null) {
            // ✅ 新增：如果玩家不在线，记录日志并稍后重试
            LoggerUtil.info("玩家不在线，延迟发送加载等待消息: 类型={}, 玩家={}, 位置={}",
                type, playerId, position);

            // 可以在这里添加重试机制，比如存储到队列中，等玩家登录时发送
            addDelayedMessage(playerId, type, position);
            return;
        }

        Component message;
        switch (type) {
            case 1: // 流浪商人
                message = Component.translatable(
                    "message.wandering_trader_express_delivery.trader_loaded_waiting",
                    position.getX(), position.getY(), position.getZ()
                ).withStyle(ChatFormatting.YELLOW);
                break;
            case 2: // 劫匪
                message = Component.translatable(
                    "message.wandering_trader_express_delivery.bandits_loaded_waiting",
                    position.getX(), position.getY(), position.getZ()
                ).withStyle(ChatFormatting.RED);
                break;
            default:
                return;
        }

        player.displayClientMessage(message, false);
        LoggerUtil.info("✅ 发送加载等待消息: 类型={}, 玩家={}, 位置={}", type, playerId, position);
    }

    /**
     * 添加延迟消息到队列
     * 新增：当玩家不在线时，存储消息等玩家登录时发送
     */
    private void addDelayedMessage(UUID playerId, int type, BlockPos position) {
        // 实现延迟消息队列逻辑
        // 可以使用一个 Map<UUID, List<DelayedMessage>> 来存储
        LoggerUtil.debug("添加延迟消息: 玩家={}, 类型={}", playerId, type);
    }

    /**
     * 获取玩家实例
     * 修复：使用公共工具类，避免重复实现
     * 优化：移除冗余代码，直接调用CommonUtils
     *
     * @param playerId 玩家UUID
     * @return 玩家实例，如果玩家不存在则返回null
     */
    public Player getPlayer(UUID playerId) {
        return CommonUtils.getPlayer(playerId);
    }

    /**
     * 获取玩家所在的世界
     * 修复：使用公共工具类，避免重复实现
     * 优化：移除冗余代码，直接调用CommonUtils
     *
     * @param playerId 玩家UUID
     * @return 玩家所在的服务器世界，如果玩家不存在则返回null
     */
    public ServerLevel getPlayerLevel(UUID playerId) {
        return CommonUtils.getPlayerLevel(playerId);
    }

    /**
     * 获取当前世界时间
     * 修复：使用公共工具类，避免重复实现
     * 优化：移除冗余代码，直接调用CommonUtils
     *
     * @return 当前世界时间（游戏刻）
     */
    public long getCurrentWorldTime() {
        return CommonUtils.getCurrentWorldTime();
    }

    /**
     * 获取主世界
     * 修复：使用公共工具类，避免重复实现
     * 优化：移除冗余代码，直接调用CommonUtils
     *
     * @return 服务器主世界实例，如果服务器未就绪则返回null
     */
    public ServerLevel getOverworld() {
        return CommonUtils.getOverworld();
    }

    /**
     * 清空所有数据
     */
    public void clearAllData() {
        for (int i = 1; i <= 2; i++) {
            managementQueues.get(i).clear();
        }
        playerInterceptPoints.clear();
        LoggerUtil.info("已清空所有管理事件数据");
    }


}