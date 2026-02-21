package com.pnkxpl.gd_ts_uk.pending.disappearance;

import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.pending.PendingDisappearanceManager;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;

/**
 * 类型1：交易列表过期事件处理器
 * 专门处理交易列表冷却时间的过期和清理逻辑
 * 修复：解决ConcurrentModificationException，使用标记移除方式替代直接迭代器移除
 * 优化：简化代码，移除所有冗余检查，只保留核心过期处理逻辑
 */
public class TradeListExpirationEvent {

    /**
     * 处理类型1消失事件（交易列表过期）
     * 当交易列表冷却时间到期时，移除玩家的交易列表数据和相关事件
     * 修复：不再直接使用迭代器移除，而是返回移除标记，由调用方统一处理
     *
     * @param event 待处理消失事件，包含玩家ID和消失时间
     * @param manager 消失事件管理器，用于访问队列和数据
     * @param currentTime 当前世界时间（游戏刻），用于判断是否过期
     * @return true表示事件已处理需要移除，false表示事件未过期需要保留
     */
    public boolean handleEvent(PendingDisappearanceManager.PendingDisappearanceEvent event,
                               PendingDisappearanceManager manager, long currentTime) {
        // 检查事件是否已过期（当前时间 >= 消失时间）
        if (currentTime < event.disappearTime) {
            // 事件未过期，跳过处理
            LoggerUtil.debug("交易列表事件未过期: 玩家={}, 剩余时间={}",
                    event.playerId, event.disappearTime - currentTime);
            return false;
        }

        // 事件已过期，处理交易列表移除
        LoggerUtil.info("处理交易列表过期事件: 玩家={}, 消失时间={}",
                event.playerId, event.disappearTime);

        // 调用交易管理器移除玩家的交易列表（会自动清理所有相关数据）
        WanderingTraderExpressDelivery.tradeManager.removePlayerTradeList(event.playerId);

        LoggerUtil.info("交易列表过期处理完成: 玩家={}", event.playerId);

        // 返回true表示这个事件需要从队列中移除
        return true;
    }
}