package com.pnkxpl.gd_ts_uk;
import java.util.ArrayList;

import com.ibm.icu.impl.coll.UVector32;
import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.libJava.c_类型转换;
import com.pnkxpl.gd_ts_uk.libJava.c_前置计算;
import com.pnkxpl.gd_ts_uk.*;
//*import com.pnkxpl.gd_ts_uk.GsNg.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Vector;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.minecraft.world.InteractionHand.MAIN_HAND;

//import static com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery.MODID;

/*现象__(右键,每tick)会烧两次*/
/*用来__g(pl1)*/
public class UiJmHander {
  public static c_类型转换 类转 = new c_类型转换( );
  /// public static Config 设件 = new Config( );
  public static c_前置计算 前算 = new c_前置计算( );

  public static com.pnkxpl.c_跳_ts_tpac_跳.c_跳 跳 = new com.pnkxpl.c_跳_ts_tpac_跳.c_跳( );
  public static c_if亮Z烧怪 if亮Z烧怪 = new c_if亮Z烧怪( );
  public static c_放块在块前 放块在块前 = new c_放块在块前( );
  public static com.pnkxpl.gd_ts_uk.c_改attribute 改attr = new c_改attribute( );
  public static c_if有饥饿值Z回血 if有饥饿值ZAddRegeneration = new c_if有饥饿值Z回血( );
  public static c_改饥饿值消耗 改饥耗 = new c_改饥饿值消耗( );


  //!读件
  /*!记录__设置*/
  int tζ读confζ上1 = -1500, 设ζ读confζt隔 = 20;
  int 设ζ启ζif亮Z烧怪 = 1, 设ζ启ζ跳同tpA跳 = 1, 设ζ启ζ放块在块前 = 1, 设ζ启ζ改attr = 1, 设ζ启ζc_if有饥饿值Z回血 = 1;
  float FovModifier = 0;
  //!彳亍中
  /*!记录__设置*/

  /*!记录*/
  Minecraft mc1 = Minecraft.getInstance( );
  int 当t = 0;
  static Player pl1 = null;
  ItemStack istack主手 = null;
  int 主手物u = 0, 主手物uζ上1 = 0;//主手物uζ放块前=主手物uζ上1,主手物uζ放块后=主手物u
  /*不需`是否将检测ent`是否将烧*/
  public UiJmHander( ) {
    // mc1 = Minecraft.getInstance( );
  }
  public void 从configG量( ) { /*2026年2月27日17时40分34*/
    设ζ启ζif亮Z烧怪 = 类转.rI( Config.设ζ启ζif亮Z烧怪.get( ).get( 0 ) );
    设ζ启ζ跳同tpA跳 = 类转.rI( Config.设ζ启ζ跳同tpA跳.get( ).get( 0 ) );
    设ζ启ζ放块在块前 = 类转.rI( Config.设ζ启ζ放块在块前.get( ).get( 0 ) );
    设ζ启ζ改attr = 类转.rI( Config.设ζ启ζattribute.get( ).get( 0 ) );
    设ζ启ζc_if有饥饿值Z回血 = 类转.rI( Config.设ζ启ζc_if有饥饿值Z回血.get( ).get( 0 ) );
    FovModifier = Float.parseFloat( Config.设ζFovModifier.get( ).get( 0 ) );
  }/*void*/
  //!  PlayerInteractEvent$    EntityInteract	玩家交互事件－实体交互
  //!  PlayerInteractEvent$    EntityInteractSpecific	玩家交互事件－具体的实体交互	当玩家右键实体，双方都会触发
  //!  PlayerInteractEvent$    LeftClickBlock	玩家交互事件－左键方块
  //!  PlayerInteractEvent$    Lefc_跳lickEmpty	玩家交互事件－对空左键
  //!  PlayerInteractEvent$    RightClickBlock	玩家交互事件－右键方块
  //!  PlayerInteractEvent$    RightClickEmpty	玩家交互事件－对空右键
  //!  PlayerInteractEvent$    RightClickItem  玩家交互事件－右键物品
  //!  PlayerEvent$    PlayerLoggedInEvent	   玩家事件－玩家登录事件
  /*<ent>.tickCount 在自增*/
  /*ChunkTicketLevelUpdatedEvent =彳亍1次;    ServerTickEvent.Post =可; */
  @SubscribeEvent public void onTick( ServerTickEvent.Post 事 ) {/*2026年2月21日06时17分58*/
    ///if(/*!t隔,推迟彳亍*/pl1.tickCount > 在首几t不彳亍 ) {/*https://docs.neoforged.net/docs/entities/#mobcategory */}/*if*/
    if( pl1 == null ) { LOGGER.info( "pl1 =null" ); return; }/*if*/
    if( pl1.tickCount - tζ读confζ上1 >= 设ζ读confζt隔 ) {//!时隔够=>读conf
      tζ读confζ上1 = pl1.tickCount;
      从configG量( );
    }/*if*/
    /*?2026年2月22日01时19分43__原来是c,  2026年2月24日04时23分57__c需player*/
    ///pl1./*c*/displayClientMessage(Component.translatable("触tick", ""), false);
    {//!记
      当t = pl1.tickCount;
      istack主手 = pl1.getItemInHand( MAIN_HAND );//todo 2026年2月27日07时15分50__每t记录`会在放块后吗
    } ;

    ///pl1.causeFoodExhaustion( (float ) 0.01 );
    if( 设ζ启ζif亮Z烧怪 == 1 ) {
      if亮Z烧怪.gVEnt_检测entAIf亮Z烧怪( pl1, mc1 );
    }/*if*/
    if( 设ζ启ζ跳同tpA跳 == 1 ) {
      跳.ifY大yZ改是否已去更高( pl1 );
      ///跳.回定相yTp下( pl1, -1 );
    }/*if*/
    if( 设ζ启ζ改attr == 1 ) {
      改attr.读A改_无移速( pl1 );
      改attr.改移速( pl1 );
    }/*if*/
    if( 设ζ启ζc_if有饥饿值Z回血 == 1 ) {
      if有饥饿值ZAddRegeneration.读A改( pl1 );
    }/*if*/

  }/*void*/

