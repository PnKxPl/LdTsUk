package com.pnkxpl.gd_ts_uk.pending.management;

import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import com.pnkxpl.gd_ts_uk.pending.PendingManagementManager;
import com.pnkxpl.gd_ts_uk.delivery.DeliveryManager;
import com.pnkxpl.gd_ts_uk.core.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 类型1：待生成流浪商人事件
 * 处理延迟生成流浪商人的逻辑
 * 修正：使用事件中的位置信息生成流浪商人
 * 修正：超时机制使用快递消失时间的一半
 * 新增：使用国际化翻译键，支持多语言
 * 修复：类型转换错误 - 正确创建 DeliveryData 对象
 * 修复：逻辑顺序错误 - 优先检查位置是否加载，如果位置已加载就立即生成
 * 新增：详细的位置检查调试信息
 * 新增：支持稀有快递状态保存和传递
 */
public class PendingWanderingTraderEvent {

    /**
     * 处理类型1管理事件（待生成流浪商人）
     * 修正：使用存储的位置信息生成流浪商人
     * 修复：优先检查位置是否加载，如果位置已加载就立即生成，不管事件是否过期
     * 新增：详细的位置检查调试信息
     * 新增：支持稀有快递状态传递
     * @param event 待处理管理事件，包含玩家ID、位置和稀有状态
     * @param manager 管理事件管理器，用于访问玩家和世界数据
     * @param currentTime 当前世界时间（游戏刻）
     * @return true=事件已处理需要移除，false=事件需要保留
     */
    public boolean handleEvent(PendingManagementManager.PendingManagementEvent event,
                               PendingManagementManager manager, long currentTime) {
        WanderingTraderExpressDelivery.LOGGER.info("🔍 处理流浪商人事件: 玩家={}, 位置={}, 管理时间={}, 当前时间={}, 稀有快递={}",
                event.playerId, event.position, event.manageTime, currentTime, event.isRareDelivery);

        // 首先检查玩家是否在主世界
        if (!isPlayerInOverworld(event.playerId, manager)) {
            WanderingTraderExpressDelivery.LOGGER.info("⏳ 玩家不在主世界，保留流浪商人事件: 玩家={}", event.playerId);
            return false; // 保留事件，等待玩家回到主世界
        }

        // 首先检查位置是否已加载（无论事件是否过期）
        ServerLevel level = manager.getPlayerLevel(event.playerId);
        if (level != null) {
            // 添加详细的位置检查调试
            boolean isLoaded = level.isLoaded(event.position);
            WanderingTraderExpressDelivery.LOGGER.info("📍 流浪商人位置检查: 位置={}, 是否加载={}, 世界维度={}, 玩家位置={}, 稀有快递={}",
                    event.position, isLoaded, level.dimension(), getPlayerPosition(event.playerId, manager), event.isRareDelivery);

            if (isLoaded) {
                // 位置已加载，立即生成流浪商人
                WanderingTraderExpressDelivery.LOGGER.info("✅ 位置已加载，立即生成流浪商人，稀有状态={}, 物品={}", event.isRareDelivery, event.selectedItem.getDescriptionId());
                generateDelayedWanderingTrader(event.playerId, level, event.position, currentTime, event.isRareDelivery, event.selectedItem);
                return true; // 事件处理完成，需要移除
            }
        } else {
            WanderingTraderExpressDelivery.LOGGER.warn("❌ 无法获取玩家世界: 玩家={}", event.playerId);
        }

        // 如果位置未加载，检查事件是否已过期
        if (currentTime >= event.manageTime) {
            WanderingTraderExpressDelivery.LOGGER.warn("⏰ 流浪商人生成事件已过期且位置未加载: 玩家={}, 位置={}, 稀有快递={}",
                    event.playerId, event.position, event.isRareDelivery);
            sendExpiredMessage(event.playerId, manager);
            return true;
        }

        // 位置未加载且事件未过期，保留事件等待下次处理
        WanderingTraderExpressDelivery.LOGGER.info("⏳ 流浪商人生成位置未加载，保留事件: 剩余时间={}, 稀有快递={}",
                event.manageTime - currentTime, event.isRareDelivery);
        return false;
    }

