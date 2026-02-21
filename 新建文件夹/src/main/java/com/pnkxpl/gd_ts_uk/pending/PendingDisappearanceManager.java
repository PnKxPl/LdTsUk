package com.pnkxpl.gd_ts_uk.pending;

import com.pnkxpl.gd_ts_uk.pending.disappearance.*;
import com.pnkxpl.gd_ts_uk.pending.disappearance.BanditExpirationEvent;
import com.pnkxpl.gd_ts_uk.pending.disappearance.DeliveryExpirationEvent;
import com.pnkxpl.gd_ts_uk.pending.disappearance.TradeListExpirationEvent;
import com.pnkxpl.gd_ts_uk.utils.CommonUtils;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 待处理消失事件管理器 - 统一管理所有类型的消失事件（类型1-3,5）
 * 负责事件的注册、处理、持久化和清理
 * 彻底修复：使用线程安全的CopyOnWriteArrayList和同步机制解决ConcurrentModificationException
 * 优化：简化代码结构，提高可读性和维护性
 * 修改：统一管理所有消失事件（类型1、2、5），移除分散的数据保存
 * 新增：详细日志输出用于问题检测
 */
public class PendingDisappearanceManager {
    // 消失事件队列：使用CopyOnWriteArrayList避免并发修改异常
    // 类型1：交易列表过期，类型2：快递过期，类型3：流浪商人过期，类型5：劫匪过期
    private final Map<Integer, CopyOnWriteArrayList<PendingDisappearanceEvent>> disappearanceQueues = new ConcurrentHashMap<>();

    // 实体数据存储：玩家ID -> 类型 -> 实体ID列表（用于类型3和5）
    private final Map<UUID, Map<Integer, List<Integer>>> playerEntityData = new ConcurrentHashMap<>();

    // 事件处理器实例：分别处理4种类型的消失事件
    private final TradeListExpirationEvent tradeListExpirationEvent = new TradeListExpirationEvent();
    private final DeliveryExpirationEvent deliveryExpirationEvent = new DeliveryExpirationEvent();
    private final BanditExpirationEvent banditExpirationEvent = new BanditExpirationEvent();

    /**
     * 构造函数 - 初始化所有队列
     */
    public PendingDisappearanceManager() {
        initializeQueues();
    }

    /**
     * 初始化所有消失事件队列（类型1-3,5）
     * 使用CopyOnWriteArrayList确保线程安全
     */
    private void initializeQueues() {
        // 类型1：交易列表过期
        disappearanceQueues.put(1, new CopyOnWriteArrayList<>());
        // 类型2：快递过期
        disappearanceQueues.put(2, new CopyOnWriteArrayList<>());
        // 类型3：流浪商人过期
        disappearanceQueues.put(3, new CopyOnWriteArrayList<>());
        // 类型5：劫匪过期
        disappearanceQueues.put(5, new CopyOnWriteArrayList<>());
    }

    /**
     * 待处理消失事件内部类
     */
    public static class PendingDisappearanceEvent {
        /** 玩家UUID，标识事件所属玩家 */
        public final UUID playerId;
        /** 消失时间（绝对时间，游戏刻） */
        public final long disappearTime;
        /** 事件类型（1-3,5），标识不同类型的事件 */
        public final int type;

        /**
         * 构造函数
         */
        public PendingDisappearanceEvent(UUID playerId, long disappearTime, int type) {
            this.playerId = playerId;
            this.disappearTime = disappearTime;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PendingDisappearanceEvent that = (PendingDisappearanceEvent) o;
            return disappearTime == that.disappearTime &&
                    type == that.type &&
                    Objects.equals(playerId, that.playerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playerId, disappearTime, type);
        }
    }

    // ==================== 核心业务方法 ====================

    /**
     * 添加待处理消失事件到对应类型的队列
     * 使用CopyOnWriteArrayList确保线程安全
     */
    public void addPendingDisappearance(UUID playerId, long disappearTime, int type) {
        if (type != 1 && type != 2 && type != 3 && type != 5) {
            LoggerUtil.warn("❌ 无效的消失事件类型: {}", type);
            return;
        }

        PendingDisappearanceEvent event = new PendingDisappearanceEvent(playerId, disappearTime, type);
        disappearanceQueues.get(type).add(event);

        LoggerUtil.debug("📝 添加消失事件: 类型={}, 玩家={}, 时间={}",
                type, playerId, disappearTime);
    }

