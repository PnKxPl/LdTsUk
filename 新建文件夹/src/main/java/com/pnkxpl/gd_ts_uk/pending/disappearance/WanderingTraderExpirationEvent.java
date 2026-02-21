package com.pnkxpl.gd_ts_uk.pending.disappearance;

import com.pnkxpl.gd_ts_uk.pending.PendingDisappearanceManager;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 类型3：流浪商人过期事件处理器
 * 处理流浪商人实体的过期和清理逻辑
 * 修复：解决ConcurrentModificationException，返回移除标记替代直接迭代器移除
 * 修复：符合要求的5分钟超时机制，正确发送离开消息
 * 优化：移除冗余代码，添加实体死亡检查
 * 新增：使用国际化翻译键，支持多语言
 */
public class WanderingTraderExpirationEvent {

    // 超时时间：5分钟（转换为游戏刻）
    private static final long TIMEOUT_TICKS = 5 * 60 * 20;

    /**
     * 处理类型3消失事件（流浪商人过期）
     * 当流浪商人过期时，移除实体并发送离开消息
     *
     * @param event 待处理消失事件，包含玩家ID和消失时间
     * @param manager 消失事件管理器，用于访问队列和数据
     * @param currentTime 当前世界时间（游戏刻），用于判断是否过期
     * @return true表示事件已处理需要移除，false表示事件未过期需要保留
     */
    public boolean handleEvent(PendingDisappearanceManager.PendingDisappearanceEvent event,
                               PendingDisappearanceManager manager, long currentTime) {
        // 判断消失列表3是否为空
        var queue3 = manager.getDisappearanceQueues().get(3);
        if (queue3.isEmpty()) {
            return false;
        }

        // 检查事件是否过期
        if (currentTime < event.disappearTime) {
            LoggerUtil.debug("流浪商人事件未过期: 玩家={}, 剩余时间={}",
                    event.playerId, event.disappearTime - currentTime);
            return false;
        }

        // 处理过期的流浪商人
        return handleExpiredTrader(event, manager, currentTime);
    }

    /**
     * 处理过期的流浪商人
     *
     * @param event 过期事件
     * @param manager 事件管理器
     * @param currentTime 当前时间
     * @return true表示事件需要移除，false表示需要保留
     */
    private boolean handleExpiredTrader(PendingDisappearanceManager.PendingDisappearanceEvent event,
                                        PendingDisappearanceManager manager, long currentTime) {
        var entityIds = manager.getPlayerEntityData()
                .getOrDefault(event.playerId, new java.util.HashMap<>())
                .getOrDefault(3, new ArrayList<>());

        boolean shouldRemoveEvent = false;
        long timeoutTime = event.disappearTime + TIMEOUT_TICKS;

        // 遍历所有关联的实体ID
        for (Integer entityId : new ArrayList<>(entityIds)) {
            Entity entity = manager.getEntityById(entityId, event.playerId);

            if (entity != null && entity.isAlive()) {
                // 实体存活且被加载
                if (entity.level().isLoaded(entity.blockPosition())) {
                    removeTrader(entity, event.playerId, manager);
                    entityIds.remove(entityId);
                    shouldRemoveEvent = true;
                } else {
                    // 实体未加载，检查超时
                    if (currentTime >= timeoutTime) {
                        LoggerUtil.info("流浪商人超时未加载，移除数据: 玩家={}, 实体ID={}",
                                event.playerId, entityId);
                        entityIds.remove(entityId);
                        shouldRemoveEvent = true;
                    }
                }
            } else {
                // 实体不存在或已死亡，从列表中移除
                entityIds.remove(entityId);
                shouldRemoveEvent = true;
                LoggerUtil.debug("流浪商人不存在或已死亡，从列表中移除: 玩家={}, 实体ID={}",
                        event.playerId, entityId);
            }
        }

        // 清理空列表
        if (entityIds.isEmpty()) {
            manager.getPlayerEntityData().getOrDefault(event.playerId, new java.util.HashMap<>()).remove(3);
            shouldRemoveEvent = true;
        }

        return shouldRemoveEvent;
    }

    /**
     * 移除流浪商人并发送消息
     */
    private void removeTrader(Entity entity, UUID playerId, PendingDisappearanceManager manager) {
        LoggerUtil.info("移除过期流浪商人: 玩家={}, 实体ID={}", playerId, entity.getId());

        // 移除实体
        entity.discard();

        // 发送离开消息 - 使用国际化翻译键
        Player player = manager.getPlayer(playerId);
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.wandering_trader_express_delivery.trader_left")
                            .withStyle(net.minecraft.ChatFormatting.RED),
                    false
            );
        }

        LoggerUtil.info("流浪商人已移除并发送离开消息: 玩家={}", playerId);
    }
}