    /**
     * 生成延迟的流浪商人
     * 修正：使用存储的位置信息生成流浪商人
     * 修复：正确创建 DeliveryData 对象
     * 新增：支持稀有快递状态传递
     * 新增：使用保存的选择物品
     * @param playerId 玩家UUID
     * @param level 服务器世界实例
     * @param position 生成位置
     * @param currentTime 当前时间（绝对时间）
     * @param isRareDelivery 稀有快递状态
     * @param selectedItem 选择的物品
     */
    private void generateDelayedWanderingTrader(UUID playerId, ServerLevel level, BlockPos position, long currentTime, boolean isRareDelivery, ItemStack selectedItem) {
        try {
            WanderingTraderExpressDelivery.LOGGER.info("👨‍💼 开始生成流浪商人: 玩家={}, 位置={}, 稀有快递={}, 物品={}",
                    playerId, position, isRareDelivery, selectedItem.getDescriptionId());

            // 创建正确的 DeliveryData 对象，传递稀有状态和选择物品
            DeliveryManager.DeliveryData deliveryData = createTempDeliveryData(playerId, position, currentTime, isRareDelivery, selectedItem);
            WanderingTraderExpressDelivery.LOGGER.info("📦 临时快递数据创建成功，稀有快递={}, 物品={}", isRareDelivery, selectedItem.getDescriptionId());

            // 检查拦截管理器是否可用
            if (WanderingTraderExpressDelivery.interceptManager == null) {
                WanderingTraderExpressDelivery.LOGGER.error("❌ 拦截管理器为null");
                return;
            }

            // 调用拦截管理器生成流浪商人
            WanderingTraderExpressDelivery.LOGGER.info("🔄 调用拦截管理器生成流浪商人，稀有快递={}, 物品={}", isRareDelivery, selectedItem.getDescriptionId());
            WanderingTraderExpressDelivery.interceptManager.spawnWanderingTrader(deliveryData);

            WanderingTraderExpressDelivery.LOGGER.info("✅ 流浪商人生成调用完成: 玩家={}, 位置={}, 稀有快递={}, 物品={}",
                    playerId, position, isRareDelivery, selectedItem.getDescriptionId());
        } catch (Exception e) {
            WanderingTraderExpressDelivery.LOGGER.error("💥 生成流浪商人时异常: 玩家={}, 位置={}, 稀有快递={}, 物品={}",
                    playerId, position, isRareDelivery, selectedItem.getDescriptionId(), e);
        }
    }

    /**
     * 创建临时快递数据
     * 修正：正确创建 DeliveryData 对象，修复类型转换错误
     * 新增：支持稀有快递状态参数
     * 新增：支持选择物品参数
     * @param playerId 玩家UUID
     * @param position 生成位置
     * @param currentTime 当前时间（绝对时间）
     * @param isRareDelivery 稀有快递状态
     * @param selectedItem 选择的物品
     * @return 临时快递数据对象
     */
    private DeliveryManager.DeliveryData createTempDeliveryData(UUID playerId, BlockPos position, long currentTime, boolean isRareDelivery, ItemStack selectedItem) {
        // 计算消失时间（使用快递消失时间的一半）
        long disappearTicks = Config.minutesToTicks(Config.getHalfDeliveryDisappearTime());

        // 创建 DeliveryData 对象，传递稀有状态和选择物品
        return new DeliveryManager.DeliveryData(
                playerId,                                    // UUID playerId
                selectedItem,                                // ItemStack selectedItem (使用保存的物品)
                position,                                    // BlockPos destination
                currentTime,                                 // long generationTime (开始时间)
                disappearTicks,                             // long relativeDisappearTicks (相对消失时间)
                false,                                       // boolean isIntercepted (不是拦截事件)
                isRareDelivery                              // boolean isRareDelivery - 传递稀有状态
        );
    }

    /**
     * 获取玩家当前位置（用于调试）
     * @param playerId 玩家UUID
     * @param manager 管理事件管理器
     * @return 玩家位置字符串或"玩家不在线"
     */
    private String getPlayerPosition(UUID playerId, PendingManagementManager manager) {
        Player player = manager.getPlayer(playerId);
        if (player != null) {
            return player.blockPosition().toString();
        }
        return "玩家不在线";
    }

    /**
     * 发送过期消息
     * 修正：使用国际化翻译键
     * @param playerId 玩家UUID
     * @param manager 管理事件管理器
     */
    private void sendExpiredMessage(UUID playerId, PendingManagementManager manager) {
        Player player = manager.getPlayer(playerId);
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.wandering_trader_express_delivery.trader_generation_failed")
                            .withStyle(net.minecraft.ChatFormatting.RED),
                    false
            );
        }
    }

    /**
     * 检查玩家是否在主世界
     * 新增：确保玩家在主世界才生成实体
     * @param playerId 玩家UUID
     * @param manager 管理事件管理器
     * @return 是否在主世界
     */
    private boolean isPlayerInOverworld(UUID playerId, PendingManagementManager manager) {
        Player player = manager.getPlayer(playerId);
        if (player != null) {
            boolean inOverworld = player.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD);
            WanderingTraderExpressDelivery.LOGGER.debug("玩家维度检查: 玩家={}, 维度={}, 是否主世界={}",
                    playerId, player.level().dimension(), inOverworld);
            return inOverworld;
        }
        // 玩家不在线时也返回false，保留事件
        return false;
    }
}