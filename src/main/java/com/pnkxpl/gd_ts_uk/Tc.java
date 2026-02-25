package com.pnkxpl.tc_ts_tpatc;

import com.ibm.icu.impl.coll.UVector32;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
//import static com.pnkxpl.tc_ts_tpatc.core.WanderingTraderExpressDelivery.MODID;

/*名__Tc=跳*/
public class Tc {
  //!记录_设置
  //!记录
  Player 当pl = null;
  BlockPos 当块pos = null;
  //*ServerboundMovePlayerPacket.Pos 当plPos
  Position pos当pl = null;
  int 记tζsetNoGravity = -1500;
  public Tc( ) { }
  /**/;//▬jump▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  public void s无gravATp到上( Player pl1, int y ) {/*2026年2月25日14时47分47*/
    if(是否pl的上の坐标是方块(pl1)==true){return;}/*if*/
    ///当块pos = pl1.blockPosition( );
    pos当pl = pl1.position( );
    double 新y = pos当pl.y( ) + y;
    /*teleportTo=tp(坐标)(不跳); teleportRelative=tp(相对)A跳;*/
    {//!tp
      pl1.teleportRelative( 0, y, 0 );
      //pl1.setNoGravity( true ); pl1.teleportTo(pos当pl.x( ), 新y, pos当pl.z( ) );
    } ;

    ///pl1./*c*/displayClientMessage( Component.translatable( "tp到上 <--新y", 新y ), false );
    int 当t = pl1.tickCount; 记tζsetNoGravity = 当t;
  }/*void*/
  public void 恢复主角grav( Player pl1 ) {/*2026年2月25日07时07分41*/
    int 当t = pl1.tickCount;
    if(/*!恢复主角grav*/当t - 记tζsetNoGravity >= 10 ) {
      pl1.setNoGravity( false );
    }/*if*/
  }/*void*/
  //不入__Position posPl, double 身高几米
  public boolean 是否pl头の坐标是方块( Player pl1 ) {/*从豆包;2026年2月25日15时11分19*/
    ///double pl的上y=posPl.y()+=身高几米;  double pl的上y = bposPl得头.getY()+1;
    BlockPos bposPl得头 = pl1.blockPosition( ).above( 1 );
    BlockState 头块状 = pl1.level( ).getBlockState(bposPl得头 );
    ///if( 头块状.isAir( ) ) { pl1./*c*/displayClientMessage( Component.translatable( "头是air", "" ), false ); }/*if*/
    return !头块状.isAir( );
  }/*boolean*/    public boolean 是否pl的上の坐标是方块( Player pl1 ) {/*2026年2月25日15时25分35*/
    BlockPos bposPl的上 = pl1.blockPosition( ).above( 2 );
    BlockState pl的上块状 = pl1.level( ).getBlockState( bposPl的上 );
    if( !pl的上块状.isAir( ) ) { pl1./*c*/displayClientMessage( Component.translatable( "头的上!=air", "" ), false ); }/*if*/
    return !pl的上块状.isAir( );
  }/*boolean*/
  /**/;//▬计算jump落点▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  public void rJump落点( Player pl1 ) {/*2026年2月25日07时07分41*/
    //!记录
    Position 起坐标= pl1.position( );    int 起t = pl1.tickCount;
    Vec3 vec3初速 = pl1.getDeltaMovement( ); ///AABB bb = pl1.getBoundingBox( );

  }/*void*/
  /**/;//!从`豆包▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  public void 惯性乘( Player pl1, float 乘这 ) {/*从豆包;2026年2月25日14时19分46*/
    Vec3 motion = pl1.getDeltaMovement( );
    pl1.setDeltaMovement( motion.scale( 乘这 ) ); //填1.5=1.5倍速度
  }/*void*/

}/*class*/