package com.pnkxpl.tc_ts_tpatc;

import com.ibm.icu.impl.coll.UVector32;
import com.pnkxpl.gd_ts_uk.libJava.QmViJiSr;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static net.minecraft.world.InteractionHand.MAIN_HAND;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.MAX_DISTANCE;
//import static com.pnkxpl.tc_ts_tpatc.core.WanderingTraderExpressDelivery.MODID;
//*final and =&&

/*名__placeFront  FhKyZlKyQm=放在块前*/
//!右键-->if air=>放!
public class FhKyZlKyQm {
  public static QmViJiSr 前算 = new QmViJiSr( );
  //!记录_设置
  //!记录
  BlockPos 当块pos = null;
  //*ServerboundMovePlayerPacket.Pos 当plPos
  Position pos当pl = null;
  int 记tζsetNoGravity = -1500;
  public FhKyZlKyQm( ) { }
  /**/;//!从`豆包▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  public static BlockPos r玩家视线指の块坐( Player pl1 ) {/*2026年2月27日01时33分57*/
    BlockPos targetPos = null;
    HitResult hitResult = pl1.pick( MAX_DISTANCE, 0, false );
    if( hitResult.getType( ) == HitResult.Type.BLOCK ) {
      BlockHitResult blockHit = ( BlockHitResult ) hitResult;
      targetPos = blockHit.getBlockPos( ).relative( blockHit.getDirection( ) );
    }/*if*/
    return targetPos;
  }/*void*/
  public static int 放块( Level level, BlockPos bpos, BlockState bstat ) {/*2026年2月27日00时59分41*/
    ///if(/*是客户端*/ level.isClientSide( ) ) { return 0; }
    if(/*位置有效A可放置*/ level.isInWorldBounds( bpos ) && level.isEmptyBlock( bpos ) ) {
      // 设置方块（2=更新邻居和通知客户端）
      level.setBlock( bpos, bstat, 2 );
      // 触发`方块放置事件
      level.levelEvent( 2001, bpos, Block.getId( bstat ) );
      return 1;
    }/*if*/
    return 0;
  }/*int*/
  public static int pl主手放块( Player pl1, BlockPos bpos ) {
    ItemStack handStack = pl1.getItemInHand( MAIN_HAND );///tOffhandItem( );
    if(/*有物品A能放*/ !handStack.isEmpty( ) && handStack.getItem( ) instanceof net.minecraft.world.item.BlockItem bitem1 ) {
      if( 放块( pl1.level( ), bpos, bitem1.getBlock( ).defaultBlockState( ) ) == 1 ) {
        handStack.shrink( 1 ); //消耗一个物品
      }/*if*/
      return 1;
    }/*if*/
    return 0;
  }/*int*/
  //打在这=(c保存,闪退)  --当pl  (双击是点,点,点点)
  public void PIEvζ右键点块ζ放块在块前( PlayerInteractEvent.RightClickBlock 事 ) {/*2026年2月27日03时09分56*/
    Player pl1 = 事.getEntity( );
    if(/*点脚下=>触*/ 前算.是否坐同( 事.getPos( ), pl1.getOnPos( ) ) == 1 ) {
      pl1./*c*/displayClientMessage( Component.translatable( "放块在块前", "" ), true );
      Direction pl朝向 = pl1.getDirection( );
      ///pl1./*c*/displayClientMessage( Component.translatable( "co1", pl朝向.getStepX()/*=0,1,-1*/ ), false );
      ///pl1./*c*/displayClientMessage( Component.translatable( "co1", pl朝向.getStepY( )/*=0*/ ), false );
      ///pl1./*c*/displayClientMessage( Component.translatable( "co1", pl朝向.getStepZ()p() ), false );
      BlockPos 放块坐 = null;
      if( pl朝向.getStepX( ) == -1 ) {//!左=西
        放块坐 = 事.getPos( ).west( );
      } else if( pl朝向.getStepX( ) == 1 ) {//!右=东
        放块坐 = 事.getPos( ).east( );
      } else if( pl朝向.getStepX( ) == 0 ) {
        if( pl朝向.getStepZ( ) == 1 ) {//!南
          放块坐 = 事.getPos( ).south( );
        } else if( pl朝向.getStepZ( ) == -1 ) {//!北
          放块坐 = 事.getPos( ).north( );
        }/*if*/
      }/*if*/
      pl主手放块( pl1, 放块坐 );
    }/*if*/
  }/*void*/

}/*class*/