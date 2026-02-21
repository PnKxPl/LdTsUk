package com.pnkxpl.gd_ts_uk;

//import dev.architectury.injectables.annotations.ExpectPlatform;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.phys.AABB;

//package net.minecraft.world.level.lighting;
//package com.ddd.wanderingtraderexpressdelivery;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

//import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.block;

/**
 * 事件处理器 - //!处理玩家与钟的交互事件
 * 修复：彻底解决需要多次点击才能打开GUI的问题
 * 修正：添加防重复机制，防止同一tick内重复处理同一玩家的事件
 * 修改：支持自定义消耗物品，显示具体需求物品
 */
//!右键,打开交易选择GUI
/*现象__(右键,每tick)会烧两次; 先(右键)启动*/
public class EventHandler { //implements EventHandler1 {
  Player player = null;//*=Minecraft.getInstance().player;
  int 右键烧のplayerTickζ上1 = 0;
  int 设ζtick隔 = 20;

  boolean 是否ent敌对(Entity entity) {/*从(围火无怪NoHostilesAroundCampfire);2026年2月21日03时25分33*/
    if (entity.getType().getCategory().equals(MobCategory.MONSTER)) {
      return true;
    }/*if*/
    return false;
  }/*boolean*/

  public void if光Z烧() {/*2026年2月21日07时12分19*/
    Level lev1 = player.level();
    Minecraft mc1 = Minecraft.getInstance();
    /*亮检ζ方块*/
    LayerLightEventListener LdJm_FhKy = mc1.level.getLightEngine().getLayerListener(LightLayer.BLOCK);

    BlockPos 玩家坐标 = player.blockPosition();
    ///*c*/player.displayClientMessage(Component.translatable("检测玩家周围", ""), false);
    for (/*检测玩家周围*/Entity ent1 : lev1.getEntities(null, new AABB(玩家坐标.getX() - 192, 玩家坐标.getY() - 192, 玩家坐标.getZ() - 192, 玩家坐标.getX() + 192, 玩家坐标.getY() + 192, 玩家坐标.getZ() + 192))) {
      ///*c*/player.displayClientMessage(Component.translatable("是否ent敌对", ""), false);
      if (是否ent敌对(ent1)) {
        BlockPos 敌怪坐标 = ent1.blockPosition();
        /*g亮度*/
        int 怪块亮级 = LdJm_FhKy.getLightValue(敌怪坐标);
        if (/*见光=>烧*/怪块亮级 > 0) {
          //ent1.level().addParticle(...);
          ///if (/*已着火=>重新着火*/ent1.isOnFire()) {
          ///  ent1.clearFire();
          ///}/*if*/
          ent1.setRemainingFireTicks(40);/*连续`和重新=连续扣血,=不(点1下烧1下)*/
          if (/*光级>1=>攻击*/怪块亮级 > 1) {
            ent1.hurt(player.damageSources().inFire(), 怪块亮级 - 1);/*从(豆包)*/
          }/*if*/
        }/*if*/
      }/*if*/
    }/*for*/
  }/*void*/

  /*方__余同这Z触`用来(保证立即彳亍),但不能*/
  public void if时隔Z_if光Z烧(int tick隔, int 余同这Z触) {/*2026年2月21日06时47分42*/
    if (player == null) {
      return;
    }/*if*/
    if (/*!时隔*/player.tickCount % tick隔 == 余同这Z触) {/*https://docs.neoforged.net/docs/entities/#mobcategory */
      if光Z烧();
    } else {/*非特时=>retu*/
      return;
    }/*if*/
  }/*void*/

  @SubscribeEvent
  /*tickCount不是(最大=19),自增*/
  /*ChunkTicketLevelUpdatedEvent =彳亍1次;    ServerTickEvent.Post =可; */
  public void onTick(ServerTickEvent.Post 事) {/*2026年2月21日06时17分58*/
    if时隔Z_if光Z烧(设ζtick隔, 0);
  }/*void*/

  /*用来__保证(右键立即彳亍, 不是每次右键都彳亍)*/
  public int 是否u1减u2大同u3(int u1, int u2, int u3) {/*2026年2月21日06时59分36*/
    if ((u1 - u2) >= u3) {
      return 1;
    }/*if*/
    return 0;
  }/*void*/

  /*用来__从(player.tickCount)g(时隔)*/
  public int rUの除余(int u1, int 除) {/*2026年2月21日06时40分35*/
    int iR = u1 % 除;
    ///*c*/player.displayClientMessage(Component.translatable("iR", iR), false);/*attentionBar=文本还是提示*/
    return iR;
  }/*void*/

  /*用来__g(player),其他的没有做到*/
  @SubscribeEvent
  public void PIEvζRightClickBlock(PlayerInteractEvent.RightClickBlock event) {/*最初, 从( Wandering-Trader-Express-main  https://github.com/ddd575/Wandering-Trader-Express/tree/main )*/
    player = event.getEntity();
    /*?没用__不立即烧; 不烧; */
    //*int 当plTi = player.tickCount;
    //*/*c*/player.displayClientMessage(Component.translatable("player.tickCount", player.tickCount), false);/*attentionBar=文本还是提示*/
    //*/*立即彳亍,记录tick(保证不连彳亍)*/
    //*if (/*时隔>~=>触*/是否u1减u2大同u3(当plTi, 右键烧のplayerTickζ上1, 设ζtick隔) == 1) {
    //*if光Z烧();
    //*  //*if光Z烧(设ζtick隔, rUの除余(当plTi, 设ζtick隔));//?竟不是`立即彳亍
    //*  右键烧のplayerTickζ上1 = 当plTi;
    //*}/*if*/
    //*if(/*(当<上1)=>上1=0*/当plTi<右键烧のplayerTickζ上1){
    //*  右键烧のplayerTickζ上1 = 0;
    //*}/*if*/
  }/*void*/

