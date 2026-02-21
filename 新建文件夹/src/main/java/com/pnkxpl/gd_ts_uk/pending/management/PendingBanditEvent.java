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
 * 类型2：待生成劫匪事件
 * 处理延迟生成劫匪的逻辑
 * 优化：简化代码，改进错误处理
 * 修正：修改拦截点数据丢失的消息为"已经找不到劫匪的踪迹了"
 * 优化：使用公共工具类，减少代码重复
 * 新增：使用国际化翻译键，支持多语言
 * 修正：使用存储的位置信息生成劫匪
 * 修复：逻辑顺序错误 - 优先检查位置是否加载，如果位置已加载就立即生成
 * 新增：保存和传递稀有快递状态
 */
public class PendingBanditEvent {

    /**
     * 处理类型2管理事件（待生成劫匪）
     * 修正：使用存储的位置信息生成劫匪
     * 修复：优先检查位置是否加载，如果位置已加载就立即生成
     * 新增：传递稀有快递状态
     * @param event 待处理管理事件，包含玩家ID、位置和稀有状态
     * @param manager 管理事件管理器，用于访问玩家和世界数据
     * @param currentTime 当前世界时间（游戏刻）
     * @return true=事件已处理需要移除，false=事件需要保留
     */
    public boolean handleEvent(PendingManagementManager.PendingManagementEvent event,
                               PendingManagementManager manager, long currentTime) {
        WanderingTraderExpressDelivery.LOGGER.info("🔍 处理劫匪事件: 玩家={}, 位置={}, 管理时间={}, 当前时间={}, 稀有快递={}",
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
            WanderingTraderExpressDelivery.LOGGER.info("📍 劫匪位置检查: 位置={}, 是否加载={}, 世界维度={}, 玩家位置={}, 稀有快递={}",
                    event.position, isLoaded, level.dimension(), getPlayerPosition(event.playerId, manager), event.isRareDelivery);

            if (isLoaded) {
                // 位置已加载，立即生成劫匪
                WanderingTraderExpressDelivery.LOGGER.info("✅ 位置已加载，立即生成劫匪，稀有状态={}, 物品={}", event.isRareDelivery, event.selectedItem.getDescriptionId());
                generateBandits(event.playerId, level, event.position, currentTime, event.isRareDelivery, event.selectedItem);
                return true; // 事件处理完成，需要移除
            }
        } else {
            WanderingTraderExpressDelivery.LOGGER.warn("❌ 无法获取玩家世界: 玩家={}", event.playerId);
        }

        // 如果位置未加载，检查事件是否已过期
        if (currentTime >= event.manageTime) {
            WanderingTraderExpressDelivery.LOGGER.warn("⏰ 劫匪事件已过期且位置未加载: 玩家={}, 位置={}, 稀有快递={}",
                    event.playerId, event.position, event.isRareDelivery);
            sendExpiredMessage(event.playerId, manager);
            return true;
        }

        // 位置未加载且事件未过期，保留事件等待下次处理
        WanderingTraderExpressDelivery.LOGGER.info("⏳ 劫匪位置未加载，保留事件: 剩余时间={}, 稀有快递={}",
                event.manageTime - currentTime, event.isRareDelivery);
        return false;
    }

