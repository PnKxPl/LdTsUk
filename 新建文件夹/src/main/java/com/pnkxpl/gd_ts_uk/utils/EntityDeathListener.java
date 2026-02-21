package com.pnkxpl.gd_ts_uk.utils;

import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;

/**
 * 实体死亡监听器 - 处理劫匪死亡掉落
 * 修改：添加劫匪死亡掉落处理
 */
public class EntityDeathListener {

    /**
     * 处理实体死亡事件
     * 修改：添加劫匪死亡掉落处理
     */
    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // 只处理服务器端的实体
        if (entity.level().isClientSide()) {
            return;
        }

        // 检查是否是我们生成的劫匪
        if (isOurGeneratedBandit(entity)) {
            handleBanditDeath(entity);
        }

        // 简单检查：如果实体有"劫匪"名称或是流浪商人
        if (isOurGeneratedEntity(entity)) {
            removeDeadEntityFromLists(entity.getId());
        }
    }

    /**
     * 处理劫匪死亡掉落
     * 新增：生成劫匪的掉落物
     * 修复：确保所有掉落物都被正确生成
     */
    private void handleBanditDeath(LivingEntity bandit) {
        LoggerUtil.info("劫匪死亡，生成掉落物: 实体ID={}", bandit.getId());

        // 获取劫匪掉落物
        List<ItemStack> loot = WanderingTraderExpressDelivery.interceptManager.getBanditLoot(bandit.getId());

        if (loot != null && !loot.isEmpty()) {
            int droppedCount = 0;

            // 在劫匪位置生成掉落物
            for (ItemStack itemStack : loot) {
                if (!itemStack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(
                            bandit.level(),
                            bandit.getX(),
                            bandit.getY(),
                            bandit.getZ(),
                            itemStack.copy()
                    );
                    // 添加随机速度，使掉落物散开
                    itemEntity.setDeltaMovement(
                            (bandit.level().random.nextDouble() - 0.5) * 0.2,
                            bandit.level().random.nextDouble() * 0.2,
                            (bandit.level().random.nextDouble() - 0.5) * 0.2
                    );

                    bandit.level().addFreshEntity(itemEntity);
                    droppedCount++;

                    LoggerUtil.debug("生成掉落物: {} x{}",
                            itemStack.getItem().getDescriptionId(), itemStack.getCount());
                }
            }
            LoggerUtil.info("劫匪掉落物生成完成: 实体ID={}, 掉落物品数量={}",
                    bandit.getId(), droppedCount);
        } else {
            LoggerUtil.warn("劫匪没有掉落物: 实体ID={}", bandit.getId());
        }
    }

    /**
     * 检查是否是我们生成的劫匪
     * 新增：通过自定义名称识别劫匪
     */
    private boolean isOurGeneratedBandit(Entity entity) {
        if (entity.hasCustomName()) {
            String name = entity.getCustomName().getString();
            return "entity.wandering_trader_express_delivery.bandit".equals(name) ||
                    "劫匪".equals(name); // 兼容中文
        }
        return false;
    }

    /**
     * 简单检查是否是我们生成的实体
     */
    private boolean isOurGeneratedEntity(Entity entity) {
        // 检查是否是流浪商人
        if (entity instanceof net.minecraft.world.entity.npc.WanderingTrader) {
            return true;
        }

        // 检查是否有"劫匪"名称
        if (entity.hasCustomName()) {
            String name = entity.getCustomName().getString();
            return "劫匪".equals(name);
        }

        return false;
    }

    /**
     * 从所有列表中移除死亡的实体
     */
    private void removeDeadEntityFromLists(int deadEntityId) {
        LoggerUtil.info("检测到生成的实体死亡，从列表中移除: 实体ID={}", deadEntityId);

        // 获取实体数据映射
        var playerEntityData = WanderingTraderExpressDelivery.pendingDisappearanceManager.getPlayerEntityData();

        // 遍历所有玩家
        for (var typeMap : playerEntityData.values()) {
            // 检查类型3（流浪商人）和类型5（劫匪）
            for (int type : new int[]{3, 5}) {
                var entityList = typeMap.get(type);
                if (entityList != null) {
                    // 移除死亡的实体ID
                    if (entityList.remove(Integer.valueOf(deadEntityId))) {
                        LoggerUtil.debug("从类型{}列表中移除实体ID: {}", type, deadEntityId);
                    }

                    // 如果列表为空，移除整个类型条目
                    if (entityList.isEmpty()) {
                        typeMap.remove(type);
                    }
                }
            }
        }
    }
}