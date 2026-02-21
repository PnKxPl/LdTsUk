package com.pnkxpl.gd_ts_uk.pending.disappearance;

import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.pending.PendingDisappearanceManager;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

/**
 * 类型5：劫匪过期事件处理器
 * 处理劫匪实体的过期和清理逻辑
 * 修复：解决ConcurrentModificationException，返回移除标记替代直接迭代器移除
 * 修复：取消计数器，改为在过期时间+5分钟后移除数据
 * 优化：移除冗余代码，添加实体死亡检查
 */
public class BanditExpirationEvent {

    // 超时时间：5分钟（转换为游戏刻）
    private static final long TIMEOUT_TICKS = 5 * 60 * 20;

    /**
     * 处理类型5消失事件（劫匪过期）
     * 当劫匪过期时，移除实体和相关数据
     *
     * @param event 待处理消失事件，包含玩家ID和消失时间
     * @param manager 消失事件管理器，用于访问队列和数据
     * @param currentTime 当前世界时间（游戏刻），用于判断是否过期
     * @return true表示事件已处理需要移除，false表示事件未过期需要保留
     */
    public boolean handleEvent(PendingDisappearanceManager.PendingDisappearanceEvent event,
                               PendingDisappearanceManager manager, long currentTime) {
        // 检查事件是否真的过期
        if (currentTime < event.disappearTime) {
            LoggerUtil.debug("劫匪事件未过期: 玩家={}, 剩余时间={}",
                    event.playerId, event.disappearTime - currentTime);
            return false;
        }

        var entityIds = manager.getPlayerEntityData()
                .getOrDefault(event.playerId, new java.util.HashMap<>())
                .getOrDefault(5, new ArrayList<>());

        boolean shouldRemoveEvent = false;
        long timeoutTime = event.disappearTime + TIMEOUT_TICKS;

        // 遍历所有关联的实体ID
        for (Integer entityId : new ArrayList<>(entityIds)) {
            Entity entity = manager.getEntityById(entityId, event.playerId);

            if (entity != null && entity.isAlive()) {
                // 判断该劫匪是否被加载
                if (entity.level().isLoaded(entity.blockPosition())) {
                    // 移除被加载的劫匪
                    LoggerUtil.info("移除过期劫匪: 玩家={}, 实体ID={}",
                            event.playerId, entityId);
                    // 新增：移除劫匪的掉落物数据
                    removeBanditLootData(entityId);
                    entity.discard();
                    entityIds.remove(entityId);
                    shouldRemoveEvent = true;
                } else {
                    // 如果没有被加载，检查是否超过超时时间
                    if (currentTime >= timeoutTime) {
                        LoggerUtil.info("劫匪超时未加载，移除数据: 玩家={}, 实体ID={}",
                                event.playerId, entityId);
                        // 新增：移除劫匪的掉落物数据
                        removeBanditLootData(entityId);
                        entityIds.remove(entityId);
                        shouldRemoveEvent = true;
                    }
                }
            } else {
                // 实体不存在或已死亡，从列表中移除
                entityIds.remove(entityId);
                shouldRemoveEvent = true;
                LoggerUtil.debug("劫匪不存在或已死亡，从列表中移除: 玩家={}, 实体ID={}",
                        event.playerId, entityId);
            }
        }

        // 如果实体列表为空，从数据结构中移除
        if (entityIds.isEmpty()) {
            manager.getPlayerEntityData().getOrDefault(event.playerId, new java.util.HashMap<>()).remove(5);
            shouldRemoveEvent = true;
        }

        LoggerUtil.info("劫匪过期事件处理完成: 玩家={}, 需要移除={}", event.playerId, shouldRemoveEvent);
        return shouldRemoveEvent;
    }
    /**
     * 移除劫匪的掉落物数据
     * 新增：从拦截管理器的banditLootMap中移除对应劫匪的掉落物数据
     * @param banditId 劫匪实体ID
     */
    private void removeBanditLootData(int banditId) {
        try {
            if (WanderingTraderExpressDelivery.interceptManager != null) {
                // 从banditLootMap中移除该劫匪的掉落物数据
                var removedLoot = WanderingTraderExpressDelivery.interceptManager.getBanditLootMap().remove(banditId);
                if (removedLoot != null) {
                    LoggerUtil.info("移除劫匪掉落物数据: 劫匪ID={}, 掉落物数量={}",
                            banditId, removedLoot.size());
                } else {
                    LoggerUtil.debug("劫匪没有掉落物数据: 劫匪ID={}", banditId);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("移除劫匪掉落物数据时出错: 劫匪ID={}", banditId, e);
        }
    }
}