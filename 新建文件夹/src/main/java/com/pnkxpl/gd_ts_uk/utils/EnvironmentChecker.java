package com.pnkxpl.gd_ts_uk.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import java.util.Set;
import java.util.HashSet;

/**
 * 环境检查器 - 专门处理环境相关的检测逻辑
 */
public class EnvironmentChecker {

    // 恶劣环境生物群系关键词集合
    private static final Set<String> HARSH_BIOME_KEYWORDS = createHarshBiomeKeywords();

    /**
     * 创建恶劣环境生物群系关键词集合
     */
    private static Set<String> createHarshBiomeKeywords() {
        Set<String> keywords = new HashSet<>();
        // 雪原相关
        keywords.add("snow");
        keywords.add("ice");
        keywords.add("frozen");
        keywords.add("cold");
        keywords.add("taiga");
        // 沙漠相关
        keywords.add("desert");
        keywords.add("badlands");
        keywords.add("arid");
        keywords.add("mesa");
        // 海洋相关
        keywords.add("ocean");
        keywords.add("deep_ocean");
        keywords.add("beach");
        keywords.add("river");
        // 其他恶劣环境
        keywords.add("waste");
        keywords.add("mushroom");
        keywords.add("stony");
        keywords.add("cave");
        keywords.add("void");
        return keywords;
    }

    /**
     * 检查位置是否为恶劣环境
     * @param level 世界实例
     * @param pos 位置坐标
     * @return 是否为恶劣环境
     */
    public static boolean isHarshEnvironment(ServerLevel level, BlockPos pos) {
        // 检查高度
        if (pos.getY() > 200) {
            return true;
        }

        // 检查生物群系
        return isHarshBiome(level, pos);
    }

    /**
     * 检查生物群系是否为恶劣环境
     * @param level 世界实例
     * @param pos 位置坐标
     * @return 是否为恶劣生物群系
     */
    public static boolean isHarshBiome(ServerLevel level, BlockPos pos) {
        // 获取生物群系
        Biome biome = level.getBiome(pos).value();

        // 获取生物群系注册表键
        var biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        ResourceLocation biomeKey = biomeRegistry.getKey(biome);

        if (biomeKey == null) {
            return false;
        }

        String biomePath = biomeKey.getPath().toLowerCase();

        // 检查是否为恶劣生物群系
        for (String keyword : HARSH_BIOME_KEYWORDS) {
            if (biomePath.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否正在下雨
     * @param level 世界实例
     * @param pos 位置坐标
     * @return 是否正在下雨
     */
    public static boolean isRaining(ServerLevel level, BlockPos pos) {
        return level.isRaining();
    }

    /**
     * 计算环境对移动速度的影响
     * @param level 世界实例
     * @param pos 位置坐标
     * @param baseSpeed 基础移动速度
     * @return 调整后的移动速度
     */
    public static int calculateAdjustedSpeed(ServerLevel level, BlockPos pos, int baseSpeed) {
        int adjustedSpeed = baseSpeed;

        // 检查恶劣环境
        if (isHarshEnvironment(level, pos)) {
            adjustedSpeed--;
        }

        // 检查天气
        if (isRaining(level, pos)) {
            adjustedSpeed--;
        }

        return Math.max(1, adjustedSpeed);
    }
}