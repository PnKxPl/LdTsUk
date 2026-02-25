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

public class ifLdZUkGy {
  public static Config 设件 = new Config( );
  //!从设件
  /*!记录__设置*/
  int if亮大这Z烧怪 = 6, 出伤ζ亮减 = 10/*7__6(-1),7(开`出伤)*/, 出伤乘 = 4;
  /*名__t隔=tick隔*//*300=15s,20=1s*/
  int 在首几t不彳亍 = 15, 设ζt隔ζ检ent = 20, 设ζt隔ζ烧怪 = 20;
  /*不知`单位(是(1个`或半个)块)*/
  int 检rX = 64, 检rY = 32, 检rZ = 64;
  //!彳亍中
  /*!记录__设置*/

  /*!记录*/
  Player 当pl = null;
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
  public ifLdZUkGy( ) {
  }
  public void 从configG量( ) {/*2026年2月25日03时16分44*/
    ///出伤ζ亮减 = Config.r出伤ζ亮减( ); 出伤乘 = Config.r出伤乘( );
    ///出伤ζ亮减 = 设件.r出伤ζ亮减( ); 出伤乘 = 设件.r出伤乘( );
    在首几t不彳亍 = 设件.在首几t不彳亍.get( );
    if亮大这Z烧怪 = 设件.if亮大这Z烧怪.get( );
    出伤ζ亮减 = 设件.出伤ζ亮减.get( ); 出伤乘 = 设件.出伤乘.get( );
    设ζt隔ζ检ent = 设件.设ζt隔ζ检ent.get( );
    检rX = 设件.检rX.get( );
    检rZ = 设件.检rZ.get( );
    检rY = 设件.检rY.get( );
    ///当pl./*c*/displayClientMessage( Component.translatable( "从configG量 <--出伤乘", 出伤乘 ), false );

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
        if(/*得正u=>出伤*/怪块亮级 > 出伤ζ亮减 ) {
          {//!出伤
            ///*c*/当pl.displayClientMessage(Component.translatable("触hurt", ""), false);
            ///ent1.hurt( 当pl.damageSources( ).inFire( ), (怪块亮级 - 出伤ζ亮减 )*出伤乘);/*烧=不打坚守者; 从(豆包)*/
            ///ent1.hurt( 当pl.damageSources( ).playerAttack( 当pl ), (怪块亮级 - 出伤ζ亮减 )*出伤乘 );
            ///ent1.hurt( 当pl.damageSources( ).dragonBreath(  ), (怪块亮级 - 出伤ζ亮减 )*出伤乘 );
            ///ent1.hurt( ent1.damageSources( ).dragonBreath(  ), (怪块亮级 - 出伤ζ亮减 )*出伤乘 );
            ///ent1.hurt( 当pl.damageSources( ).anvil( ent1 ), (怪块亮级 - 出伤ζ亮减 )*出伤乘 );
            ent1.hurt( ent1.damageSources( ).inWall( ), ( 怪块亮级 - 出伤ζ亮减 ) * 出伤乘 );
          } ;
        }/*if*/
      }/*if*/
    }/*if*/
  }/*void*/
  /*!(forAGVEnt --> if亮Z烧怪)或(forVEnt --> if亮Z烧怪)*/
  public void gVEnt_检测entAIf亮Z烧怪( ) {/*2026年2月21日07时12分19*/
    int 当t = 当pl.tickCount;
    ///if(/*!恢复主角grav*/当t - 记tζsetNoGravity >= 10 ) {
    ///  当pl.setNoGravity( false );
    ///}/*if*/
    if(/*!时隔够=>将检*/当t - 记t点ζ检entζ上1 >= 设ζt隔ζ检ent ) {
      从configG量( );
      ///当pl./*c*/displayClientMessage( Component.translatable( "forAGVEnt --> if亮Z烧怪", "" ), false );
      玩家坐标 = 当pl.blockPosition( );
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
      //*  当pl./*c*/displayClientMessage( Component.translatable( "forVEnt --> if亮Z烧怪", "" ), false );
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
  public void if时隔Z_if亮Z烧( Player pl1 ) {/*2026年2月21日06时47分42*/
    if(/*!时隔*/pl1.tickCount % 5 == 0 ) {/*https://docs.neoforged.net/docs/entities/#mobcategory */
      lev1 = pl1.level( ); Minecraft mc1 = Minecraft.getInstance( );
      LdJm_FhKy = mc1.level.getLightEngine( ).getLayerListener( LightLayer.BLOCK );
      当pl = pl1;//!填
      gVEnt_检测entAIf亮Z烧怪( );
    } else {/*非特时=>retu*/}/*if*/
  }/*void*/

  /**/;//▬测试▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  /*-100=出伤了`mob会死*/
  public void 在0亮Z对怪出负1伤(/*入*/int 怪块亮级,/*出*/Entity ent1 ) {/*2026年2月23日22时19分16*/
    if(/*测试(光级=0,攻击)*/怪块亮级 == 0 ) {/*2026年2月23日22时42分52*/
      ///player./*c*/displayClientMessage( Component.translatable( "亮级 = 0", "" ), false );
      ///ent1.hurt( player.damageSources( ).inFire( ), 怪块亮级 - 1 );/*从(豆包)*/
    }/*if*/
  }/*void*/
}/*class*/