
package com.pnkxpl.ld_ts_uk;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/*现象__(右键,每tick)会烧两次; 先(右键)启动*/
public class EventHandler{
  Player player = null;
  int 右键烧のplayerTickζ上1 = 0;
  /*!记录__设置*/
  int 设ζtick隔 = 20;
  boolean 是否ent敌对( Entity entity ){/*从(围火无怪NoHostilesAroundCampfire);2026年2月21日03时25分33*/
    if( entity.getType( ).getCategory( ).equals( MobCategory.MONSTER ) ){
      return true;
    }/*if*/
    return false;
  }/*boolean*/
  public void 烧怪( Entity ent1){/*2026年2月21日18时11分01*/
    //ent1.level().addParticle(...);
    ///if (/*已着火=>重新着火*/ent1.isOnFire()) { ent1.clearFire(); }/*if*/
    ent1.setRemainingFireTicks( 40 );/*连续`和重新=连续扣血,=不(点1下烧1下)*/
    if(/*光级>1=>攻击*/怪块亮级 > 1 ){
      ent1.hurt( player.damageSources( ).inFire( ), 怪块亮级 - 1 );/*从(豆包)*/
    }/*if*/
  }/*void*/
  public void if光Z烧( ){/*2026年2月21日07时12分19*/
    Level lev1 = player.level( );
    Minecraft mc1 = Minecraft.getInstance( );
    LayerLightEventListener LdJm_FhKy/*亮检ζ方块*/ = mc1.level.getLightEngine( ).getLayerListener( LightLayer.BLOCK );
    BlockPos 玩家坐标 = player.blockPosition( );
    ///*c*/player.displayClientMessage(Component.translatable("检测玩家周围", ""), false);
    for(/*检测玩家周围*/Entity ent1 : lev1.getEntities( null, new AABB( 玩家坐标.getX( ) - 192, 玩家坐标.getY( ) - 192, 玩家坐标.getZ( ) - 192, 玩家坐标.getX( ) + 192, 玩家坐标.getY( ) + 192, 玩家坐标.getZ( ) + 192 ) ) ){
      ///*c*/player.displayClientMessage(Component.translatable("是否ent敌对", ""), false);
      if( 是否ent敌对( ent1 ) ){
        BlockPos 敌怪坐标 = ent1.blockPosition( );
        /*g亮度*/int 怪块亮级 = LdJm_FhKy.getLightValue( 敌怪坐标 );
        if(/*见光=>烧*/怪块亮级 > 0 ){
 //烧怪(ent1);
 //ent1.
        }/*if*/
      }/*if*/
    }/*for*/
  }/*void*/
  /*方__余同这Z触`用来(保证立即彳亍),但不能*/
  public void if时隔Z_if光Z烧( int tick隔, int 余同这Z触 ){/*2026年2月21日06时47分42*/
    if( player == null ){
      return;
    }/*if*/
    if(/*!时隔*/player.tickCount % tick隔 == 余同这Z触 ){/*https://docs.neoforged.net/docs/entities/#mobcategory */
      if光Z烧( );
    } else{/*非特时=>retu*/
      return;
    }/*if*/
  }/*void*/
  @SubscribeEvent
  /*tickCount不是(最大=19),自增*/
  /*ChunkTicketLevelUpdatedEvent =彳亍1次;    ServerTickEvent.Post =可; */
  public void onTick( ServerTickEvent.Post 事 ){ if时隔Z_if光Z烧( 设ζtick隔, 0 ); }/*void*//*2026年2月21日06时17分58*/
  /*用来__保证(右键立即彳亍, 不是每次右键都彳亍)*/
  public int 是否u1减u2大同u3( int u1, int u2, int u3 ){ if( (u1 - u2) >= u3 ){ return 1; }/*if*/    return 0; }/*void*//*2026年2月21日06时59分36*/
  /*用来__从(player.tickCount)g(时隔)*/
  public int rUの除余( int u1, int 除 ){/*2026年2月21日06时40分35*/
    int iR = u1 % 除;
    ///*c*/player.displayClientMessage(Component.translatable("iR", iR), false);/*attentionBar=文本还是提示*/
    return iR;
  }/*void*/
  /*用来__g(player),其他的没有做到*/
  @SubscribeEvent
  public void PIEvζRightClickBlock( PlayerInteractEvent.RightClickBlock event ){/*最初, 从( Wandering-Trader-Express-main  https://github.com/ddd575/Wandering-Trader-Express/tree/main )*/
    player = event.getEntity( );
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
  public void PIEvζEntityInteract( PlayerInteractEvent.EntityInteract event ){/*2026年2月21日07时26分33*/
    player = event.getEntity( );
  }/*void*/    public void PIEvζEntityInteractSpecific( PlayerInteractEvent.EntityInteractSpecific event ){
    player = event.getEntity( );
  }/*void*/    public void PIEvζLeftClickBlock( PlayerInteractEvent.LeftClickBlock event ){
    player = event.getEntity( );
  }/*void*/    public void PIEvζLeftClickEmpty( PlayerInteractEvent.LeftClickEmpty event ){
    player = event.getEntity( );
  }/*void*/    public void PIEvζRightClickEmpty( PlayerInteractEvent.RightClickEmpty event ){
    player = event.getEntity( );
  }/*void*/    public void PIEvζRightClickItem( PlayerInteractEvent.RightClickItem event ){
    player = event.getEntity( );
  }/*void*/
}/*class*/