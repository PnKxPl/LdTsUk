package com.pnkxpl.gd_ts_uk;
import java.util.ArrayList;

import com.ibm.icu.impl.coll.UVector32;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Vector;
//import static com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery.MODID;

/*现象__(右键,每tick)会烧两次; 先(右键)启动*/
public class UiJmHander {
  /*!记录*/
  Player player = null;
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
  /*!记录__设置*/
  /*不知`单位(是(1个`或半个)块)*/
  int 检rX = 0, 检rY = 0, 检rZ = 0;
  /*名__t隔=tick隔*//*300=15s,20=1s*/
  int 设ζt隔ζ检ent = 20, 设ζt隔ζ烧怪 = 20;
  int 记t点ζ检entζ上1 = -1500, 记t点ζ烧怪ζ上1 = -1500;
  /*不需`是否将检测ent`是否将烧*/
  public UiJmHander( ) {
    //检rX=192,检rY=192,检rZ=192;
    检rX = 64; 检rY = 32; 检rZ = 64;
  }
  boolean 是否entMonster( Entity entity ) {/*从(围火无怪NoHostilesAroundCampfire);2026年2月21日03时25分33*/
    if( entity.getType( ).getCategory( ).equals( MobCategory.MONSTER ) ) {
      return true;
    }/*if*/ return false;
  }/*boolean*/
  /*没用*/
  /*方__燃烧,if亮ZHurt*/
  public void if亮Z烧怪(/*入*/int 怪块亮级,/*出*/Entity ent1 ) {/*2026年2月21日18时11分01*/
    if(/*见光=>烧*/怪块亮级 > 7 ) {
      //if亮Z烧怪(怪块亮级,ent1);
      //ent1.addTag();
      //*int 设ζ友好 = -1;
      //boolean 设ζ友好;
      //设ζ友好 = ent1.getType( ).getCategory( ).isFriendly( );
      //.equals( MobCategory.MONSTER ) ){
      //*player./*c*/displayClientMessage( Component.translatable( "设ζ友好", 设ζ友好 ), false );
      // 先清空父类（原版敌对AI）
      //*ent1.goalSelector.getAvailableGoals( ).clear( );
      //*ent1.targetSelector.getAvailableGoals( ).clear( );
      //
      //ent1.isEffectiveAi();
      //*ent1.level().isClientSide=false;
      //ent1.level().addParticle(...);
      ///if (/*已着火=>重新着火*/ent1.isOnFire()) { ent1.clearFire(); }/*if*/
      ent1.setRemainingFireTicks( 40 );/*连续`和重新=连续扣血,=不(点1下烧1下)*/ if(/*光级>1=>攻击*/怪块亮级 > 1 ) {
        ///*c*/player.displayClientMessage(Component.translatable("触hurt", ""), false);
        ent1.hurt( player.damageSources( ).inFire( ), 怪块亮级 - 1 );/*从(豆包)*/
      }/*if*/
    }/*if*/
  }/*void*/
  /*!(forAGVEnt --> if亮Z烧怪)或(forVEnt --> if亮Z烧怪)*/
  public void gVEnt_检测entAIf亮Z烧怪( ) {/*2026年2月21日07时12分19*/
    int 当t = player.tickCount;
    if(/*!时隔够=>将检*/当t - 记t点ζ检entζ上1 >= 设ζt隔ζ检ent ) {
      player./*c*/displayClientMessage( Component.translatable( "forAGVEnt --> if亮Z烧怪", "" ), false );
      玩家坐标 = player.blockPosition( );
      ///*c*/player.displayClientMessage(Component.translatable("检测玩家周围", ""), false);
      //*vEntζ周围 = null; vEntζmonster = null; vEntζ敌 = null; vEntζmonsterS = 0;
      vEnt坐标ζ周围 = null; vEnt坐标ζmonster = null; vEnt坐标ζ敌 = null; vEnt坐标ζmonsterS = 0;
      for(/*检测玩家周围*/Entity ent1 : lev1.getEntities( null/*todo 2026年2月23日23时05分31__这书啥*/
          , new AABB( 玩家坐标.getX( ) - 检rX, 玩家坐标.getY( ) - 检rY, 玩家坐标.getZ( ) - 检rZ, 玩家坐标.getX( ) + 检rX, 玩家坐标.getY( ) + 检rY, 玩家坐标.getZ( ) + 检rZ ) ) ) {
        //*vEntζ周围.add( ent1 );
        if( 是否entMonster( ent1 ) ) {
          敌怪坐标 = ent1.blockPosition( );
          //*vEnt坐标ζmonster.  add( 敌怪坐标 ); vEnt坐标ζmonsterS++;/*2026年2月24日04时36分09*/
          怪块亮级/*g亮度*/ = LdJm_FhKy.getLightValue( 敌怪坐标 );
          {/*测试*/
            //在0亮Z对怪出负1伤( 怪块亮级, ent1 );
          } ;
          if亮Z烧怪( 怪块亮级, ent1 );
        }/*if*/
      }/*for*/ ;
      /*!彳亍后*/
      记t点ζ检entζ上1 = 当t; ///是否将检测ent=0;
    } else {/*2026年2月24日03时42分23*/
      //*if(/*!时隔够=>将烧*/当t - 记t点ζ烧怪ζ上1 >= 设ζt隔ζ烧怪 ) {
      //*  player./*c*/displayClientMessage( Component.translatable( "forVEnt --> if亮Z烧怪", "" ), false );
      //*  for( int 一 =0; 一< vEntζmonsterS;一++ ) {
      //*    Entity ent1 =vEntζmonster.get(一);//*[一];
      //*    BlockPos 敌怪坐标 = ent1.blockPosition( );
      //*    int /*g亮度*/怪块亮级 = LdJm_FhKy.getLightValue( 敌怪坐标 );
      //*    if亮Z烧怪( 怪块亮级, ent1 );
      //*  }/*for*/
      //*  /*!彳亍后*/
      //*  记t点ζ烧怪ζ上1 = 当t;
      //*}/*if*/
    }/*if*/
  }/*void*/
  /*方__余同这Z触`用来(保证立即彳亍),但不能*/
  /*不需__int tick隔, int 余同这Z触*/
  public void if时隔Z_if亮Z烧( ) {/*2026年2月21日06时47分42*/
    if(/*!时隔*/player.tickCount % 5 == 0 ) {/*https://docs.neoforged.net/docs/entities/#mobcategory */
      lev1 = player.level( ); Minecraft mc1 = Minecraft.getInstance( );
      LdJm_FhKy = mc1.level.getLightEngine( ).getLayerListener( LightLayer.BLOCK );
      gVEnt_检测entAIf亮Z烧怪( );
    } else {/*非特时=>retu*/}/*if*/
  }/*void*/
  /*2026年2月22日01时18分37__没这=不闪退*/
  /*tickCount不是(最大=19),自增*/
  /*ChunkTicketLevelUpdatedEvent =彳亍1次;    ServerTickEvent.Post =可; */
  @SubscribeEvent public void onTick( ServerTickEvent.Post 事 ) {/*2026年2月21日06时17分58*/
    if( player == null ) {
      return;
    }/*if*/
    ///if(/*!t隔,推迟彳亍*/player.tickCount > 15 ) {/*https://docs.neoforged.net/docs/entities/#mobcategory */}/*if*/
    /*?2026年2月22日01时19分43__原来是c,  2026年2月24日04时23分57__c需player*/
    ///player./*c*/displayClientMessage(Component.translatable("触tick", ""), false);
    if时隔Z_if亮Z烧( );
  }/*void*/
  /**/;//▬算▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  /*用来__保证(右键立即彳亍, 不是每次右键都彳亍)*/
  public int 是否u1减u2大同u3( int u1, int u2, int u3 ) {
    if( ( u1 - u2 ) >= u3 ) {
      return 1;
    }/*if*/ return 0;
  }/*void*//*2026年2月21日06时59分36*/
  /*用来__从(player.tickCount)g(时隔)*/
  public int rUの除余( int u1, int 除 ) {/*2026年2月21日06时40分35*/
    int iR = u1 % 除;
    ///*c*/player.displayClientMessage(Component.translatable("iR", iR), false);/*attentionBar=文本还是提示*/
    return iR;
  }/*void*/
  /*用来__g(player),其他的没有做到*/
  //*@EventBusSubscriber(modid = MODID )
  @SubscribeEvent public void PIEvζRightClickBlock( PlayerInteractEvent.RightClickBlock event ) {/*最初, 从( Wandering-Trader-Express-main  https://github.com/ddd575/Wandering-Trader-Express/tree/main )*/
    player = event.getEntity( );
    /*?没用__不立即烧; 不烧; */
    //*int 当plTi = player.tickCount;
    //*/*c*/player.displayClientMessage(Component.translatable("player.tickCount", player.tickCount), false);/*attentionBar=文本还是提示*/
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
  //PlayerInteractEvent$    EntityInteract	玩家交互事件－实体交互
  //PlayerInteractEvent$    EntityInteractSpecific	玩家交互事件－具体的实体交互	当玩家右键实体，双方都会触发
  //PlayerInteractEvent$    LeftClickBlock	玩家交互事件－左键方块
  //PlayerInteractEvent$    LeftClickEmpty	玩家交互事件－对空左键
  //PlayerInteractEvent$    RightClickBlock	玩家交互事件－右键方块
  //PlayerInteractEvent$    RightClickEmpty	玩家交互事件－对空右键
  //PlayerInteractEvent$    RightClickItem  玩家交互事件－右键物品
  //PlayerEvent$PlayerLoggedInEvent	   玩家事件－玩家登录事件
  @SubscribeEvent public void PIEvζEntityInteract( PlayerInteractEvent.EntityInteract event ) {/*2026年2月21日07时26分33*/
    player = event.getEntity( );
  }/*void*/
  /*问__火只亮一下 =有的`可能不是player( (左键空气,左键mob)会触发(不彳亍)), (右键开箱)可 */
  /*?__火只亮一下 =有的`可能不是player( (左键空气,左键mob)会触发(不彳亍)), (右键开箱)可 */
  //*@SubscribeEvent
  //*public void PIEvζEntityInteractSpecific( PlayerInteractEvent.EntityInteractSpecific event ){
  //*  player = event.getEntity( );
  //*}/*void*/
  @SubscribeEvent public void PIEvζLeftClickBlock( PlayerInteractEvent.LeftClickBlock event ) {
    player = event.getEntity( );
  }/*void*/
  //*@SubscribeEvent
  //*public void PIEvζLeftClickEmpty( PlayerInteractEvent.LeftClickEmpty event ){
  //*  player = event.getEntity( );
  //*}/*void*/
  //*@SubscribeEvent
  //*public void PIEvζRightClickEmpty( PlayerInteractEvent.RightClickEmpty event ){
  //*  player = event.getEntity( );
  //*}/*void*/
  /// @SubscribeEvent public void PIEvζRightClickItem( PlayerInteractEvent.RightClickItem event ){
  ///  player = event.getEntity( );
  /// }/*void*/
  @SubscribeEvent public void PEveζPlayerLoggedInEvent( PlayerEvent.PlayerLoggedInEvent event ) {/*2026年2月22日03时00分25*/
    player = event.getEntity( );
  }/*void*/

  /**/;//▬测试▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  /*-100=出伤了`mob会死*/
  public void 在0亮Z对怪出负1伤(/*入*/int 怪块亮级,/*出*/Entity ent1 ) {/*2026年2月23日22时19分16*/
    if(/*测试(光级=0,攻击)*/怪块亮级 == 0 ) {/*2026年2月23日22时42分52*/
      player./*c*/displayClientMessage( Component.translatable( "亮级 = 0", "" ), false );
      ent1.hurt( player.damageSources( ).inFire( ), 怪块亮级 - 1 );/*从(豆包)*/
    }/*if*/
  }/*void*/
}/*class*/