  //*@EventBusSubscriber(modid = MODID )
  @SubscribeEvent public void PIEvζ右键点块( PlayerInteractEvent.RightClickBlock 事 ) {/*最初, 从( Wandering-Trader-Express-main  https://github.com/ddd575/Wandering-Trader-Express/tree/main )*/
    pl1 = 事.getEntity( );
    {//!记录
      //*主手物uζ上1 = 主手物u;
      主手物u = istack主手.getCount( );
    } ;
    if( 设ζ启ζ放块在块前 == 1 ) {//!放块在块前
      ///pl1./*c*/displayClientMessage( Component.translatable( "主手物uζ放块前", 主手物u ), false );
      放块在块前.PIEvζ右键点块ζ放块在块前( 事 );
    }/*if*/
  }/*void*/

  @SubscribeEvent public void PIEvζent交互( PlayerInteractEvent.EntityInteract 事 ) {/*2026年2月21日07时26分33*/
    if( pl1 == null ) { return; }/*if*/
    ///Entity 当ent = 事.getEntity( ); if( pl1 == 当ent ) { }/*if*/
    改饥耗.饥加( pl1, 1, 0 );/*2026年3月2日05时12分35*/
  }/*void*/
  @SubscribeEvent
  public void PIEvζent交互Specific( PlayerInteractEvent.EntityInteractSpecific 事 ) {
    if( pl1 == null ) { return; }/*if*/
  }/*void*/
  //(事.getPos,pl1.getOnPos)=块坐(不是`空气坐)    BlockPos不能==
  @SubscribeEvent public void PIEvζ左键点块( PlayerInteractEvent.LeftClickBlock 事 ) {
    pl1 = 事.getEntity( );
    ///if( Config.设ζ启ζco.get( ).get( 0 ) ) { pl1./*c*/displayClientMessage( Component.translatable( "pl1 !=null", "" ), false ); }/*if*/
    ///pl1./*c*/displayClientMessage( Component.translatable( "co1",  事.getPos( ).getY() ), true );
    ///pl1./*c*/displayClientMessage( Component.translatable( "co1",  pl1.getOnPos().getY() ), true );
    ///pl1./*c*/displayClientMessage( Component.translatable( "co1" ,""  ), true );
    if( 前算.是否坐同( 事.getPos( ), pl1.getOnPos( ) ) == 1 ) {
      ///pl1./*c*/displayClientMessage( Component.translatable( "co1", "" ), false );
    }/*if*/

  }/*void*/
  @SubscribeEvent public void PEveζ玩家登陆事( PlayerEvent.PlayerLoggedInEvent 事 ) {/*2026年2月22日03时00分25*/
    pl1 = 事.getEntity( );
  }/*void*/
  /*没(sec_跳anceled)*/
  //@SubscribeEvent(priority = EventPriority.LOWEST)
  @SubscribeEvent public void LEζ生物跳跃事( LivingEvent.LivingJumpEvent 事 ) {/*2026年2月24日13时34分23*/
    if( pl1 == null ) { return; }/*if*/
    Entity 当ent = 事.getEntity( ); if( pl1 != 当ent ) { return; }/*if*/
    跳.读confATp坐加(  pl1 );
  }/*void*/
  //*@SubscribeEvent
  public static void on移动输入更新( MovementInputUpdateEvent 事 ) {/*2026年3月2日05时48分53*/
    if( pl1 == null ) { return; }/*if*/
    Entity 当ent = 事.getEntity( ); if( pl1 == 当ent ) {
      Input input = 事.getInput( );
      if( pl1.isSprinting( ) ) { //!疾跑
        input.forwardImpulse *= 2.0F;
        input.leftImpulse *= 2.0F;
      }/*if*/
    }/*if*/
  }/*void*/
  //*@SubscribeEvent public void PIEvζ生物受伤( LivingDamageEvent 事 ) {
  //*  if( pl1 == null ) { return; }/*if*/
  //*  Entity 当ent = 事.getEntity( ); if( pl1 == 当ent ) {
  //*  }/*if*/
  //*  return;
  //*}/*void*/
  @SubscribeEvent public void onComputeFovModifier( ComputeFovModifierEvent 事 ) {
    事.setNewFovModifier( FovModifier ); //70*0.57≈40，实现缩放效果
  }/*void*/

}/*class*/