package com.pnkxpl.gd_ts_uk.manager;

import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.*;

/**
 * MOB配置管理器 - 管理劫匪MOB池和生成逻辑 NeoForge 1.21.1版本
 */
public class MobConfigManager {
    private static final List<MobConfig> MOB_POOL = new ArrayList<>();
    private static int TOTAL_WEIGHT = 0;
    private static boolean initialized = false;

    /**
     * MOB配置类
     */
    public static class MobConfig {
        public final EntityType<? extends Mob> entityType;
        public final int weight;
        public final boolean glowing;
        public final String entityId;

        public MobConfig(EntityType<? extends Mob> entityType, int weight, boolean glowing, String entityId) {
            this.entityType = entityType;
            this.weight = weight;
            this.glowing = glowing;
            this.entityId = entityId;
        }
    }

    /**
     * 初始化MOB池
     */
    public static void initializeMobPool() {
        MOB_POOL.clear();
        TOTAL_WEIGHT = 0;
        initialized = false;

        List<? extends String> mobPoolConfig = Config.getBanditMobPool();
        if (mobPoolConfig.isEmpty()) {
            LoggerUtil.warn("劫匪MOB池配置为空，使用默认配置");
            loadDefaultMobPool();
            return;
        }

        int successCount = 0;
        for (String configStr : mobPoolConfig) {
            try {
                MobConfig mobConfig = parseMobConfig(configStr);
                if (mobConfig != null) {
                    MOB_POOL.add(mobConfig);
                    TOTAL_WEIGHT += mobConfig.weight;
                    successCount++;
                }
            } catch (Exception e) {
                LoggerUtil.error("解析MOB配置失败: {}", configStr, e);
            }
        }

        if (MOB_POOL.isEmpty()) {
            LoggerUtil.warn("没有有效的MOB配置，使用默认配置");
            loadDefaultMobPool();
        } else {
            initialized = true;
            LoggerUtil.info("MOB池初始化完成: 成功加载 {} 种MOB, 总权重: {}", successCount, TOTAL_WEIGHT);
        }
    }

    /**
     * 解析MOB配置字符串 - 简单版本
     */
    private static MobConfig parseMobConfig(String configStr) {
        try {
            String[] parts = configStr.split(",");
            if (parts.length < 2) {
                LoggerUtil.warn("MOB配置格式错误: {}", configStr);
                return null;
            }

            String entityId = parts[0].trim();
            int weight = Integer.parseInt(parts[1].trim());
            boolean glowing = parts.length >= 3 ? Boolean.parseBoolean(parts[2].trim()) : true;

            // 解析实体类型
            ResourceLocation resourceLocation = ResourceLocation.tryParse(entityId);
            if (resourceLocation == null) {
                LoggerUtil.warn("无效的实体ID: {}", entityId);
                return null;
            }

            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);
            if (entityType == null) {
                LoggerUtil.warn("未找到实体类型: {}", entityId);
                return null;
            }

            // 检查注册名是否匹配
            ResourceLocation actualKey = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (actualKey == null || !actualKey.equals(resourceLocation)) {
                LoggerUtil.warn("实体类型注册名不匹配: 输入={}, 实际={}", entityId, actualKey);
                return null;
            }

            // 简单检查：尝试转换为Mob类型
            try {
                @SuppressWarnings("unchecked")
                EntityType<? extends Mob> mobEntityType = (EntityType<? extends Mob>) entityType;
                return new MobConfig(mobEntityType, weight, glowing, entityId);
            } catch (ClassCastException e) {
                LoggerUtil.warn("实体类型不是Mob: {}", entityId);
                return null;
            }

        } catch (Exception e) {
            LoggerUtil.error("解析MOB配置时出错: {}", configStr, e);
            return null;
        }
    }

    /**
     * 加载默认MOB池
     */
    private static void loadDefaultMobPool() {
        String[] defaultMobs = {
                "minecraft:pillager,40,true",
                "minecraft:vindicator,30,true",
                "minecraft:witch,20,true",
                "minecraft:evoker,10,true"
        };

        for (String configStr : defaultMobs) {
            MobConfig mobConfig = parseMobConfig(configStr);
            if (mobConfig != null) {
                MOB_POOL.add(mobConfig);
                TOTAL_WEIGHT += mobConfig.weight;
            }
        }

        initialized = true;
        LoggerUtil.info("默认MOB池加载完成: {} 种MOB, 总权重: {}", MOB_POOL.size(), TOTAL_WEIGHT);
    }

    /**
     * 安全重载MOB池
     */
    public static void safeReloadMobPool() {
        try {
            initializeMobPool();
        } catch (Exception e) {
            LoggerUtil.error("重载MOB池时出错", e);
        }
    }

    /**
     * 检查并重载MOB池
     */
    public static void checkAndReloadMobPool() {
        if (Config.shouldReloadMobPoolOnSpawn()) {
            safeReloadMobPool();
        } else if (!initialized) {
            initializeMobPool();
        }
    }

    /**
     * 获取随机MOB
     */
    public static MobConfig getRandomMob(Random random) {
        checkAndReloadMobPool();

        if (MOB_POOL.isEmpty()) {
            LoggerUtil.error("MOB池为空");
            return null;
        }

        int randomWeight = random.nextInt(TOTAL_WEIGHT);
        int currentWeight = 0;

        for (MobConfig mobConfig : MOB_POOL) {
            currentWeight += mobConfig.weight;
            if (randomWeight < currentWeight) {
                return mobConfig;
            }
        }

        return MOB_POOL.get(0);
    }

    /**
     * 获取MOB池信息
     */
    public static String getMobPoolInfo() {
        if (!initialized) {
            return "MOB池未初始化";
        }

        StringBuilder info = new StringBuilder("MOB池: " + MOB_POOL.size() + " 种, 总权重: " + TOTAL_WEIGHT);
        for (MobConfig mobConfig : MOB_POOL) {
            info.append("\n  - ").append(mobConfig.entityId)
                    .append(" (权重: ").append(mobConfig.weight)
                    .append(", 发光: ").append(mobConfig.glowing).append(")");
        }
        return info.toString();
    }

    /**
     * 重新加载MOB池
     */
    public static void reloadMobPool() {
        LoggerUtil.info("重新加载MOB池配置");
        initializeMobPool();
    }

    /**
     * 检查MOB池是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取MOB池大小
     */
    public static int getMobPoolSize() {
        return MOB_POOL.size();
    }

    /**
     * 获取总权重
     */
    public static int getTotalWeight() {
        return TOTAL_WEIGHT;
    }

    /**
     * 获取所有MOB配置（用于调试）
     */
    public static List<MobConfig> getAllMobConfigs() {
        return new ArrayList<>(MOB_POOL);
    }

    /**
     * 验证实体类型是否存在且为Mob
     */
    public static boolean isValidEntityType(String entityId) {
        try {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(entityId);
            if (resourceLocation == null) {
                return false;
            }

            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);
            if (entityType == null) {
                return false;
            }

            return Mob.class.isAssignableFrom(entityType.getBaseClass());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清理MOB池
     */
    public static void clearMobPool() {
        MOB_POOL.clear();
        TOTAL_WEIGHT = 0;
        initialized = false;
        LoggerUtil.info("已清空MOB池");
    }

}