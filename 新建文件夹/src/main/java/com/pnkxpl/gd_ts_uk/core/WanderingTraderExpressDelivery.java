package com.pnkxpl.gd_ts_uk.core;

import com.pnkxpl.gd_ts_uk.*;
import com.pnkxpl.gd_ts_uk.EventHandler;
import com.pnkxpl.gd_ts_uk.delivery.DeliveryManager;
import com.pnkxpl.gd_ts_uk.manager.*;
import com.pnkxpl.gd_ts_uk.manager.*;
import com.pnkxpl.gd_ts_uk.pending.PendingDisappearanceManager;
import com.pnkxpl.gd_ts_uk.pending.PendingManagementManager;
import com.pnkxpl.gd_ts_uk.utils.EntityDeathListener;
import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
import com.pnkxpl.gd_ts_uk.utils.PlayerLoginListener;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 主模组类 - 流浪商人快递系统
 * 负责模组初始化、管理器创建和事件注册
 * 优化：集成实体死亡监听器，移除冗余代码
 * 修正：改进数据保存逻辑，确保符合保存要求
 */
@Mod(WanderingTraderExpressDelivery.MODID)
public class WanderingTraderExpressDelivery {
    public static final String MODID = "wandering_trader_express_delivery";
    public static final Logger LOGGER = LogUtils.getLogger();

    // 单例实例
    public static WanderingTraderExpressDelivery instance;

    // 管理器实例
    public static EventHandler eventHandler;
    public static TradeManager tradeManager;
    public static DeliveryManager deliveryManager;
    public static InterceptManager interceptManager;
    public static PendingDisappearanceManager pendingDisappearanceManager;
    public static PendingManagementManager pendingManagementManager;
    public static TimeCalculator timeCalculator;
    public static EntityDeathListener entityDeathListener;
    public static TradePriceManager tradePriceManager;

    // 服务器实例
    private MinecraftServer server;

    /**
     * 模组构造函数
     */
    public WanderingTraderExpressDelivery(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;

        // 注册通用设置
        modEventBus.addListener(this::commonSetup);

        // 初始化管理器
        initializeManagers();

        // 注册事件处理器
        registerEventHandlers(modEventBus);

        // 注册配置
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("流浪商人快递模组初始化完成");
    }

    /**
     * 初始化所有管理器
     */
    private void initializeManagers() {
        // 按依赖顺序初始化管理器
        timeCalculator = new TimeCalculator();
        pendingDisappearanceManager = new PendingDisappearanceManager();
        pendingManagementManager = new PendingManagementManager();
        tradeManager = new TradeManager();
        deliveryManager = new DeliveryManager();
        interceptManager = new InterceptManager();
        eventHandler = new EventHandler();
        entityDeathListener = new EntityDeathListener();
        tradePriceManager = new TradePriceManager(); // 新增

        LOGGER.info("所有管理器初始化完成");
    }

    /**
     * 注册事件处理器
     */
    private void registerEventHandlers(IEventBus modEventBus) {
        // 注册到NeoForge事件总线
        NeoForge.EVENT_BUS.register(timeCalculator);
        NeoForge.EVENT_BUS.register(eventHandler);
        NeoForge.EVENT_BUS.register(entityDeathListener);
        NeoForge.EVENT_BUS.register(new PlayerLoginListener());
        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("事件处理器注册完成");
    }

    /**
     * 通用设置
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("流浪商人快递模组通用设置完成");
    }

    /**
     * 服务器启动事件
     * 修正：加载所有需要持久化的数据
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.server = event.getServer();

        // 在加载数据之前清空所有数据
        LoggerUtil.info("🧹 清空所有管理器数据...");
        clearAllManagerData();

        // 初始化MOB配置管理器
        LOGGER.info("🎯 初始化劫匪MOB配置...");
        MobConfigManager.initializeMobPool();
        LoggerUtil.info(MobConfigManager.getMobPoolInfo());

        // 先清理可能损坏的数据文件
        LOGGER.info("🧹 清理损坏的数据文件...");
        deliveryManager.cleanupCorruptedData();
        tradeManager.cleanupCorruptedData();

        // 加载交易价格数据 - 新增
        LOGGER.info("📥 加载交易价格数据...");
        tradePriceManager.loadData();

        // 先加载交易列表数据
        LOGGER.info("📥 加载交易列表数据...");
        tradeManager.loadData();

        // 加载管理事件数据 - 新增
        LOGGER.info("📥 加载管理事件数据...");
        pendingManagementManager.loadData();

        // 最后加载消失事件数据（依赖于基础数据）
        LOGGER.info("📥 加载消失事件数据...");
        pendingDisappearanceManager.loadData();

        LOGGER.info("✅ 服务器启动数据加载流程完成");
    }



    /**
     * 服务器停止事件
     * 修正：保存所有需要持久化的数据，清除不需要的数据
     */
    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {

        // 保存交易价格数据 - 新增
        LOGGER.info("📥 保存交易价格数据...");
        tradePriceManager.saveData();

        // 先保存基础数据
        LOGGER.info("💾 保存交易列表数据...");
        tradeManager.saveData();

        LOGGER.info("💾 保存快递数据...");
        deliveryManager.saveData();

        // 保存管理事件数据 - 新增
        LOGGER.info("💾 保存管理事件数据...");
        pendingManagementManager.saveData();

        // 最后保存消失事件数据
        LOGGER.info("💾 保存消失事件数据...");
        pendingDisappearanceManager.saveData();

        LOGGER.info("✅ 所有持久化数据保存完成");

        // 清除非持久化数据
        LOGGER.info("🧹 清除非持久化数据...");
        pendingDisappearanceManager.clearNonPersistentData();
        pendingManagementManager.clearAllData();

        // 取消移动中的快递
        LOGGER.info("🚫 取消移动中的快递...");
        deliveryManager.cancelAllActiveDeliveries();

        LOGGER.info("✅ 服务器关闭数据保存流程完成");
    }

    /**
     * 获取服务器实例
     */
    public MinecraftServer getServer() {
        return server;
    }

    /**
     * 清空所有管理器数据
     */
    private void clearAllManagerData() {
        try {
            // 清空消失事件管理器数据
            pendingDisappearanceManager.clearAllData();

            // 清空管理事件管理器数据
            pendingManagementManager.clearAllData();

            // 清空交易管理器数据
            tradeManager.clearTradeData();

            // 清空快递管理器数据
            deliveryManager.activeDeliveries.clear();

            // 新增：清空交易价格管理器数据
            tradePriceManager.clearAllData();

            LoggerUtil.info("✅ 所有管理器数据已清空");
        } catch (Exception e) {
            LoggerUtil.error("❌ 清空管理器数据时发生错误", e);
        }
    }
}