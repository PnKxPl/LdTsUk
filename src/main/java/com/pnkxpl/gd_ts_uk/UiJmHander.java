package com.pnkxpl.gd_ts_uk;
import java.util.ArrayList;

import com.ibm.icu.impl.coll.UVector32;
import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.libJava.QmViJiSr;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Vector;

import static net.minecraft.world.InteractionHand.MAIN_HAND;

//import static com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery.MODID;

/*现象__(右键,每tick)会烧两次; 先(右键)启动*/
public class UiJmHander {
  public static Config 设件 = new Config( );
  public static QmViJiSr 前算 = new QmViJiSr( );

  public static com.pnkxpl.tc_ts_tpatc.Tc 跳 = new com.pnkxpl.tc_ts_tpatc.Tc( );
  public static ifLdZUkGy if亮Z烧怪 = new ifLdZUkGy( );
  public static com.pnkxpl.tc_ts_tpatc.FhKyZlKyQm 放块在块前 = new com.pnkxpl.tc_ts_tpatc.FhKyZlKyQm( );
  //!读件
  /*!记录__设置*/

  //!彳亍中
  /*!记录__设置*/

  /*!记录*/
  int 当t=0;
  Player pl1 = null;
  ItemStack istack主手 = null;
  int 主手物u = 0, 主手物uζ上1 = 0;//主手物uζ放块前=主手物uζ上1,主手物uζ放块后=主手物u
  /*不需`是否将检测ent`是否将烧*/
  public UiJmHander( ) {
  }
  public void 从configG量( ) {

  }/*void*/


  /*
  PlayerInteractEvent$    EntityInteract	玩家交互事件－实体交互
  PlayerInteractEvent$    EntityInteractSpecific	玩家交互事件－具体的实体交互	当玩家右键实体，双方都会触发
  PlayerInteractEvent$    LeftClickBlock	玩家交互事件－左键方块
  PlayerInteractEvent$    LeftClickEmpty	玩家交互事件－对空左键
  PlayerInteractEvent$    RightClickBlock	玩家交互事件－右键方块
  PlayerInteractEvent$    RightClickEmpty	玩家交互事件－对空右键
  PlayerInteractEvent$    RightClickItem  玩家交互事件－右键物品
  PlayerEvent$    PlayerLoggedInEvent	   玩家事件－玩家登录事件
  */
  /*<ent>.tickCount 在自增*/
  /*ChunkTicketLevelUpdatedEvent =彳亍1次;    ServerTickEvent.Post =可; */
  @SubscribeEvent public void onTick( ServerTickEvent.Post 事 ) {/*2026年2月21日06时17分58*/
    ///if(/*!t隔,推迟彳亍*/pl1.tickCount > 在首几t不彳亍 ) {/*https://docs.neoforged.net/docs/entities/#mobcategory */}/*if*/
    if( pl1 == null ) { return; }/*if*/
    /*?2026年2月22日01时19分43__原来是c,  2026年2月24日04时23分57__c需player*/
    ///pl1./*c*/displayClientMessage(Component.translatable("触tick", ""), false);
    {//!记
      当t = pl1.tickCount;
      istack主手 = pl1.getItemInHand( MAIN_HAND );//todo 2026年2月27日07时15分50__每t记录`会在放块后吗
    } ;

    if亮Z烧怪.if时隔Z_if亮Z烧( pl1 );
    {//!跳
      跳.ifY大yZ改是否已去更高( pl1 );
      ///跳.回定相yTp下( pl1, -1 );
    } ;
  }/*void*/
  /*用来__g(pl1),其他的没有做到*/
  //*@EventBusSubscriber(modid = MODID )
  @SubscribeEvent public void PIEvζ右键点块( PlayerInteractEvent.RightClickBlock 事 ) {/*最初, 从( Wandering-Trader-Express-main  https://github.com/ddd575/Wandering-Trader-Express/tree/main )*/
    pl1 = 事.getEntity( );
    {//!记录
      //*主手物uζ上1 = 主手物u;
      主手物u = istack主手.getCount( );
    } ;
    {//!放块在块前
      pl1./*c*/displayClientMessage( Component.translatable( "主手物uζ放块前", 主手物u ), false );
      放块在块前.PIEvζ右键点块ζ放块在块前( 事 );
    } ;
  }/*void*/

  @SubscribeEvent public void PIEvζent交互( PlayerInteractEvent.EntityInteract event ) {/*2026年2月21日07时26分33*/
    ///pl1 = event.getEntity( );
  }/*void*/
  //(事.getPos,pl1.getOnPos)=块坐(不是`空气坐)    BlockPos不能==
  @SubscribeEvent public void PIEvζ左键点块( PlayerInteractEvent.LeftClickBlock 事 ) {
    pl1 = 事.getEntity( );
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
  /*没(setCanceled)*/
  //@SubscribeEvent(priority = EventPriority.LOWEST)
  @SubscribeEvent public void LEζ生物跳跃事( LivingEvent.LivingJumpEvent 事 ) {/*2026年2月24日13时34分23*/
    Entity 当ent = 事.getEntity( ); if( pl1 != 当ent ) { return; }/*if*/
    跳.s无gravATp到上( pl1, 1 );
  }/*void*/


}/*class*/