package com.pnkxpl.gd_ts_uk;

import com.ibm.icu.impl.coll.UVector32;
import com.pnkxpl.gd_ts_uk.core.Config;
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

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
//*import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.block;
import static net.minecraft.world.InteractionHand.MAIN_HAND;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.MAX_DISTANCE;
//import static com.pnkxpl.tc_ts_tpatc.core.WanderingTraderExpressDelivery.MODID;
//*final and =&&

/*名__placeFront  FhKyZlKyQm=放在块前*/
//!右键-->if air=>放!
public class FhKyZlKyQm {
  public static QmViJiSr 前算 = new QmViJiSr( );
  //!记录__设置
  static int 是否放前 = 0;
  static int 是否放后 = 0;
  static int 是否放左 = 0;
  static int 是否放右 = 0;
  static double if坐差小这Z触ζy = 0.5;
  static double if坐差小这Z触ζxz  = 0.5; 
  //!记录
  public int 是否刚if放块在块前A将if补块 = 0;//用来__再if`补块
  public BlockPos bpos右键事块_在这if放块在块前 = null;//bpos当 =bpos刚if放块在块前 =bpos右键事块_在这if放块在块前
  public int 记tζif放块在块前 = 0;
  //*ServerboundMovePlayerPacket.Pos 当plPos
  Position pos当pl = null;
  int 记tζsetNoGravity = -1500;
  public FhKyZlKyQm( ) { }
  private void 从uG放块在块の向( int u ) {
    if( u == 0 ) { 是否放前 = 1; 是否放后 = 1; 是否放左 = 1; 是否放右 = 1; }/*if*/ else if( u == 1 ) { 是否放前 = 1; }/*if*/ else if( u == 2 ) {
      是否放后 = 1;
    }/*if*/ else if( u == 3 ) { 是否放左 = 1; }/*if*/ else if( u == 4 ) { 是否放右 = 1; }/*if*/
  }/*void*/
  public void 从configG量( ) {/*2026年3月1日00时20分41*/
    ///Config.设ζ多选ζ放块在块の向.get().size()
    for( int i1 : Config.设ζ多选ζ放块在块の向.get( ) ) {
      从uG放块在块の向( i1 );
    }/*for*/
    if坐差小这Z触ζy= Double.valueOf (Config.设ζif坐差小这Z触.get().get(0));
    if坐差小这Z触ζxz= Double.valueOf (Config.设ζif坐差小这Z触.get().get(1));
  }/*void*/
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
      level.setBlock( bpos, bstat, 2 );//设置方块（2=更新邻居和通知客户端）
      level.levelEvent( 2001, bpos, Block.getId( bstat ) );//!触发`方块放置事件
      return 1;
    }/*if*/
    return 0;
  }/*int*/
  public static int pl主手放块( Player pl1, BlockPos bpos ) {
    ItemStack handStack = pl1.getItemInHand( MAIN_HAND );///getOffhandItem( );
    if(/*有物品A能放*/ !handStack.isEmpty( ) && handStack.getItem( ) instanceof net.minecraft.world.item.BlockItem bitem1 ) {
      if( 放块( pl1.level( ), bpos, bitem1.getBlock( ).defaultBlockState( ) ) == 1 ) {
        handStack.shrink( 1 ); //消耗一个物品
      }/*if*/
      return 1;
    }/*if*/
    return 0;
  }/*int*/
  public static void 据pl朝向主手放块( Player pl1, BlockPos bpos ) {/*2026年2月27日18时11分13*/
    ///pl1./*c*/displayClientMessage( Component.translatable( "放块在块前", "" ), true );
    Direction pl朝向 = pl1.getDirection( );//pl朝向.getStepX()/*=0,1,-1*/ pl朝向.getStepY( )/*=0*/ pl朝向.getStepZ()
    BlockPos 放块坐 = null;
    if( pl朝向.getStepX( ) == -1 ) {//!左=西
      if( 是否放前 == 1 ) { 放块坐 = bpos.west( ); pl主手放块( pl1, 放块坐 ); }/*if*/
      if( 是否放后 == 1 ) { 放块坐 = bpos.east( ); pl主手放块( pl1, 放块坐 ); }/*if*/
      if( 是否放左 == 1 ) { 放块坐 = bpos.south( ); pl主手放块( pl1, 放块坐 ); }/*if*/
      if( 是否放右 == 1 ) { 放块坐 = bpos.north( ); pl主手放块( pl1, 放块坐 ); }/*if*/
    } else if( pl朝向.getStepX( ) == 1 ) {//!右=东
      if( 是否放前 == 1 ) { 放块坐 = bpos.east( ); pl主手放块( pl1, 放块坐 ); }/*if*/
      if( 是否放后 == 1 ) { 放块坐 = bpos.west( ); pl主手放块( pl1, 放块坐 ); }/*if*/
      if( 是否放左 == 1 ) { 放块坐 = bpos.north( ); pl主手放块( pl1, 放块坐 ); }/*if*/
      if( 是否放右 == 1 ) { 放块坐 = bpos.south( ); pl主手放块( pl1, 放块坐 ); }/*if*/
    } else if( pl朝向.getStepX( ) == 0 ) {
      if( pl朝向.getStepZ( ) == 1 ) {//!南
        if( 是否放前 == 1 ) { 放块坐 = bpos.south( ); pl主手放块( pl1, 放块坐 ); }/*if*/
        if( 是否放后 == 1 ) { 放块坐 = bpos.north( ); pl主手放块( pl1, 放块坐 ); }/*if*/
        if( 是否放左 == 1 ) { 放块坐 = bpos.east( ); pl主手放块( pl1, 放块坐 ); }/*if*/
        if( 是否放右 == 1 ) { 放块坐 = bpos.west( ); pl主手放块( pl1, 放块坐 ); }/*if*/
        放块坐 = bpos.south( ); pl主手放块( pl1, 放块坐 );
      } else if( pl朝向.getStepZ( ) == -1 ) {//!北
        if( 是否放前 == 1 ) { 放块坐 = bpos.north( ); pl主手放块( pl1, 放块坐 ); }/*if*/
        if( 是否放后 == 1 ) { 放块坐 = bpos.south( ); pl主手放块( pl1, 放块坐 ); }/*if*/
        if( 是否放左 == 1 ) { 放块坐 = bpos.west( ); pl主手放块( pl1, 放块坐 ); }/*if*/
        if( 是否放右 == 1 ) { 放块坐 = bpos.east( ); pl主手放块( pl1, 放块坐 ); }/*if*/
        放块坐 = bpos.north( ); pl主手放块( pl1, 放块坐 );
      }/*if*/
    }/*if*/
  }/*void*/
  //打在这=(c保存,闪退)  --当pl  (双击是点,点,点点)
  public void PIEvζ右键点块ζ放块在块前(/*1*/ PlayerInteractEvent.RightClickBlock 事 ) {/*2026年2月27日09时22分03*/
    从configG量( );
    ///事.getLevel()
    //块是否能在该位置放置
    //*Block b1; b1.canSurvive( b1.defaultBlockState( ), 事.getLevel(), 事.getPos( ) );
    Player pl1 = 事.getEntity( );
    Position pl1Pos = pl1.position( );//足坐 //todo 2026年2月27日09时17分50__和身高有关吗？
    ///pl1./*c*/displayClientMessage( Component.translatable( "co1", pl1Pos.y( ) ), false ); pl1./*c*/displayClientMessage( Component.translatable( "co1", 事.getPos( ).getY( ) ), false );
    if( ( 前算.是否差小且大负d2( 事.getPos( ).getY( ), pl1Pos.y( ) - 1,if坐差小这Z触ζy ) == 1 ) || ( 前算.是否差小且大负d2( 事.getPos( ).getY( ), pl1Pos.y( ) + 2,if坐差小这Z触ζy ) == 1 ) ) {//!y,if下上
      if( 前算.是否坐xz差小且大负d( 事.getPos( ), pl1Pos ,if坐差小这Z触ζxz) == 1 ) {//!x,z
        据pl朝向主手放块( pl1, 事.getPos( ) );
      }/*if*/
    }/*if*/
    //*if(/*点脚下=>触`放块*/ 前算.是否坐同( 事.getPos( ), pl1.getOnPos( ) ) == 1 ) {/*2026年2月27日03时09分56*/    }/*if*/
    ///是否刚if放块在块前A将if补块 = 1; bpos右键事块_在这if放块在块前 = 事.getPos( );//用来`if补块;    以事件方块为准; 也可`以脚下方块为准`那就放在隔壁了
    ///记tζif放块在块前 = pl1.tickCount;
  }/*void*/
  /**/;//▬弃▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  //!太难了(不知道什么时候减少，要在玩家下次放完怪之前判断;)
  //!主手物品减少=放块=>不触, 不减少=没放块=>触发
  //主手物uζ放块前=主手物uζ上1,主手物uζ放块后=主手物u
  public void PIEvζ右键点块ζif主手物减Z放块在块前(/*3*/ Player pl1, BlockPos bpos右键事件块在这if放块在块前1, int 主手物uζ放块前,/*出*/int 主手物uζ放块后 ) {/*2026年2月27日08时32分08*/
    if(/*物没减=>触`放块*/ 主手物uζ放块前 <= 主手物uζ放块后 ) {//todo 2026年2月27日07时54分55__如果(获得了方块，导致没减少)怎么办？
      据pl朝向主手放块( pl1, bpos右键事件块在这if放块在块前1 );
    }/*if*/
  }/*void*/

}/*class*/