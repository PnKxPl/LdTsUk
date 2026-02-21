package com.pnkxpl.gd_ts_uk.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;

/**
 * 稀有物品生成器
 * 负责生成稀有快递的稀有物品
 */
public class RareItemGenerator {

    /**
     * 生成稀有物品
     */
    public static ItemStack generateRareItem() {
        Random random = new Random();
        double chance = random.nextDouble();

        if (chance < 0.30) {
            // 30% - 32个经验之瓶
            LoggerUtil.debug("生成稀有物品: 32个经验之瓶");
            return new ItemStack(Items.EXPERIENCE_BOTTLE, 32);
        } else if (chance < 0.60) {
            // 30% - 12个钻石
            LoggerUtil.debug("生成稀有物品: 24个钻石");
            return new ItemStack(Items.DIAMOND, 24);
        } else if (chance < 0.90) {
            // 30% - 6个金苹果
            LoggerUtil.debug("生成稀有物品: 6个金苹果");
            return new ItemStack(Items.GOLDEN_APPLE, 6);
        } else {
            // 10% - 1个附魔金苹果
            LoggerUtil.debug("生成稀有物品: 1个附魔金苹果");
            return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2);
        }
    }

    /**
     * 检查物品是否为稀有物品
     */
    public static boolean isRareItem(ItemStack item) {
        if (item.isEmpty()) return false;

        return item.getItem() == Items.EXPERIENCE_BOTTLE && item.getCount() == 15 ||
                item.getItem() == Items.DIAMOND && item.getCount() == 10 ||
                item.getItem() == Items.GOLDEN_APPLE && item.getCount() == 4 ||
                item.getItem() == Items.ENCHANTED_GOLDEN_APPLE && item.getCount() == 1;
    }
}