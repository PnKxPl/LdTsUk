package com.pnkxpl.gd_ts_uk.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

/**
 * 配置管理器 - 处理所有模组配置项
 * 新增：添加完整的日志输出控制开关，包括ERROR级别
 * 新增：GUI消耗物品自定义配置
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 配置项定义 - 使用国际化翻译键
    public static final ModConfigSpec.DoubleValue DELIVERY_DISAPPEAR_TIME = BUILDER
            .comment("快递消失时间（分钟），支持小数，最小3分钟")
            .translation("config.wandering_trader_express_delivery.delivery_disappear_time")
            .defineInRange("deliveryDisappearTime", 8.0, 3.0, 15.0);

    public static final ModConfigSpec.IntValue INTERCEPT_CHANCE = BUILDER
            .comment("拦截概率（百分比）")
            .translation("config.wandering_trader_express_delivery.intercept_chance")
            .defineInRange("interceptChance", 15, 0, 100);

    public static final ModConfigSpec.IntValue BANDIT_MIN_COUNT = BUILDER
            .comment("劫匪最小生成数量")
            .translation("config.wandering_trader_express_delivery.bandit_min_count")
            .defineInRange("banditMinCount", 3, 3, 6);

    public static final ModConfigSpec.IntValue BANDIT_MAX_COUNT = BUILDER
            .comment("劫匪最大生成数量")
            .translation("config.wandering_trader_express_delivery.bandit_max_count")
            .defineInRange("banditMaxCount", 6, 6, 15);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BANDIT_MOB_POOL = BUILDER
            .comment("劫匪MOB池配置，格式: modid:entity_id,权重,是否发光(可选)",
                    "示例:",
                    "  minecraft:pillager,40,true",
                    "  youmod:your_bandit,50,true")
            .translation("config.wandering_trader_express_delivery.bandit_mob_pool")
            .defineList("banditMobPool",
                    Arrays.asList(
                            "minecraft:pillager,30,true",
                            "minecraft:vindicator,30,true",
                            "minecraft:witch,20,true",
                            "minecraft:evoker,10,true"
                    ),
                    obj -> obj instanceof String);

    // ✅ 新增：GUI消耗物品自定义配置 - 与自定义怪物格式类似
    public static final ModConfigSpec.ConfigValue<List<? extends String>> GUI_COST_ITEMS = BUILDER
            .comment("打开GUI时需要消耗的物品配置，格式: modid:item_id,数量",
                    "示例:",
                    "  minecraft:emerald,1 (默认 - 1个绿宝石)",
                    "  minecraft:diamond,2 (需要2个钻石)",
                    "  minecraft:gold_ingot,5 (需要5个金锭)",
                    "注意：玩家主手或副手持有任意一种配置的物品即可",
                    "如果配置多个物品，玩家只需满足其中一个即可")
            .translation("config.wandering_trader_express_delivery.gui_cost_items")
            .defineList("guiCostItems",
                    Arrays.asList("minecraft:emerald,1"), // 默认：1个绿宝石
                    obj -> obj instanceof String);

    public static final ModConfigSpec.BooleanValue RELOAD_MOB_POOL_ON_SPAWN = BUILDER
            .comment("是否在每次生成劫匪时重载MOB池配置")
            .translation("config.wandering_trader_express_delivery.reload_mob_pool_on_spawn")
            .define("reloadMobPoolOnSpawn", true);

    // ✅ 完整的日志输出控制开关
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGS = BUILDER
            .comment("启用调试日志输出")
            .translation("config.wandering_trader_express_delivery.enable_debug_logs")
            .define("enableDebugLogs", false);

    public static final ModConfigSpec.BooleanValue ENABLE_INFO_LOGS = BUILDER
            .comment("启用信息日志输出")
            .translation("config.wandering_trader_express_delivery.enable_info_logs")
            .define("enableInfoLogs", false);

    public static final ModConfigSpec.BooleanValue ENABLE_WARN_LOGS = BUILDER
            .comment("启用警告日志输出")
            .translation("config.wandering_trader_express_delivery.enable_warn_logs")
            .define("enableWarnLogs", false);

    public static final ModConfigSpec.BooleanValue ENABLE_ERROR_LOGS = BUILDER
            .comment("启用错误日志输出")
            .translation("config.wandering_trader_express_delivery.enable_error_logs")
            .define("enableErrorLogs", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    // 配置获取方法
    public static double getDeliveryDisappearTime() {
        return DELIVERY_DISAPPEAR_TIME.get();
    }

    public static int getInterceptChance() {
        return INTERCEPT_CHANCE.get();
    }

    public static int getBanditMinCount() {return BANDIT_MIN_COUNT.get();}

    public static int getBanditMaxCount() {return BANDIT_MAX_COUNT.get();}

    public static List<? extends String> getBanditMobPool() {return BANDIT_MOB_POOL.get();}

    // ✅ 新增：获取GUI消耗物品配置
    public static List<? extends String> getGuiCostItems() {return GUI_COST_ITEMS.get();}

    public static boolean shouldReloadMobPoolOnSpawn() {return RELOAD_MOB_POOL_ON_SPAWN.get();}

    // ✅ 完整的日志开关获取方法
    public static boolean enableDebugLogs() {
        return ENABLE_DEBUG_LOGS.get();
    }

    public static boolean enableInfoLogs() {
        return ENABLE_INFO_LOGS.get();
    }

    public static boolean enableWarnLogs() {
        return ENABLE_WARN_LOGS.get();
    }

    public static boolean enableErrorLogs() {
        return ENABLE_ERROR_LOGS.get();
    }

    /**
     * 获取安全的快递消失时间（确保最小值为0.5分钟）
     */
    public static double getSafeDeliveryDisappearTime() {
        return getDeliveryDisappearTime();
    }

    /**
     * 获取快递消失时间的一半（用于交易列表冷却时间和箱子和劫匪的消失时间）
     */
    public static double getHalfDeliveryDisappearTime() {
        return getSafeDeliveryDisappearTime() / 2.0;
    }

    /**
     * 将分钟转换为游戏刻
     */
    public static long minutesToTicks(double minutes) {
        return (long) (minutes * 60 * 20);
    }

    /**
     * ✅ 新增：获取需求物品的显示名称列表
     * 用于在消息中显示玩家需要哪些物品
     * @return 格式化后的需求物品字符串，如"绿宝石 x1 或 钻石 x2"
     */
    public static String getRequiredCostItemsDisplayString() {
        List<? extends String> costItemsConfig = getGuiCostItems();
        if (costItemsConfig.isEmpty()) {
            // 默认配置
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < costItemsConfig.size(); i++) {
            String configEntry = costItemsConfig.get(i);
            try {
                // 解析格式: modid:item_id,数量
                String[] parts = configEntry.split(",");
                if (parts.length != 2) continue;

                String itemId = parts[0].trim();
                int requiredCount = Integer.parseInt(parts[1].trim());

                // 获取物品
                ResourceLocation itemRes = ResourceLocation.tryParse(itemId);
                if (itemRes == null) continue;

                Item item = BuiltInRegistries.ITEM.get(itemRes);
                if (item == null) continue;

                // 获取物品的显示名称
                String displayName = item.getDescription().getString();

                if (i > 0) {
                    // 添加分隔符（使用"或"）
                    sb.append(" 或 ");
                }
                sb.append(displayName).append(" x").append(requiredCount);

            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        // 如果没有有效配置，返回默认
        if (sb.length() == 0) {
            return "null";
        }

        return sb.toString();
    }

    /**
     * ✅ 新增：检查玩家是否持有任何配置中的物品
     * @param player 玩家实例
     * @return 是否持有任何配置中的物品
     */
    public static boolean hasAnyCostItem(net.minecraft.world.entity.player.Player player) {
        List<? extends String> costItemsConfig = getGuiCostItems();
        if (costItemsConfig.isEmpty()) {
            // 默认配置：检查绿宝石
            return player.getMainHandItem().getItem() == Items.EMERALD ||
                    player.getOffhandItem().getItem() == Items.EMERALD;
        }

        // 解析配置并检查玩家手持物品
        for (String configEntry : costItemsConfig) {
            try {
                String[] parts = configEntry.split(",");
                if (parts.length != 2) continue;

                String itemId = parts[0].trim();
                int requiredCount = Integer.parseInt(parts[1].trim());

                ResourceLocation itemRes = ResourceLocation.tryParse(itemId);
                if (itemRes == null) continue;

                Item item = BuiltInRegistries.ITEM.get(itemRes);
                if (item == null) continue;

                // 检查主手和副手
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                if ((mainHand.getItem() == item && mainHand.getCount() >= requiredCount) ||
                        (offHand.getItem() == item && offHand.getCount() >= requiredCount)) {
                    return true;
                }

            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        return false;
    }
}