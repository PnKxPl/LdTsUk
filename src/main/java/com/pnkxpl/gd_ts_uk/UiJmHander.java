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
  public static Tc 跳 = new Tc( );
  public static Config 设件 = new Config( );
  //!从设件
  /*!记录__设置*/
  int if亮大这Z烧怪=6, 出伤ζ亮减 = 10/*7__6(-1),7(开`出伤)*/, 出伤乘 = 4;
  /*名__t隔=tick隔*//*300=15s,20=1s*/
  int 在首几t不彳亍 = 15, 设ζt隔ζ检ent = 20, 设ζt隔ζ烧怪 = 20;
  /*不知`单位(是(1个`或半个)块)*/
  int 检rX = 64, 检rY = 32, 检rZ = 64;
  //!彳亍中
  /*!记录__设置*/

  /*!记录*/
  Player 本pl = null;
  BlockPos 玩家坐标 = null;
  Level lev1 = null;
  LayerLightEventListener LdJm_FhKy/*亮检ζ方块*/ = null;
  BlockPos 敌怪坐标;
  int 怪块亮级;
  int 右键烧のplayerTickζ上1 = 0;
  //*Vector<Entity> vEnt1=null;
  //*ArrayList< Entity > vEntζ周围 = null, vEntζmonster = null, vEntζ敌 = null;int vEntζmonsterS = 0;
  ArrayList< BlockPos > vEnt坐标ζ周围 = null, vEnt坐标ζmonster = null, vEnt坐标ζ敌 = null;
  int vEnt坐标ζmonsterS = 0;
  int 记t点ζ检entζ上1 = -1500, 记t点ζ烧怪ζ上1 = -1500;
  int 记tζsetNoGravity = -1500, 几t后恢复grav = 10;

  /*不需`是否将检测ent`是否将烧*/
  public UiJmHander( ) {
  }
  public void 从configG量( ) {/*2026年2月25日03时16分44*/
    ///出伤ζ亮减 = Config.r出伤ζ亮减( ); 出伤乘 = Config.r出伤乘( );
    ///出伤ζ亮减 = 设件.r出伤ζ亮减( ); 出伤乘 = 设件.r出伤乘( );
    在首几t不彳亍 = 设件.在首几t不彳亍.get( );
    if亮大这Z烧怪=设件.if亮大这Z烧怪.get();
    出伤ζ亮减 = 设件.出伤ζ亮减.get( ); 出伤乘 = 设件.出伤乘.get( );
    设ζt隔ζ检ent = 设件.设ζt隔ζ检ent.get( );
    检rX = 设件.检rX.get( );
    检rZ = 设件.检rZ.get( );
    检rY = 设件.检rY.get( );
    ///本pl./*c*/displayClientMessage( Component.translatable( "从configG量 <--出伤乘", 出伤乘 ), false );

  }/*void*/
  boolean 是否entMonster( Entity ent1 ) {/*从(围火无怪NoHostilesAroundCampfire);2026年2月21日03时25分33*/
    if( ent1.getType( ).getCategory( ).equals( MobCategory.MONSTER ) ) { return true; }/*if*/
    return false;
  }/*boolean*/
  /*没用*/
  /*方__燃烧,if亮ZHurt*/
  public void if亮Z烧怪(/*入*/int 怪块亮级,/*出*/Entity ent1 ) {/*2026年2月21日18时11分01*/
    if(/*见光=>烧*/怪块亮级 > if亮大这Z烧怪 ) {
      if(/*已着火=>删火*/ent1.isOnFire( ) ) { ent1.clearFire( ); }/*if*/
      ent1.setRemainingFireTicks( 40 );/*连续`和重新=连续扣血,=不(点1下烧1下)*/ if(/*光级>1=>攻击*/怪块亮级 > 1 ) {
        {//!出伤
          ///*c*/本pl.displayClientMessage(Component.translatable("触hurt", ""), false);
          ///ent1.hurt( 本pl.damageSources( ).inFire( ), (怪块亮级 - 出伤ζ亮减 )*出伤乘);/*烧=不打坚守者; 从(豆包)*/
          ///ent1.hurt( 本pl.damageSources( ).playerAttack( 本pl ), (怪块亮级 - 出伤ζ亮减 )*出伤乘 );
          ///ent1.hurt( 本pl.damageSources( ).dragonBreath(  ), (怪块亮级 - 出伤ζ亮减 )*出伤乘 );
          ///ent1.hurt( ent1.damageSources( ).dragonBreath(  ), (怪块亮级 - 出伤ζ亮减 )*出伤乘 );
          ///ent1.hurt( 本pl.damageSources( ).anvil( ent1 ), (怪块亮级 - 出伤ζ亮减 )*出伤乘 );
          ent1.hurt( ent1.damageSources( ).inWall( ), ( 怪块亮级 - 出伤ζ亮减 ) * 出伤乘 );
        } ;
      }/*if*/
    }/*if*/
  }/*void*/
  /*!(forAGVEnt --> if亮Z烧怪)或(forVEnt --> if亮Z烧怪)*/
  public void gVEnt_检测entAIf亮Z烧怪( ) {/*2026年2月21日07时12分19*/
    int 当t = 本pl.tickCount;
    ///if(/*!恢复主角grav*/当t - 记tζsetNoGravity >= 10 ) {
    ///  本pl.setNoGravity( false );
    ///}/*if*/
    if(/*!时隔够=>将检*/当t - 记t点ζ检entζ上1 >= 设ζt隔ζ检ent ) {
      从configG量( );
      ///本pl./*c*/displayClientMessage( Component.translatable( "forAGVEnt --> if亮Z烧怪", "" ), false );
      玩家坐标 = 本pl.blockPosition( );
      //*vEntζ周围 = null; vEntζmonster = null; vEntζ敌 = null; vEntζmonsterS = 0;
      vEnt坐标ζ周围 = null; vEnt坐标ζmonster = null; vEnt坐标ζ敌 = null; vEnt坐标ζmonsterS = 0;
      for(/*检测玩家周围*/Entity 当ent : lev1.getEntities( null/*todo 2026年2月23日23时05分31__这书啥*/
          , new AABB( 玩家坐标.getX( ) - 检rX, 玩家坐标.getY( ) - 检rY, 玩家坐标.getZ( ) - 检rZ, 玩家坐标.getX( ) + 检rX, 玩家坐标.getY( ) + 检rY, 玩家坐标.getZ( ) + 检rZ ) ) ) {
        //*vEntζ周围.add( 当ent );
        if( 是否entMonster( 当ent ) ) {
          敌怪坐标 = 当ent.blockPosition( );
          //*vEnt坐标ζmonster.  add( 敌怪坐标 ); vEnt坐标ζmonsterS++;/*2026年2月24日04时36分09*/
          怪块亮级/*g亮度*/ = LdJm_FhKy.getLightValue( 敌怪坐标 );
          {/*测试*/
            //在0亮Z对怪出负1伤( 怪块亮级, 当ent );
          } ;
          if亮Z烧怪( 怪块亮级, 当ent );
          if(/*!恢复entのgrav*/当t - 记tζsetNoGravity >= 10 ) {
            当ent.setNoGravity( false );
          }/*if*/
        }/*if*/
      }/*for*/ ;
      /*!彳亍后*/
      记t点ζ检entζ上1 = 当t; ///是否将检测ent=0;
    } else {/*2026年2月24日03时42分23*/
      //*if(/*!时隔够=>将烧*/当t - 记t点ζ烧怪ζ上1 >= 设ζt隔ζ烧怪 ) {
      //*  本pl./*c*/displayClientMessage( Component.translatable( "forVEnt --> if亮Z烧怪", "" ), false );
      //*  for( int 一 =0; 一< vEntζmonsterS;一++ ) {
      //*    Entity 当ent =vEntζmonster.get(一);//*[一];
      //*    BlockPos 敌怪坐标 = 当ent.blockPosition( );
      //*    int /*g亮度*/怪块亮级 = LdJm_FhKy.getLightValue( 敌怪坐标 );
      //*    if亮Z烧怪( 怪块亮级, 当ent );
      //*  }/*for*/
      //*  /*!彳亍后*/
      //*  记t点ζ烧怪ζ上1 = 当t;
      //*}/*if*/
    }/*if*/
  }/*void*/
  /*方__余同这Z触`用来(保证立即彳亍),但不能*/
  /*不需__int tick隔, int 余同这Z触*/
  public void if时隔Z_if亮Z烧( ) {/*2026年2月21日06时47分42*/
    if(/*!时隔*/本pl.tickCount % 5 == 0 ) {/*https://docs.neoforged.net/docs/entities/#mobcategory */
      lev1 = 本pl.level( ); Minecraft mc1 = Minecraft.getInstance( );
      LdJm_FhKy = mc1.level.getLightEngine( ).getLayerListener( LightLayer.BLOCK );
      gVEnt_检测entAIf亮Z烧怪( );
    } else {/*非特时=>retu*/}/*if*/
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
    ///if(/*!t隔,推迟彳亍*/本pl.tickCount > 在首几t不彳亍 ) {/*https://docs.neoforged.net/docs/entities/#mobcategory */}/*if*/
    if( 本pl == null ) { return; }/*if*/
    /*?2026年2月22日01时19分43__原来是c,  2026年2月24日04时23分57__c需player*/
    ///本pl./*c*/displayClientMessage(Component.translatable("触tick", ""), false);
    if时隔Z_if亮Z烧( );
  }/*void*/
  /*用来__g(本pl),其他的没有做到*/
  //*@EventBusSubscriber(modid = MODID )
  @SubscribeEvent public void PIEvζRightClickBlock( PlayerInteractEvent.RightClickBlock event ) {/*最初, 从( Wandering-Trader-Express-main  https://github.com/ddd575/Wandering-Trader-Express/tree/main )*/
    本pl = event.getEntity( );
    /*?没用__不立即烧; 不烧; */
    //*int 当plTi = 本pl.tickCount;
    //*/*c*/本pl.displayClientMessage(Component.translatable("本pl.tickCount", 本pl.tickCount), false);/*attentionBar=文本还是提示*/
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
  @SubscribeEvent public void PIEvζEntityInteract( PlayerInteractEvent.EntityInteract event ) {/*2026年2月21日07时26分33*/
    本pl = event.getEntity( );
  }/*void*/
  @SubscribeEvent public void PIEvζLeftClickBlock( PlayerInteractEvent.LeftClickBlock event ) {
    本pl = event.getEntity( );
  }/*void*/
  @SubscribeEvent public void PEveζPlayerLoggedInEvent( PlayerEvent.PlayerLoggedInEvent event ) {/*2026年2月22日03时00分25*/
    本pl = event.getEntity( );
  }/*void*/
  /*没(setCanceled)*/
  //@SubscribeEvent(priority = EventPriority.LOWEST)
  // @SubscribeEvent public void LEζ生物跳跃事( LivingEvent.LivingJumpEvent 事 ) {/*2026年2月24日13时34分23*/
  //   Entity 当ent = 事.getEntity( ); //*ClientboundMoveEntityPacket.
  //   if( 本pl != 当ent ) { return; }/*if*/
  //   int 当t = 本pl.tickCount;
  //   记tζsetNoGravity = 当t;
  //   跳.本pl = 本pl; 跳.tp到上( 当ent, 1 );
  //   ///当ent.setNoGravity( true );
  // }/*void*/

}/*class*/