    /**
     * 移除指定玩家和类型的所有待处理消失事件
     * 使用CopyOnWriteArrayList确保线程安全
     */
    public void removePendingDisappearance(UUID playerId, int type) {
        if (type != 1 && type != 2 && type != 3 && type != 5) return;

        CopyOnWriteArrayList<PendingDisappearanceEvent> queue = disappearanceQueues.get(type);
        int initialSize = queue.size();
        queue.removeIf(event -> event.playerId.equals(playerId));

        if (initialSize != queue.size()) {
            LoggerUtil.debug("🗑️ 移除消失事件: 类型={}, 玩家={}, 移除了{}个事件",
                    type, playerId, initialSize - queue.size());
        }
    }

    /**
     * 存储实体数据，将实体ID与玩家和事件类型关联
     */
    public void storeEntityData(UUID playerId, int entityId, int type) {
        if (type != 3 && type != 5) {
            LoggerUtil.warn("❌ 无效的实体数据类型: {}", type);
            return;
        }

        playerEntityData
                .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(type, k -> new ArrayList<>())
                .add(entityId);

        LoggerUtil.debug("💾 存储实体数据: 玩家={}, 类型={}, 实体ID={}",
                playerId, type, entityId);
    }

    /**
     * 处理所有待处理消失事件
     * 使用CopyOnWriteArrayList确保线程安全，避免ConcurrentModificationException
     */
    public void processAllEvents(long currentWorldTime) {
        // 处理类型1：交易列表过期
        processEventsByType(1, currentWorldTime);
        // 处理类型2：快递过期
        processEventsByType(2, currentWorldTime);
        // 处理类型3：流浪商人过期
        processEventsByType(3, currentWorldTime);
        // 处理类型5：劫匪过期
        processEventsByType(5, currentWorldTime);
    }

    /**
     * 处理指定类型的所有消失事件
     * 使用CopyOnWriteArrayList的迭代器，确保线程安全
     */
    private void processEventsByType(int type, long currentTime) {
        CopyOnWriteArrayList<PendingDisappearanceEvent> queue = disappearanceQueues.get(type);
        if (queue == null || queue.isEmpty()) return;

        List<PendingDisappearanceEvent> eventsToRemove = new ArrayList<>();

        for (PendingDisappearanceEvent event : queue) {
            if (currentTime >= event.disappearTime) {
                // 处理事件并检查是否需要移除
                boolean shouldRemove = handleEventByType(event, currentTime);
                if (shouldRemove) {
                    eventsToRemove.add(event);
                }
            }
        }

        // 统一移除所有标记的事件
        if (!eventsToRemove.isEmpty()) {
            queue.removeAll(eventsToRemove);
            LoggerUtil.debug("✅ 移除过期事件: 类型={}, 数量={}", type, eventsToRemove.size());
        }
    }

    /**
     * 根据事件类型分发给对应的处理器
     *
     * @param event 待处理事件
     * @param currentTime 当前时间
     * @return true表示事件需要移除，false表示事件需要保留
     */
    private boolean handleEventByType(PendingDisappearanceEvent event, long currentTime) {
        return switch (event.type) {
            case 1 -> tradeListExpirationEvent.handleEvent(event, this, currentTime);
            case 2 -> deliveryExpirationEvent.handleEvent(event, this, currentTime);
            case 5 -> banditExpirationEvent.handleEvent(event, this, currentTime);
            default -> false;
        };
    }

    // ==================== 数据持久化方法 ====================

