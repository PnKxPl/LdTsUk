package com.pnkxpl.gd_ts_uk.manager;

import com.pnkxpl.gd_ts_uk.utils.CommonUtils;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交易价格管理器 - 存储和管理玩家选择的交易物品的价格信息
 * 新增：支持存储两个交易物品的成本和最大交易次数
 * 新增：数据持久化支持，确保服务器重启后价格信息不丢失
 * 优化：使用线程安全的数据结构
 */
public class TradePriceManager {
    // 交易价格数据存储：玩家UUID -> 交易价格数据
    private final Map<UUID, TradePriceData> tradePriceData = new ConcurrentHashMap<>();

    public TradePriceManager() {}

    /**
     * 交易价格数据类
     * 存储玩家选择的交易物品的成本信息
     */
    public static class TradePriceData {
        /** 玩家UUID */
        public final UUID playerId;
        /** 选择的交易物品 */
        public final ItemStack selectedItem;
        /** 第一个交易成本物品 */
        public final ItemStack costItem1;
        /** 第一个交易成本物品的数量 */
        public final int costCount1;
        /** 第二个交易成本物品（可能为空） */
        public final ItemStack costItem2;
        /** 第二个交易成本物品的数量 */
        public final int costCount2;
        /** 最大可交易次数 */
        public final int maxUses;
        /** 生成时间（绝对时间，游戏刻） */
        public final long generationTime;

        public TradePriceData(UUID playerId, ItemStack selectedItem,
                              ItemStack costItem1, int costCount1,
                              ItemStack costItem2, int costCount2,
                              int maxUses, long generationTime) {
            this.playerId = playerId;
            this.selectedItem = selectedItem.copy();
            this.costItem1 = costItem1.copy();
            this.costCount1 = costCount1;
            this.costItem2 = costItem2 != null ? costItem2.copy() : ItemStack.EMPTY;
            this.costCount2 = costCount2;
            this.maxUses = maxUses;
            this.generationTime = generationTime;
        }

        /**
         * 检查是否有第二个交易成本
         */
        public boolean hasSecondCost() {
            return costItem2 != null && !costItem2.isEmpty() && costCount2 > 0;
        }
    }

    public void storePlayerTradePrice(UUID playerId, ItemStack selectedItem,
                                      ItemStack costItem1, int costCount1,
                                      ItemStack costItem2, int costCount2,
                                      int maxUses, long generationTime) {
        TradePriceData priceData = new TradePriceData(playerId, selectedItem,
                costItem1, costCount1, costItem2, costCount2, maxUses, generationTime);

        tradePriceData.put(playerId, priceData);

        LoggerUtil.info("存储交易价格信息: 玩家={}, 物品={}, 成本1={}x{}, 成本2={}x{}, 最大次数={}, 当前总数据条数={}",
                playerId, selectedItem.getDescriptionId(),
                costItem1.getDescriptionId(), costCount1,
                costItem2 != null ? costItem2.getDescriptionId() : "无", costCount2,
                maxUses, tradePriceData.size());
    }

    /**
     * 获取玩家的交易价格信息
     */
    public TradePriceData getPlayerTradePrice(UUID playerId) {
        return tradePriceData.get(playerId);
    }

    public void removePlayerTradePrice(UUID playerId) {
        TradePriceData removed = tradePriceData.remove(playerId);
        if (removed != null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            // 跳过前两个元素（当前方法和getStackTrace）
            if (stackTrace.length > 2) {
                StackTraceElement caller = stackTrace[2];
                LoggerUtil.info("移除交易价格信息: 玩家={}, 物品={}, 调用者={}.{}",
                        playerId, removed.selectedItem.getDescriptionId(),
                        caller.getClassName(), caller.getMethodName());
            } else {
                LoggerUtil.info("移除交易价格信息: 玩家={}, 物品={}, 调用栈未知",
                        playerId, removed.selectedItem.getDescriptionId());
            }
        }
    }

    // ==================== 数据持久化方法 ====================

