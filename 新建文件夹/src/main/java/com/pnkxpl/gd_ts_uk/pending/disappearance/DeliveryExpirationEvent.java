package com.pnkxpl.gd_ts_uk.pending.disappearance;

import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.pending.PendingDisappearanceManager;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

/**
 * 类型2：快递过期事件处理器
 * 处理快递数据的过期和清理逻辑
 * 修复：解决ConcurrentModificationException，返回移除标记替代直接迭代器移除
 * 新增：使用国际化翻译键，支持多语言
 */
public class DeliveryExpirationEvent {

    /**
     * 处理类型2消失事件（快递过期）
     * 当快递过期时，移除玩家的快递数据和相关事件，并发送过期消息
     *
     * @param event 待处理消失事件，包含玩家ID和消失时间
     * @param manager 消失事件管理器，用于访问队列和数据
     * @param currentTime 当前世界时间（游戏刻），用于判断是否过期
     * @return true表示事件已处理需要移除，false表示事件未过期需要保留
     */
    public boolean handleEvent(PendingDisappearanceManager.PendingDisappearanceEvent event,
                               PendingDisappearanceManager manager, long currentTime) {
        // 检查事件是否已过期
        if (currentTime < event.disappearTime) {
            LoggerUtil.debug("快递事件未过期: 玩家={}, 剩余时间={}",
                    event.playerId, event.disappearTime - currentTime);
            return false;
        }

        // 事件已过期，处理快递移除
        LoggerUtil.info("处理快递过期事件: 玩家={}, 消失时间={}",
                event.playerId, event.disappearTime);

        // 移除玩家的快递数据
        WanderingTraderExpressDelivery.deliveryManager.removePlayerDelivery(event.playerId);

        // 发送过期消息给玩家
        Player player = manager.getPlayer(event.playerId);
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.wandering_trader_express_delivery.delivery_expired")
                            .withStyle(ChatFormatting.BLUE),
                    false
            );
        }

        LoggerUtil.info("快递过期处理完成: 玩家={}", event.playerId);

        // 返回true表示这个事件需要从队列中移除
        return true;
    }
}