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

/*** 从这开 - 流浪商人快递系统 */
@Mod( WanderingTraderExpressDelivery.MODID ) //*@EventBusSubscriber(WanderingTraderExpressDelivery.MODID )
///@Mod("ld_ts_uk" )
public class WanderingTraderExpressDelivery {
  //!2026年2月21日23时49分22__是在这设置吗？
  public static final String MODID = "ld_ts_uk";
  //!单例实例
  public static WanderingTraderExpressDelivery instance;
  //!管理器实例
  public static UiJmHander on事 = new UiJmHander();/*2026年2月25日03时12分25__在读conf前初始化了,内有初(Config)*/
  //!服务器实例
  private MinecraftServer server;
  /*用来__LOGGER.info*/
  ///public static final Logger LOGGER = LogUtils.getLogger();
  public WanderingTraderExpressDelivery( IEventBus modEventBus, ModContainer modContainer ) {
    instance = this;
    //modEventBus.addListener(this::commonSetup);
    初始化( );
    注册事件( modEventBus );
    // 注册配置
    modContainer.registerConfig( ModConfig.Type.COMMON, Config.SPEC );/*无这=进存闪退, 可能不是读件*/
  }

  private void 初始化( ) {
    //timeCalculator = new TimeCalculator();
    //pendingDisappearanceManager = new PendingDisappearanceManager();
    //pendingManagementManager = new PendingManagementManager();
    //tradeManager = new TradeManager();
    //deliveryManager = new DeliveryManager();
    //interceptManager = new InterceptManager();
    ///on事 = new UiJmHander( ); //*on事.从configG量( );
    //entityDeathListener = new EntityDeathListener();
    //tradePriceManager = new TradePriceManager(); // 新增
  }

  /*不用=不能注册*/
  private void 注册事件( IEventBus modEventBus ) {
    // 注册到NeoForge事件总线
    /// NeoForge.EVENT_BUS.register(timeCalculator);
    ///
    NeoForge.EVENT_BUS.register( on事 );
    //NeoForge.EVENT_BUS.register();
    /// NeoForge.EVENT_BUS.register(entityDeathListener);
    /// NeoForge.EVENT_BUS.register(new PlayerLoginListener());
    NeoForge.EVENT_BUS.register( this );
  }

  @SubscribeEvent
  public void on服务器启动(ServerStartingEvent event) {
    this.server = event.getServer();
  }
}