    /**
     * 生成劫匪
     * 修正：使用存储的位置信息生成劫匪
     * 修复：确保拦截点正确传递到 DeliveryData
     * 新增：传递稀有快递状态
     * 新增：使用保存的选择物品
     * @param playerId 玩家UUID
     * @param level 服务器世界实例
     * @param interceptPoint 拦截点位置
     * @param currentTime 当前时间（绝对时间）
     * @param isRareDelivery 稀有快递状态
     * @param selectedItem 选择的物品
     */
    private void generateBandits(UUID playerId, ServerLevel level, BlockPos interceptPoint, long currentTime, boolean isRareDelivery, ItemStack selectedItem) {
        WanderingTraderExpressDelivery.LOGGER.info("开始生成劫匪: 玩家={}, 拦截点={}, 稀有快递={}, 物品={}",
                playerId, interceptPoint, isRareDelivery, selectedItem.getDescriptionId());

        // 验证拦截点不为null
        if (interceptPoint == null) {
            WanderingTraderExpressDelivery.LOGGER.error("拦截点为null，无法生成劫匪: 玩家={}", playerId);
            return;
        }

        try {
            // 创建临时快递数据，传递稀有状态和选择物品
            DeliveryManager.DeliveryData tempDelivery = createTempDeliveryData(playerId, interceptPoint, currentTime, isRareDelivery, selectedItem);

            WanderingTraderExpressDelivery.LOGGER.info("调用拦截管理器生成劫匪: 拦截点={}, 稀有快递={}, 物品={}",
                    tempDelivery.interceptPoint, isRareDelivery, selectedItem.getDescriptionId());
            WanderingTraderExpressDelivery.interceptManager.spawnBandits(tempDelivery);

            WanderingTraderExpressDelivery.LOGGER.info("延迟劫匪生成成功: 玩家={}, 位置={}, 稀有快递={}, 物品={}",
                    playerId, interceptPoint, isRareDelivery, selectedItem.getDescriptionId());
        } catch (Exception e) {
            WanderingTraderExpressDelivery.LOGGER.error("生成劫匪时异常: 玩家={}, 位置={}, 稀有快递={}, 物品={}",
                    playerId, interceptPoint, isRareDelivery, selectedItem.getDescriptionId(), e);
        }
    }

    /**
     * 创建临时快递数据
     * 修正：修复 DeliveryData 构造函数参数，确保interceptPoint被正确设置
     * 修改：添加isRareDelivery参数，传递稀有状态
     * 新增：添加selectedItem参数，传递选择物品
     * @param playerId 玩家UUID
     * @param interceptPoint 拦截点位置
     * @param currentTime 当前时间（绝对时间）
     * @param isRareDelivery 稀有快递状态
     * @param selectedItem 选择的物品
     * @return 临时快递数据对象
     */
    private DeliveryManager.DeliveryData createTempDeliveryData(UUID playerId, BlockPos interceptPoint, long currentTime, boolean isRareDelivery, ItemStack selectedItem) {
        // 创建 DeliveryData 对象，传递稀有状态和选择物品
        DeliveryManager.DeliveryData deliveryData = new DeliveryManager.DeliveryData(
                playerId,
                selectedItem, // 使用保存的物品
                interceptPoint, // 使用拦截点作为目的地
                currentTime, // 开始时间（绝对时间）
                Config.minutesToTicks(Config.getHalfDeliveryDisappearTime()), // 相对消失时间（使用快递消失时间的一半）
                true, // isIntercepted
                isRareDelivery // 传递稀有状态
        );

        // 确保interceptPoint字段被正确设置
        deliveryData.interceptPoint = interceptPoint;

        WanderingTraderExpressDelivery.LOGGER.debug("创建临时快递数据: 玩家={}, 拦截点={}, 设置后的拦截点={}, 稀有快递={}, 物品={}",
                playerId, interceptPoint, deliveryData.interceptPoint, isRareDelivery, selectedItem.getDescriptionId());

        return deliveryData;
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
     * 修正：统一使用"已经找不到劫匪的踪迹了"，使用国际化翻译键
     * @param playerId 玩家UUID
     * @param manager 管理事件管理器
     */
    private void sendExpiredMessage(UUID playerId, PendingManagementManager manager) {
        Player player = manager.getPlayer(playerId);
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.wandering_trader_express_delivery.bandits_gone")
                            .withStyle(net.minecraft.ChatFormatting.RED),
                    false
            );
        }
        WanderingTraderExpressDelivery.LOGGER.info("劫匪事件过期: 玩家={}", playerId);
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