    public void saveData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) {
                LoggerUtil.warn("无法获取主世界，跳过交易价格保存");
                return;
            }

            // 新增日志：输出当前保存的数据条数
            LoggerUtil.info("开始保存交易价格数据，当前数据条数: {}", tradePriceData.size());

            Path dataFile = CommonUtils.getModDataDir(overworld).resolve("trade_prices.dat");
            Files.createDirectories(dataFile.getParent());

            CompoundTag rootTag = new CompoundTag();
            ListTag priceList = new ListTag();

            // 保存所有交易价格数据
            for (TradePriceData priceData : tradePriceData.values()) {
                CompoundTag priceTag = createPriceTag(priceData, overworld);
                priceList.add(priceTag);
            }

            rootTag.put("tradePrices", priceList);
            net.minecraft.nbt.NbtIo.writeCompressed(rootTag, dataFile);

            LoggerUtil.info("交易价格数据保存成功: {}个记录", tradePriceData.size());
        } catch (IOException e) {
            LoggerUtil.error("保存交易价格数据失败", e);
        }
    }

    /**
     * 创建价格数据标签
     */
    private CompoundTag createPriceTag(TradePriceData priceData, ServerLevel level) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("playerId", priceData.playerId);
        tag.put("selectedItem", priceData.selectedItem.save(level.registryAccess()));
        tag.put("costItem1", priceData.costItem1.save(level.registryAccess()));
        tag.putInt("costCount1", priceData.costCount1);

        if (priceData.hasSecondCost()) {
            tag.put("costItem2", priceData.costItem2.save(level.registryAccess()));
            tag.putInt("costCount2", priceData.costCount2);
        }

        tag.putInt("maxUses", priceData.maxUses);
        tag.putLong("generationTime", priceData.generationTime);

        return tag;
    }

    /**
     * 加载交易价格数据
     */
    public void loadData() {
        try {
            ServerLevel overworld = CommonUtils.getOverworld();
            if (overworld == null) {
                LoggerUtil.warn("无法获取主世界，跳过交易价格加载");
                return;
            }

            Path dataFile = CommonUtils.getModDataDir(overworld).resolve("trade_prices.dat");
            if (!Files.exists(dataFile)) {
                LoggerUtil.info("没有找到交易价格数据文件，跳过加载");
                return;
            }

            CompoundTag rootTag = net.minecraft.nbt.NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
            ListTag priceList = rootTag.getList("tradePrices", Tag.TAG_COMPOUND);

            int loadedCount = 0;
            for (int i = 0; i < priceList.size(); i++) {
                if (loadSinglePriceData(priceList.getCompound(i), overworld)) {
                    loadedCount++;
                }
            }

            LoggerUtil.info("交易价格数据加载完成: {}个记录", loadedCount);
        } catch (IOException e) {
            LoggerUtil.error("加载交易价格数据失败", e);
        }
    }

    /**
     * 加载单个价格数据
     */
    private boolean loadSinglePriceData(CompoundTag priceTag, ServerLevel level) {
        try {
            UUID playerId = priceTag.getUUID("playerId");
            ItemStack selectedItem = ItemStack.parse(level.registryAccess(), priceTag.getCompound("selectedItem"))
                    .orElse(new ItemStack(Items.STONE, 1));
            ItemStack costItem1 = ItemStack.parse(level.registryAccess(), priceTag.getCompound("costItem1"))
                    .orElse(new ItemStack(Items.EMERALD, 1));
            int costCount1 = priceTag.getInt("costCount1");

            ItemStack costItem2 = ItemStack.EMPTY;
            int costCount2 = 0;
            if (priceTag.contains("costItem2")) {
                costItem2 = ItemStack.parse(level.registryAccess(), priceTag.getCompound("costItem2"))
                        .orElse(ItemStack.EMPTY);
                costCount2 = priceTag.getInt("costCount2");
            }

            int maxUses = priceTag.getInt("maxUses");
            long generationTime = priceTag.getLong("generationTime");

            TradePriceData priceData = new TradePriceData(playerId, selectedItem,
                    costItem1, costCount1, costItem2, costCount2, maxUses, generationTime);

            tradePriceData.put(playerId, priceData);
            return true;
        } catch (Exception e) {
            LoggerUtil.error("加载单个交易价格数据失败", e);
            return false;
        }
    }

    /**
     * 清空交易价格数据
     */
    public void clearAllData() {
        tradePriceData.clear();
        LoggerUtil.info("已清空所有交易价格数据");
    }
}