  //PlayerInteractEvent$    EntityInteract	玩家交互事件－实体交互
  //PlayerInteractEvent$    EntityInteractSpecific	玩家交互事件－具体的实体交互	当玩家右键实体，双方都会触发
  //PlayerInteractEvent$    LeftClickBlock	玩家交互事件－左键方块
  //PlayerInteractEvent$    LeftClickEmpty	玩家交互事件－对空左键
  //PlayerInteractEvent$    RightClickBlock	玩家交互事件－右键方块
  //PlayerInteractEvent$    RightClickEmpty	玩家交互事件－对空右键
  //PlayerInteractEvent$    RightClickItem  玩家交互事件－右键物品
  @SubscribeEvent
  public void PIEvζEntityInteract(PlayerInteractEvent.EntityInteract event) {/*2026年2月21日07时26分33*/
    player = event.getEntity();
  }/*void*/

  public void PIEvζEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
    player = event.getEntity();
  }/*void*/

  public void PIEvζLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
    player = event.getEntity();
  }/*void*/

  public void PIEvζLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
    player = event.getEntity();
  }/*void*/

  public void PIEvζRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
    player = event.getEntity();
  }/*void*/

  public void PIEvζRightClickItem(PlayerInteractEvent.RightClickItem event) {
    player = event.getEntity();
  }/*void*/

  //!这是`原字
  ///     // 基于时间戳的防重复 - 记录玩家最后一次处理的时间
  ///     private final Map<UUID, Long> lastProcessTime = new WeakHashMap<>();
  ///     private static final long MIN_PROCESS_INTERVAL = 100; // 最小处理间隔100毫秒
  ///
  ///     //!右键(钟)后彳亍
  ///     @SubscribeEvent
  ///     public void PlayerInteractEventζRightClickBlock1(PlayerInteractEvent.RightClickBlock event) {
  ///       player = event.getEntity();
  ///       ///onTick();
  ///       Level level = player.level();
  ///
  ///       // 只在服务器端处理
  ///       if (level.isClientSide()) {
  ///         return;
  ///       }
  ///
  ///       // 检查所有交互条件
  ///       if (!isValidInteraction(player, level, event)) {
  ///         return;
  ///       }
  ///
  ///       UUID playerId = player.getUUID();
  ///       long currentTime = System.currentTimeMillis();
  ///
  ///       // 防重复检查 - 检查处理时间间隔
  ///       synchronized (lastProcessTime) {
  ///         Long lastTime = lastProcessTime.get(playerId);
  ///         if (/*!时差<100毫秒*/lastTime != null && (currentTime - lastTime) < MIN_PROCESS_INTERVAL) {
  ///           LoggerUtil.debug("跳过快速重复点击: 玩家={}, 间隔={}ms",
  ///               player.getName().getString(), currentTime - lastTime);
  ///           return;
  ///         }/*if*/
  ///         lastProcessTime.put(playerId, currentTime);
  ///       }/*synchronized*/
  ///
  ///       // 取消事件，防止钟被正常使用
  ///       event.setCanceled(true);
  ///
  ///       //!立即打开交易选择GUI
  ///       LoggerUtil.info("玩家尝试打开交易GUI: 玩家={}", player.getName().getString());
  ///       WanderingTraderExpressDelivery.tradeManager.openTradeSelectionGUI(player);
  ///     }/*void*/
  ////    /!右键方块后彳亍(右键空气不行)
  ///
  ///     /**
  ///      * 验证交互是否有效
  ///      * 修复：简化条件判断，确保所有条件正确检查
  ///      * ✅ 修改：支持自定义消耗物品，显示具体需求物品
  ///      */
  ///     private boolean isValidInteraction(Player player, Level level, PlayerInteractEvent.RightClickBlock event) {
  ///       // 检查主世界
  ///       if (!level.dimension().equals(Level.OVERWORLD)) {
  ///         return false;
  ///       }
  ///
  ///       // 检查钟方块
  ///       if (!event.getLevel().getBlockState(event.getPos()).is(Blocks.BELL)) {
  ///         return false;
  ///       }
  ///
  ///       // 检查蹲下状态
  ///       if (!player.isCrouching()) {
  ///         return false;
  ///       }
  ///
  ///       // ✅ 修改：检查玩家是否持有配置要求的物品
  ///       if (!hasRequiredCostItem(player)) {
  ///         // 消息将在hasRequiredCostItem方法中发送
  ///         return false;
  ///       }
  ///
  ///       LoggerUtil.debug("交互验证通过: 玩家={}", player.getName().getString());
  ///       return true;
  ///     }
  ///
  ///     /**
  ///      * ✅ 修改：检查玩家是否持有配置要求的物品
  ///      * 如果玩家没有持有任何配置中的物品，发送详细需求消息
  ///      *
  ///      * @param player 玩家实例
  ///      * @return 是否持有要求的物品
  ///      */
  ///     private boolean hasRequiredCostItem(Player player) {
  ///       // 使用Config中的辅助方法检查玩家是否持有任何配置物品
  ///       if (Config.hasAnyCostItem(player)) {
  ///         return true;
  ///       }
  ///
  ///       // 玩家没有持有任何配置物品，发送详细需求消息
  ///       String requiredItems = Config.getRequiredCostItemsDisplayString();
  ///       player.displayClientMessage(
  ///           Component.translatable("message.wandering_trader_express_delivery.need_cost_item_detail", requiredItems),
  ///           false
  ///       );
  ///
  ///       LoggerUtil.debug("玩家未持有配置中指定的消耗物品，需要: {}", requiredItems);
  ///       return false;
  ///     }


}/*class*/
