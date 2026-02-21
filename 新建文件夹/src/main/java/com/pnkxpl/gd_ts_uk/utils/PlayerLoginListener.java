package com.pnkxpl.gd_ts_uk.utils;

import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.UUID;

/**
 * 玩家登录事件监听器
 * 负责在玩家登录时发送延迟的等待消息
 */
public class PlayerLoginListener {

    /**
     * 处理玩家登录事件
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        LoggerUtil.info("玩家登录: {}", player.getName().getString());

        // 发送该玩家的所有等待消息
        sendPendingMessages(player);

        // 检查并发送管理事件等待消息
        sendManagementEventMessages(player);
    }

    /**
     * 发送管理事件等待消息
     */
    private void sendManagementEventMessages(Player player) {
        UUID playerId = player.getUUID();

        // 检查类型1事件（流浪商人）
        var type1Events = WanderingTraderExpressDelivery.pendingManagementManager.getManagementQueues().get(1);
        if (type1Events != null) {
            for (var event : type1Events) {
                if (event.playerId.equals(playerId)) {
                    Component message = Component.translatable(
                            "message.wandering_trader_express_delivery.trader_loaded_waiting",
                            event.position.getX(), event.position.getY(), event.position.getZ()
                    ).withStyle(net.minecraft.ChatFormatting.YELLOW);

                    player.displayClientMessage(message, false);
                    LoggerUtil.info("✅ 登录时发送流浪商人等待消息: 玩家={}, 位置={}",
                            playerId, event.position);
                }
            }
        }

        // 检查类型2事件（劫匪）
        var type2Events = WanderingTraderExpressDelivery.pendingManagementManager.getManagementQueues().get(2);
        if (type2Events != null) {
            for (var event : type2Events) {
                if (event.playerId.equals(playerId)) {
                    Component message = Component.translatable(
                            "message.wandering_trader_express_delivery.bandits_loaded_waiting",
                            event.position.getX(), event.position.getY(), event.position.getZ()
                    ).withStyle(net.minecraft.ChatFormatting.RED);

                    player.displayClientMessage(message, false);
                    LoggerUtil.info("✅ 登录时发送劫匪等待消息: 玩家={}, 位置={}",
                            playerId, event.position);
                }
            }
        }
    }

    /**
     * 发送延迟消息（如果有实现的话）
     */
    private void sendPendingMessages(Player player) {
        // 实现发送延迟消息的逻辑
        // 可以从延迟消息队列中获取该玩家的消息并发送
    }
}