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

        //发送该玩家的所有等待消息
        sendPendingMessages(player);

        //检查并发送管理事件等待消息
        sendManagementEventMessages(player);
    }

    /**
     * 发送管理事件等待消息
     */
    private void sendManagementEventMessages(Player player) {
        UUID playerId = player.getUUID();
    }

    /**
     * 发送延迟消息（如果有实现的话）
     */
    private void sendPendingMessages(Player player) {
        //实现发送延迟消息的逻辑
        //可以从延迟消息队列中获取该玩家的消息并发送
    }
}