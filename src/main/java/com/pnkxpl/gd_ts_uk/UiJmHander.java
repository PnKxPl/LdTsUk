package com.pnkxpl.gd_ts_uk;
import java.util.ArrayList;

import com.ibm.icu.impl.coll.UVector32;
import com.pnkxpl.gd_ts_uk.core.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
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

//import static com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery.MODID;

/*现象__(右键,每tick)会烧两次; 先(右键)启动*/
public class UiJmHander {
  public static Config 设件 = new Config( );
  public static com.pnkxpl.tc_ts_tpatc.Tc 跳 = new com.pnkxpl.tc_ts_tpatc.Tc( );
  public static ifLdZUkGy if亮Z烧怪 = new ifLdZUkGy( );
  //!
  /*!记录__设置*/

  //!彳亍中
  /*!记录__设置*/

  /*!记录*/
  Player 当pl = null;


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
    ///if(/*!t隔,推迟彳亍*/当pl.tickCount > 在首几t不彳亍 ) {/*https://docs.neoforged.net/docs/entities/#mobcategory */}/*if*/
    if( 当pl == null ) { return; }/*if*/
    /*?2026年2月22日01时19分43__原来是c,  2026年2月24日04时23分57__c需player*/
    ///当pl./*c*/displayClientMessage(Component.translatable("触tick", ""), false);
    if亮Z烧怪.if时隔Z_if亮Z烧( 当pl);
  }/*void*/
  /*用来__g(当pl),其他的没有做到*/
  //*@EventBusSubscriber(modid = MODID )
  @SubscribeEvent public void PIEvζRightClickBlock( PlayerInteractEvent.RightClickBlock event ) {/*最初, 从( Wandering-Trader-Express-main  https://github.com/ddd575/Wandering-Trader-Express/tree/main )*/
    当pl = event.getEntity( );
    /*?没用__不立即烧; 不烧; */
    //*int 当plTi = 当pl.tickCount;
    //*/*c*/当pl.displayClientMessage(Component.translatable("当pl.tickCount", 当pl.tickCount), false);/*attentionBar=文本还是提示*/
    //*/*立即彳亍,记录tick(保证不连彳亍)*/
    //*if (/*时隔>~=>触*/是否u1减u2大同u3(当plTi, 右键烧のplayerTickζ上1, 设ζt隔ζ烧怪) == 1) {
    //*if亮Z烧();
    //*  //*if亮Z烧(设ζt隔ζ烧怪, rUの除余(当plTi, 设ζt隔ζ烧怪));//?竟不是`立即彳亍
    //*  右键烧のplayerTickζ上1 = 当plTi;
    //*}/*if*/
    //*if(/*(当<上1)=>上1=0*/当plTi<右键烧のplayerTickζ上1){
    //*  右键烧のplayerTickζ上1 = 0;
    //*}/*if*/
  }/*void*/
  @SubscribeEvent public void PIEvζent交互( PlayerInteractEvent.EntityInteract event ) {/*2026年2月21日07时26分33*/
    当pl = event.getEntity( );
  }/*void*/
  @SubscribeEvent public void PIEvζ左键点块( PlayerInteractEvent.LeftClickBlock event ) {
    当pl = event.getEntity( );
  }/*void*/
  @SubscribeEvent public void PEveζ玩家登陆事( PlayerEvent.PlayerLoggedInEvent event ) {/*2026年2月22日03时00分25*/
    当pl = event.getEntity( );
  }/*void*/
  /*没(setCanceled)*/
  //@SubscribeEvent(priority = EventPriority.LOWEST)
  @SubscribeEvent public void LEζ生物跳跃事( LivingEvent.LivingJumpEvent 事 ) {/*2026年2月24日13时34分23*/
    Entity 当ent = 事.getEntity( ); if( 当pl != 当ent ) { return; }/*if*/
    跳.s无gravATp到上(当pl,1);
  }/*void*/

}/*class*/