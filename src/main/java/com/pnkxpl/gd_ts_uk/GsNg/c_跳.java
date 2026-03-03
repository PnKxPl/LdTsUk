package com.pnkxpl.c_跳_ts_tpac_跳;

import com.ibm.icu.impl.coll.UVector32;
import com.pnkxpl.gd_ts_uk.core.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.pnkxpl.gd_ts_uk.c_改attribute.类转;
//import static com.pnkxpl.c_跳_ts_tpac_跳.core.WanderingTraderExpressDelivery.MODID;

/*名__c_跳=跳*/
public class c_跳 {
  //!记录__设置
  //用来__tp后记y, 回y触发
  int 设ζ回定相yTp下 = 1, 回定相yTp下 = 0, 是否in跳 = 0, 是否in跳A已tp上 = 0, 是否已去更高 = 0;
  double tp后y = -1500;
  //!记录
  Player 当pl = null;
  BlockPos 当块pos = null;
  //*ServerboundMovePlayerPacket.Pos 当plPos
  Position pos当pl = null;
  static double 设ζy加 = 0, 设ζx加 = 0, 设ζz加 = 0;
  int 记tζsetNoGravity = -1500;
  public c_跳( ) { }
  public void 读confATp坐加( Player pl1 ) {
    跳同tpA跳ζ从configG值( );
    if足到目标块ATp坐加( pl1, 设ζy加, 设ζx加, 设ζz加 );
  }/*void*/
  /**/;//▬jump▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  ;//▬▬▬▬▬jump=(tp上,jump,tp下)▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  ;//▬▬▬▬▬jump=(tp上,jump)▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  //名__从configG值ζ跳同tpA跳  ζ跳同tpA跳从configG值
  public static void 跳同tpA跳ζ从configG值( ) {
    设ζy加 = 类转.rD( Config.设ζ跳同tpA跳ζtp坐加.get( ).get( 0 ) );
    设ζx加 = 类转.rD( Config.设ζ跳同tpA跳ζtp坐加.get( ).get( 1 ) );
    设ζz加 = 类转.rD( Config.设ζ跳同tpA跳ζtp坐加.get( ).get( 2 ) );
  }/*void*/
  //todo ClientboundMoveEntityPacket.
  //todo tp_坐加
  //!方__tp上,跳 --> 每t(检(是否已去更高)) --> ifY=>tp下
  //todo s无gravATp到上
  public void if足到目标块ATp坐加( Player pl1, double y加, double x加, double z加 ) {/*2026年3月3日03时58分16*/
    int iR = 是否pl的上の坐有块AR从头加几の坐是块( pl1, ( int ) y加 );  if(/*头上1=>r*/ iR == 1 ) { return; }/*if*/ if(/*头上2=>tp1*/ iR > 1 ) { y加 = iR - 1; }/*if*/
    //*if( 是否从pl足坐加の竖经y坐有块( pl1, y加 + 1, x加, z加 ) == 1 ) { return; }/*if*/
    ///当块pos = pl1.blockPosition( );
    ///double 新y = pos当pl.y( ) + y加, 新x = pos当pl.x( ) + x加, 新z = pos当pl.z( ) + z加;
    /*teleportTo=tp(坐标)(不跳); teleportRelative=tp(相对)A跳;*/
    {//!tp
      //!x z 只能0, 不0零等于原力
      ///pl1./*c*/displayClientMessage(Component.translatable("tpRelat", ""), false);
      //*pl1.teleportRelative( 新x, 新y, 新z );
      //*pl1.teleportRelative( x加, y加, z加 );
      //*pl1.teleportRelative( x加, y加,0 );
      pl1.teleportRelative( 0, y加, 0 );
    } ;
  }/*void*/
  public void tp_坐加( Player pl1, double y加 ) {/*2026年2月25日14时47分47*/
    int iR = 是否pl的上の坐有块AR从头加几の坐是块( pl1, ( int ) y加 );  if(/*头上1=>r*/ iR == 1 ) { return; }/*if*/ if(/*头上2=>tp1*/ iR > 1 ) { y加 = iR - 1; }/*if*/
   
    ///当块pos = pl1.blockPosition( );
    ///pos当pl = pl1.position( );
    ///double 新y = pos当pl.y( ) + y加;
    /*teleportTo=tp(坐标)(不跳); teleportRelative=tp(相对)A跳;*/
    {//!tp
      pl1.teleportRelative( 0, y加, 0 );
      //pl1.setNoGravity( true ); pl1.teleportTo(pos当pl.x( ), 新y, pos当pl.z( ) );
    } ;
    {//!tp后
      是否in跳A已tp上 = 1;
      if(/*=>记y*/设ζ回定相yTp下 == 1 ) {
        tp后y = pl1.position( ).y;
      }/*if*/
    } ;
    ///pl1./*c*/displayClientMessage( Component.translatable( "tp到上 <--新y", 新y ), false );
    int 当t = pl1.tickCount; 记tζsetNoGravity = 当t;
  }/*void*/