    /**
     * 保存所有需要持久化的消失事件数据到文件
     * 修改：统一保存类型1、2、5的消失事件数据
     * 新增：详细日志输出用于问题检测
     */
    public void saveData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) {
                LoggerUtil.warn("❌ 无法获取主世界，跳过消失事件保存");
                return;
            }

            Path dataFile = CommonUtils.getModDataDir(overworld).resolve("pending_disappearance_events.dat");
            Files.createDirectories(dataFile.getParent());

            CompoundTag rootTag = new CompoundTag();
            long currentTime = getCurrentWorldTime();

            // 保存类型1（交易列表过期）数据
            int saved1 = saveTypeData(rootTag, 1, currentTime);

            // 保存类型2（快递过期）数据
            int saved2 = saveTypeData(rootTag, 2, currentTime);

            // 保存类型5（劫匪）数据
            int saved5 = saveTypeData(rootTag, 5, currentTime);

            net.minecraft.nbt.NbtIo.writeCompressed(rootTag, dataFile);

            LoggerUtil.info("✅ 消失事件数据保存成功: 类型1={}个, 类型2={}个, 类型5={}个, 总计={}个",
                    saved1, saved2, saved5, saved1 + saved2 + saved5);
        } catch (IOException e) {
            LoggerUtil.error("💥 保存消失事件数据失败", e);
        } catch (Exception e) {
            LoggerUtil.error("💥 保存消失事件数据时发生未知错误", e);
        }
    }

    /**
     * 保存指定类型的数据
     * 新增：通用类型数据保存方法
     */
    private int saveTypeData(CompoundTag rootTag, int type, long currentTime) {
        ListTag typeList = new ListTag();
        CopyOnWriteArrayList<PendingDisappearanceEvent> queue = disappearanceQueues.get(type);

        if (queue != null && !queue.isEmpty()) {
            for (PendingDisappearanceEvent event : queue) {
                CompoundTag eventTag = createEventTag(event, currentTime);
                typeList.add(eventTag);

                LoggerUtil.debug("💾 保存消失事件: 类型={}, 玩家={}, 剩余时间={} ticks",
                        type, event.playerId, event.disappearTime - currentTime);
            }
        }

        rootTag.put("type" + type + "Data", typeList);
        return typeList.size();
    }

    /**
     * 创建事件基础标签
     */
    private CompoundTag createEventTag(PendingDisappearanceEvent event, long currentTime) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("playerId", event.playerId);
        tag.putLong("relativeTime", event.disappearTime - currentTime);
        return tag;
    }

    /**
     * 从文件加载所有持久化的消失事件数据
     * 修改：加载类型1、2、5的消失事件数据
     * 新增：详细日志输出用于问题检测
     */
    public void loadData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) {
                LoggerUtil.warn("❌ 无法获取主世界，跳过消失事件加载");
                return;
            }

            Path dataFile = CommonUtils.getModDataDir(overworld).resolve("pending_disappearance_events.dat");

            if (!Files.exists(dataFile)) {
                LoggerUtil.info("📝 没有找到消失事件数据文件，跳过加载: {}", dataFile);
                return;
            }

            // 检查文件大小
            long fileSize = Files.size(dataFile);
            if (fileSize == 0) {
                LoggerUtil.warn("⚠️ 消失事件数据文件为空，跳过加载");
                return;
            }

            LoggerUtil.info("📥 开始加载消失事件数据: 文件大小={} bytes", fileSize);

            CompoundTag rootTag = net.minecraft.nbt.NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
            long currentTime = getCurrentWorldTime();

            int loaded1 = loadTypeData(rootTag, 1, currentTime);
            int loaded2 = loadTypeData(rootTag, 2, currentTime);
            int loaded5 = loadTypeData(rootTag, 5, currentTime);

            LoggerUtil.info("✅ 消失事件数据加载完成: 类型1={}个, 类型2={}个, 类型5={}个, 总计={}个",
                    loaded1, loaded2, loaded5, loaded1 + loaded2 + loaded5);
        } catch (IOException e) {
            LoggerUtil.error("💥 加载消失事件数据失败", e);
        } catch (Exception e) {
            LoggerUtil.error("💥 加载消失事件数据时发生未知错误", e);
        }
    }

    /**
     * 加载指定类型的数据
     */
    private int loadTypeData(CompoundTag rootTag, int type, long currentTime) {
        String tagName = "type" + type + "Data";
        if (!rootTag.contains(tagName, Tag.TAG_LIST)) {
            LoggerUtil.debug("📝 数据文件中没有找到{}列表", tagName);
            return 0;
        }

        ListTag typeList = rootTag.getList(tagName, Tag.TAG_COMPOUND);
        int loadedCount = 0;
        int expiredCount = 0;
        int errorCount = 0;

        for (int i = 0; i < typeList.size(); i++) {
            CompoundTag eventTag = typeList.getCompound(i);
            LoadResult result = loadSingleEvent(eventTag, type, currentTime);

            switch (result) {
                case SUCCESS -> loadedCount++;
                case EXPIRED -> expiredCount++;
                case ERROR -> errorCount++;
            }
        }

        LoggerUtil.debug("📊 消失事件类型{}加载统计: 成功={}, 过期={}, 错误={}",
                type, loadedCount, expiredCount, errorCount);
        return loadedCount;
    }

    /**
     * 加载单个事件
     */
    private LoadResult loadSingleEvent(CompoundTag eventTag, int expectedType, long currentTime) {
        try {
            UUID playerId = eventTag.getUUID("playerId");
            long relativeTime = eventTag.getLong("relativeTime");

            if (relativeTime <= 0) {
                LoggerUtil.debug("⏰ 跳过已过期的消失事件: 类型={}, 玩家={}", expectedType, playerId);
                return LoadResult.EXPIRED;
            }

            long absoluteTime = currentTime + relativeTime;
            addPendingDisappearance(playerId, absoluteTime, expectedType);

            LoggerUtil.debug("📥 成功加载消失事件: 类型={}, 玩家={}, 剩余时间={} ticks",
                    expectedType, playerId, relativeTime);

            return LoadResult.SUCCESS;
        } catch (Exception e) {
            LoggerUtil.error("💥 加载单个消失事件时出错: 类型={}", expectedType, e);
            return LoadResult.ERROR;
        }
    }

    /**
     * 加载结果枚举
     */
    private enum LoadResult {
        SUCCESS,    // 加载成功
        EXPIRED,    // 事件已过期
        ERROR       // 加载过程中出错
    }

    // ==================== 数据访问方法 ====================

    /**
     * 获取消失事件队列的不可修改副本
     * 避免外部代码直接修改内部队列
     */
    public Map<Integer, List<PendingDisappearanceEvent>> getDisappearanceQueues() {
        Map<Integer, List<PendingDisappearanceEvent>> copy = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, CopyOnWriteArrayList<PendingDisappearanceEvent>> entry : disappearanceQueues.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public Map<UUID, Map<Integer, List<Integer>>> getPlayerEntityData() {
        return Collections.unmodifiableMap(playerEntityData);
    }

    /**
     * 根据实体ID获取实体实例
     */
    public Entity getEntityById(int entityId, UUID playerId) {
        ServerLevel level = CommonUtils.getPlayerLevel(playerId);
        return (level != null) ? level.getEntity(entityId) : null;
    }

    // ==================== 工具方法 ====================

    public Player getPlayer(UUID playerId) {
        return CommonUtils.getPlayer(playerId);
    }

    public ServerLevel getPlayerLevel(UUID playerId) {
        return CommonUtils.getPlayerLevel(playerId);
    }

    public long getCurrentWorldTime() {
        return CommonUtils.getCurrentWorldTime();
    }

    // ==================== 数据清理方法 ====================

    /**
     * 清空特定的消失事件数据（类型1、2、5）
     */
    private void clearSpecificDisappearanceData() {
        disappearanceQueues.get(1).clear();
        disappearanceQueues.get(2).clear();
        disappearanceQueues.get(5).clear();

        // 清理劫匪实体数据
        for (Map<Integer, List<Integer>> typeMap : playerEntityData.values()) {
            typeMap.remove(5);
        }

        LoggerUtil.info("🗑️ 已清空特定消失事件数据");
    }

    /**
     * 清空所有非持久化数据
     */
    public void clearNonPersistentData() {
        // 清空类型3（流浪商人）数据，这些不需要持久化
        CopyOnWriteArrayList<PendingDisappearanceEvent> queue3 = disappearanceQueues.get(3);
        if (queue3 != null) {
            queue3.clear();
        }

        // 清空实体数据
        playerEntityData.clear();

        LoggerUtil.info("🗑️ 已清除非持久化消失事件数据");
    }

    /**
     * 清空所有数据
     */
    public void clearAllData() {
        LoggerUtil.info("🗑️ 清空消失事件管理器所有数据");

        // 清空所有队列
        for (CopyOnWriteArrayList<PendingDisappearanceEvent> queue : disappearanceQueues.values()) {
            queue.clear();
        }

        // 清空实体数据
        playerEntityData.clear();

        LoggerUtil.info("✅ 消失事件管理器所有数据已清空");
    }
}