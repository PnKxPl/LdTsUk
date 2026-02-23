package com.pnkxpl.gd_ts_uk.core;
import com.pnkxpl.gd_ts_uk.*;
import com.pnkxpl.gd_ts_uk.UiJmHander;
//import com.pnkxpl.gd_ts_uk.delivery.DeliveryManager;
//import com.pnkxpl.gd_ts_uk.manager.*;
//import com.pnkxpl.gd_ts_uk.manager.*;
//import com.pnkxpl.gd_ts_uk.pending.PendingDisappearanceManager;
//import com.pnkxpl.gd_ts_uk.pending.PendingManagementManager;
//import com.pnkxpl.gd_ts_uk.utils.EntityDeathListener;
//import com.pnkxpl.gd_ts_uk.utils.LoggerUtil;
//import com.pnkxpl.gd_ts_uk.utils.PlayerLoginListener;
import net.neoforged.fml.common.EventBusSubscriber;
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
//@Mod(WanderingTraderExpressDelivery.MODID)
//@EventBusSubscriber(WanderingTraderExpressDelivery.MODID )
@Mod("ld_ts_uk" )
public class WanderingTraderExpressDelivery {
  //!2026年2月21日23时49分22__是在这设置吗？
  public static final String MODID = "ld_ts_uk";
  public static UiJmHander eventHandler = new UiJmHander();
  /*用来__LOGGER.info*/
  ///public static final Logger LOGGER = LogUtils.getLogger();
  /**
   * 模组构造函数
   */
  public WanderingTraderExpressDelivery(IEventBus modEventBus, ModContainer modContainer) {
    //instance = this;

    // 注册通用设置
    //modEventBus.addListener(this::commonSetup);
    //初始化管理器();
    NeoForge.EVENT_BUS.register(eventHandler);
    //注册事件处理器(modEventBus);
    // 注册配置
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    //LOGGER.info("流浪商人快递模组初始化完成");
  }

  /**
   * 初始化所有管理器
   */

  private void 初始化管理器() {
    //按依赖顺序初始化管理器sheng
    //timeCalculator = new TimeCalculator();
    //pendingDisappearanceManager = new PendingDisappearanceManager();
    //pendingManagementManager = new PendingManagementManager();
    //tradeManager = new TradeManager();
    //deliveryManager = new DeliveryManager();
    //interceptManager = new InterceptManager();
    eventHandler = new UiJmHander();
    //entityDeathListener = new EntityDeathListener();
    //tradePriceManager = new TradePriceManager(); // 新增

    /// LOGGER.info("所有管理器初始化完成");
  }

  /**
   * 注册事件处理器
   */
  /*2026年2月21日23时59分21__没有这=不闪退了*/
  private void 注册事件处理器(IEventBus modEventBus) {
    // 注册到NeoForge事件总线
    /// NeoForge.EVENT_BUS.register(timeCalculator);
    ///
    NeoForge.EVENT_BUS.register(eventHandler);
    //NeoForge.EVENT_BUS.register();
    /// NeoForge.EVENT_BUS.register(entityDeathListener);
    /// NeoForge.EVENT_BUS.register(new PlayerLoginListener());
    NeoForge.EVENT_BUS.register(this );
    ///
    /// LOGGER.info("事件处理器注册完成");
  }


}