  /*是否已去更高=1*/
  public void ifY大yZ改是否已去更高( Player pl1 ) {/*2026年2月26日13时06分32*/
    if( 是否in跳A已tp上 == 1 ) {
      if( pl1.position( ).y > tp后y ) { 是否已去更高 = 1; }/*if*/
    }/*if*/
  }/*void*/

  //!没用
  //入__负u=向下
  /*是否已去更高=0*/
  public void 回定相yTp下( Player pl1, int 相y ) {/*2026年2月26日12时58分27*/
    if( 是否已去更高 == 1 ) {
      if( pl1.position( ).y == tp后y ) {
        //*if(/*回y,ifAir=>tp下*/ 是否pl的下の坐标是方块( pl1 ) == false ) {/*2026年2月26日14时04分58*/}/*if*/
        {//!tp下
          pl1.teleportRelative( 0, 相y, 0 );
          pl1./*c*/displayClientMessage( Component.translatable( "tp下", "" ), false );
        } ;

        //todo 2026年2月26日12时57分31__是否初始化量?
        是否已去更高 = 0;
        是否in跳A已tp上 = 0;
        是否in跳 = 0; tp后y = -1500;
      }/*if*/
    }/*if*/
  }/*void*/
  public void 恢复主角grav( Player pl1 ) {/*2026年2月25日07时07分41*/
    int 当t = pl1.tickCount;
    if(/*!恢复主角grav*/当t - 记tζsetNoGravity >= 10 ) {
      pl1.setNoGravity( false );
    }/*if*/
  }/*void*/
  /**/;//▬检测if块▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  //不入__Position posPl, double 身高几米
  public boolean 是否pl头の坐标是方块( Player pl1 ) {/*从豆包;2026年2月25日15时11分19*/
    ///double pl的上y=posPl.y()+=身高几米;  double pl的上y = bposPl得头.getY()+1;
    BlockPos bposPl得头 = pl1.blockPosition( ).above( 1 );
    BlockState 头块状 = pl1.level( ).getBlockState( bposPl得头 );
    ///if( 头块状.isAir( ) ) { pl1./*c*/displayClientMessage( Component.translatable( "头是air", "" ), false ); }/*if*/
    return !头块状.isAir( );
  }/*boolean*/
  public int 是否pl的上の坐有块AR从头加几の坐是块( Player pl1, int 从头上几 ) {/*2026年2月25日15时25分35*/
    //!for( if(yの块) )
    for( int y加 = 1 ; y加 <= 从头上几 ; y加++ ) {/*2026年3月3日17时19分32*/
      BlockPos bposPl的上 = pl1.blockPosition( ).above( y加 + 1 );
      BlockState pl的上块状 = pl1.level( ).getBlockState( bposPl的上 );
      if( !pl的上块状.isAir( ) ) { ///pl1./*c*/displayClientMessage( Component.translatable( "头的上!=air", "" ), false );
        return y加;
      }/*if*/
    }/*for*/
    return 0;
  }/*int*/
  //todo public int 是否从pl足坐加の坐是方块( Player pl1, double x, double z, double y从足加 ) {
  //pl.(above(1 )=头,   above( 2 )=头的上)
  int 是否从pl足坐加の坐是块( Player pl1, double y从足加, double x加, double z加 ) {/*2026年3月3日14时04分35*/
    BlockPos bposPl足 = pl1.blockPosition( ); BlockPos bpos改xz = bposPl足;
    //!改xz
    return 0;
  }/*int*/
  //if(足到目标)块
  //!xz是错的，应该用旧版
  //!用法_入__y加+1(头)
  public int 是否从pl足坐加の竖经y坐有块( Player pl1, double y从足加, double x加, double z加 ) {
    BlockPos bposPl足 = pl1.blockPosition( ); BlockPos bpos改xz = bposPl足;
    //!改xz
    //todo -数行不行？
    //bpos改xz=bpos改xz.relative( Direction.EAST, ( int ) x加 ); bpos改xz=bpos改xz.relative( Direction.SOUTH, ( int ) z加 );
    if( x加 > 1 ) { bpos改xz = bpos改xz.east( ( int ) x加 ); }/*if*/
    if( z加 > 1 ) { bpos改xz = bpos改xz.south( ( int ) z加 ); }/*if*/
    pl1./*c*/displayClientMessage( Component.translatable( "co1", bpos改xz.getX( ) ), false );
    //!for( if(yの块) )
    for( int y加 = 0 ; y加 <= y从足加 ; y加++ ) {
      BlockPos bpos已加 = bpos改xz.above( ( int ) y加 );
      BlockState 坐已加の块状 = pl1.level( ).getBlockState( bpos已加 );
      if(/*不是空气=>r1*/ !坐已加の块状.isAir( ) ) {
        pl1./*c*/displayClientMessage( Component.translatable( "目标坐块!=air", "" ), false );
        return y加;

      }/*if*/
    }/*for*/
    return 0;
  }/*int*/
  //!无规则
  public boolean 是否pl的下の坐标是方块( Player pl1 ) {/*2026年2月26日13时46分53*/
    BlockPos bposPl的下 = pl1.blockPosition( ).below( -2 );//-1=0=; 2=没变
    BlockState pl的下块状 = pl1.level( ).getBlockState( bposPl的下 ); if( pl的下块状.isAir( ) ) { pl1./*c*/displayClientMessage( Component.translatable( "pl的下=air", "" ), false ); }/*if*/
    return !pl的下块状.isAir( );
  }/*boolean*/
  /**/;//▬计算jump落点▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  public void rJump落点( Player pl1 ) {/*2026年2月25日07时07分41*/
    //!记录
    Position 起坐标 = pl1.position( ); int 起t = pl1.tickCount;
    Vec3 vec3初速 = pl1.getDeltaMovement( );/*c空(无小括);*/ ///AABB bb = pl1.getBoundingBox( );
    double 初速y = vec3初速.y;
    pl1./*c*/displayClientMessage( Component.translatable( "vec3初速y", 初速y ), false );
    pl1./*c*/displayClientMessage( Component.translatable( "vec3初速x", vec3初速.x ), false );
    double y = 0, x = 0, z = 0;/*相对坐标*/
    //!算程__(撞块)=>tp, (无结果)=>不彳亍
    for( int t = 0 ; t < 100 ; t++ ) { //最多模拟 5 秒
      break;
    }/*for*/
  }/*void*/
  /**/;//!从`豆包▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  //没用
  public void 惯性乘( Player pl1, float 乘这 ) {/*从豆包;2026年2月25日14时19分46*/
    Vec3 motion = pl1.getDeltaMovement( );
    pl1.setDeltaMovement( motion.scale( 乘这 ) ); //填1.5=1.5倍速度
  }/*void*/

}/*class*/