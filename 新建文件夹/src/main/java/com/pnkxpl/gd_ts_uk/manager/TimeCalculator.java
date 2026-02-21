package com.pnkxpl.gd_ts_uk.manager;

import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.delivery.DeliveryManager;
import com.pnkxpl.gd_ts_uk.delivery.DeliveryMovement;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.UUID;

/**
 * 时间计算器 - 定期处理所有事件和快递移动
 * 修复：确保快递移动被正确调用
 * 修正：快递时间计算问题 - 使用正确的过期检查方法
 */
public class TimeCalculator {
    private long lastProcessTime = 0;
    private long lastDeliveryProcessTime = 0;

    // 处理间隔：每3秒处理一次其他事件（60 ticks）
    private static final long PROCESS_INTERVAL = 60;

    // 快递移动处理间隔：每5秒处理一次（100 ticks）
    private static final long DELIVERY_PROCESS_INTERVAL = 100;
    // 初始处理延迟（5 tick）
    private static final long INITIAL_DELAY = 5;

    public TimeCalculator() {
        // 构造函数
    }

    /**
     * 服务器tick事件监听
     */
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        // 获取主世界
        ServerLevel overworld = event.getServer().overworld();
        if (overworld == null) return;

        long currentTime = overworld.getGameTime();

        // 检查处理间隔（3秒）- 处理所有管理器事件
        if (currentTime - lastProcessTime >= PROCESS_INTERVAL) {
            processAllManagers(overworld);
            lastProcessTime = currentTime;
        }

        // 检查快递移动处理间隔（5秒）- 处理快递移动
        if (currentTime - lastDeliveryProcessTime >= DELIVERY_PROCESS_INTERVAL) {
            processDeliveryMovements(overworld);
            lastDeliveryProcessTime = currentTime;
        }
    }

    /**
     * 服务器停止事件监听 - 重置时间变量
     */
    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        resetTimeVariables();
        LoggerUtil.info("⏰ 时间计算器已重置");
    }

    /**
     * 重置时间变量W
     */
    public void resetTimeVariables() {
        lastProcessTime = 0;
        lastDeliveryProcessTime = 0;
        LoggerUtil.debug("时间计算器变量已重置: lastProcessTime={}, lastDeliveryProcessTime={}",
                lastProcessTime, lastDeliveryProcessTime);
    }

    /**
     * 处理所有管理器（不包括快递移动）
     * 新增：关键调试日志
     */
    private void processAllManagers(ServerLevel level) {
        long currentTime = level.getGameTime();
        LoggerUtil.debug("⏰ 时间计算器处理: 当前时间={}", currentTime);

        // 处理待处理消失事件
        WanderingTraderExpressDelivery.pendingDisappearanceManager.processAllEvents(currentTime);

        // 处理待处理管理事件
        LoggerUtil.info("🔄 开始处理管理事件");
        WanderingTraderExpressDelivery.pendingManagementManager.processAllEvents(currentTime);
        LoggerUtil.info("✅ 管理事件处理完成");
    }

    /**
     * 处理快递移动 - 每5秒执行一次
     * 修复：确保快递移动被正确调用
     * 修正：使用剩余时间进行正确的过期检查
     */
    private void processDeliveryMovements(ServerLevel level) {
        long currentTime = level.getGameTime();

        // 遍历所有活跃快递并移动
        java.util.Map<UUID, DeliveryManager.DeliveryData> deliveries =
                new java.util.HashMap<>(WanderingTraderExpressDelivery.deliveryManager.activeDeliveries);

        int processedCount = 0;
        for (java.util.Map.Entry<UUID, DeliveryManager.DeliveryData> entry : deliveries.entrySet()) {
            UUID playerId = entry.getKey();
            DeliveryManager.DeliveryData delivery = entry.getValue();

            // 检查快递是否仍然活跃
            if (!WanderingTraderExpressDelivery.deliveryManager.activeDeliveries.containsKey(playerId)) {
                continue;
            }

            // 检查快递是否过期 - 使用剩余时间进行过期检查
            long remainingTime = delivery.getRemainingTime(currentTime);
            if (remainingTime <= 0) {
                LoggerUtil.info("快递已过期，移除: 玩家={}, 剩余时间={} ticks",
                        playerId, remainingTime);
                WanderingTraderExpressDelivery.deliveryManager.removePlayerDelivery(playerId);
                continue;
            }

            // 使用DeliveryMovement类处理移动逻辑
            try {
                DeliveryMovement.moveDelivery(delivery);
                processedCount++;

                LoggerUtil.debug("处理快递移动: 玩家={}, 当前位置={}, 目的地={}",
                        playerId, delivery.currentPosition, delivery.destination);
            } catch (Exception e) {
                LoggerUtil.error("处理快递移动时出错: 玩家={}", playerId, e);
            }
        }

        if (processedCount > 0) {
            LoggerUtil.debug("处理了 {} 个快递的移动", processedCount);
        }
    }

    /**
     * 获取最后处理时间（用于调试）
     */
    public long getLastProcessTime() {
        return lastProcessTime;
    }

    /**
     * 获取处理间隔（用于调试）
     */
    public long getProcessInterval() {
        return PROCESS_INTERVAL;
    }

    /**
     * 获取快递移动处理间隔（用于调试）
     */
    public long getDeliveryProcessInterval() {
        return DELIVERY_PROCESS_INTERVAL;
    }
}