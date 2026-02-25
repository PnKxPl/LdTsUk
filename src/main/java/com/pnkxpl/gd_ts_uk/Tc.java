package com.pnkxpl.gd_ts_uk;

import com.ibm.icu.impl.coll.UVector32;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
//import static com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery.MODID;

/*名__Tc=跳*/
public class Tc {
  //!记录_设置
  //!记录
  Player 当pl = null;
  BlockPos 当块pos = null;
  //*ServerboundMovePlayerPacket.Pos 当plPos
  Position pos当plPos = null;
  int 记tζsetNoGravity = -1500;
  public Tc( ) { }
  /**/;//▬jump▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  /*现象__tp到上A跳*/ /*入__1=+1=到上1*/
  public void tp到上( Entity ent1, int y ) {
    //*ent1.setDeltaMovement(ent1.getDeltaMovement().x, 0, ent1.getDeltaMovement().z);/*没用(无关EventPriority); onTick=大多是普通跳跃; 向y速度=0*/
    //*Vec3 v1= new Vec3(0, -1000, 0); pl1.addDeltaMovement(v1);/*没用*/
    当块pos = ent1.blockPosition( );//*getPose();
    ent1.setPos( 当块pos.getX( ), 当块pos.getY( ) + y, 当块pos.getZ( ) );/*+3=+3*/
  }/*void*/
  public void tp到上( Player pl1, int y ) {/*2026年2月25日07时12分48*/
    //*pl1.setDeltaMovement(pl1.getDeltaMovement().x, 0, pl1.getDeltaMovement().z);/*没用(无关EventPriority); onTick=大多是普通跳跃; 向y速度=0*/
    //*Vec3 v1= new Vec3(0, -1000, 0); pl1.addDeltaMovement(v1);/*没用*/
    pl1./*c*/displayClientMessage( Component.translatable( "tp到上 <--当块pos.getY", 当块pos.getY( ) ), false );
    int 新y = 当块pos.getY( ) + y;
    pl1.setPos( 当块pos.getX( ), 新y, 当块pos.getZ( ) );/*+3=+3*/
    新y += y;
    pl1.setPosRaw( 当块pos.getX( ), 新y, 当块pos.getZ( ) );
    pl1./*c*/displayClientMessage( Component.translatable( "tp到上 <--新y", 新y ), false );
  }/*void*/
  public void s无gravATp到上( Player pl1, int y ) {
    当块pos = pl1.blockPosition( );
    pos当plPos = pl1.position( );
    double 新y = pos当plPos.y( ) + y; float 速乘 = ( float ) 0.2;
    /*teleportTo=tp(坐标),不跳跃; teleportRelative=tp(相对),跳跃;*/
    {//!tp
      pl1.teleportRelative( 0, y, 0 );
      //pl1.setNoGravity( true ); pl1.teleportTo(pos当plPos.x( ), 新y, pos当plPos.z( ) );
    } ;//*惯性乘(pl1,10);
    pl1.getAbilities( ).setFlyingSpeed( 速乘 );
    ///pl1.getAbilities(  ).setWalkingSpeed(速乘 );
    //pl1.teleportTo( 0, 新y, 0 );/*+3=+3*/
    pl1./*c*/displayClientMessage( Component.translatable( "tp到上 <--新y", 新y ), false );
    ///当pl = pl1;
    int 当t = pl1.tickCount; 记tζsetNoGravity = 当t;
  }/*void*/
  public void 恢复主角grav( Player pl1 ) {/*2026年2月25日07时07分41*/
    int 当t = pl1.tickCount;
    if(/*!恢复主角grav*/当t - 记tζsetNoGravity >= 10 ) {
      pl1.setNoGravity( false );
    }/*if*/
  }/*void*/
  /**/;//!从`豆包▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  public void 惯性乘( Player pl1, float 乘这 ) {/*从豆包;2026年2月25日14时19分46*/
    Vec3 motion = pl1.getDeltaMovement( );
    pl1.setDeltaMovement( motion.scale( 乘这 ) ); //填1.5=1.5倍速度
  }/*void*/

}/*class*/