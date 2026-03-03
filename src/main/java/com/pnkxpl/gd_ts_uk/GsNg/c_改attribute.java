package com.pnkxpl.gd_ts_uk;

import com.ibm.icu.impl.coll.UVector32;
import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.libJava.c_前置计算;
import com.pnkxpl.gd_ts_uk.libJava.c_类型转换;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
//import static com.pnkxpl.c_跳_ts_tpac_跳.core.WanderingTraderExpressDelivery.MODID;
//*final and =&&

/*名__placeFront  c_放块在块前=放在块前*/
//!右键-->if air=>放!
public class c_改attribute {
  public static c_前置计算 前算 = new c_前置计算( );
  public static c_类型转换 类转 = new c_类型转换( );
  //!记录__设置
  static int 设ζt隔 = 20;
  static int 设ζ启ζhp = 0;
  static int 设ζ启ζwalkSpeed = 0;
  static int 设ζ启ζflySpeed = 0;
  static int 设ζ启ζstepHight = 0;
  static int 设ζ启ζattackSpeed = 0;
  static int 设ζ启ζsweep = 0;
  //!下标
  static int 一hp = 0;
  static int 一walkSpeed = 1;
  static int 一flySpeed = 2;
  static int 一stepHight = 3;
  static int 一attackSpeed = 4;
  static int 一sweep = 5;
  //!数值
  static float 跑时walkSpeed乘 = 0, 跑时flySpeed乘 = 0, flySpeed = 2, flySpeedζ跑 = 2;
  static double hp = 0, walkSpeed = 1, walkSpeedζ跑 = 1, stepHight = 3, attackSpeed = 4, sweep = 5;

  //!记录
  static int tζ上1 = -1500;
  static int 是否跑 = 0;
  static int 是否跑ζ上1 = 0;
  static int 是否本次跑已改 = 0, 是否本次走已改 = 0;

  public static void 从configG量( ) {/*2026年2月28日01时09分19*/
    设ζt隔 = 类转.rI( Config.设ζattributeζt隔.get( ).get( 0 ) );
    设ζ启ζhp = Config.设ζattributeζ启.get( ).get( 一hp );
    设ζ启ζwalkSpeed = Config.设ζattributeζ启.get( ).get( 一walkSpeed );
    设ζ启ζflySpeed = Config.设ζattributeζ启.get( ).get( 一flySpeed );
    设ζ启ζstepHight = Config.设ζattributeζ启.get( ).get( 一stepHight );
    设ζ启ζattackSpeed = Config.设ζattributeζ启.get( ).get( 一attackSpeed );
    设ζ启ζsweep = Config.设ζattributeζ启.get( ).get( 一sweep );
    hp = 类转.rD( Config.设ζattribute.get( ).get( 一hp ) );
    walkSpeed = 类转.rD( Config.设ζattribute.get( ).get( 一walkSpeed ) );
    flySpeed = 类转.rF( Config.设ζattribute.get( ).get( 一flySpeed ) );
    跑时walkSpeed乘 = 类转.rF( Config.跑时walkSpeed乘.get( ).get( 0 ) );
    跑时flySpeed乘 = 类转.rF( Config.跑时flySpeed乘.get( ).get( 0 ) );
    walkSpeedζ跑 = walkSpeed * 跑时walkSpeed乘; flySpeedζ跑 = flySpeed * 跑时flySpeed乘;
  }/*void*/
  public static void 读A改_无移速( Player pl1 ) {/*2026年2月28日01时12分43*/
    if( pl1.tickCount - tζ上1 >= 设ζt隔 ) {//!时隔够=>彳亍
      tζ上1 = pl1.tickCount;
      从configG量( );
      ///pl1./*c*/displayClientMessage( Component.translatable( "co1", Double.valueOf( Config.设ζattribute.get( ).get( 一hp ) ) ), false );
      ///pl1./*c*/displayClientMessage( Component.translatable( "co1", Double.valueOf( Config.设ζattribute.get( ).get( 一hp ) ) ), false );
      if(/*启=>改*/设ζ启ζhp == 1 ) { pl1.getAttribute( Attributes.MAX_HEALTH ).setBaseValue( hp ); }/*if*/
      if(/*启=>改*/设ζ启ζstepHight == 1 ) { pl1.getAttribute( Attributes.STEP_HEIGHT ).setBaseValue( Double.valueOf( Config.设ζattribute.get( ).get( 一stepHight ) ) ); }/*if*/
      if(/*启=>改*/设ζ启ζattackSpeed == 1 ) { pl1.getAttribute( Attributes.ATTACK_SPEED ).setBaseValue( Double.valueOf( Config.设ζattribute.get( ).get( 一attackSpeed ) ) ); }/*if*/
      if(/*启=>改*/设ζ启ζsweep == 1 ) { pl1.getAttribute( Attributes.SWEEPING_DAMAGE_RATIO ).setBaseValue( Double.valueOf( Config.设ζattribute.get( ).get( 一sweep ) ) ); }/*if*/
      改移速( pl1 );
    } /*if*/
  }/*void*/
  public static void 改移速( Player pl1 ) {/*2026年3月2日06时25分32*/
    if(/*疾跑*/ pl1.isSprinting( ) ) {
      ///是否跑ζ上1 = 是否跑; 是否跑 = 1;/*2026年3月2日06时40分44*/
      if( 是否本次跑已改 == 0 ) {
        是否本次跑已改 = 1; 是否本次走已改 = 0;
        if(/*启=>改*/设ζ启ζwalkSpeed == 1 ) { pl1.getAttribute( Attributes.MOVEMENT_SPEED ).setBaseValue( walkSpeedζ跑 ); }/*if*/
        if(/*启=>改*/设ζ启ζflySpeed == 1 ) { pl1.getAbilities( ).setFlyingSpeed( flySpeedζ跑 ); }/*if*/
      }/*if*/
    } else {/*不疾跑*/
      ///是否跑ζ上1 = 是否跑; 是否跑 = 0; 是否本次跑已改 = 0;
      if( 是否本次走已改 == 0 ) {/*2026年3月2日07时34分29*/
        是否本次走已改 = 1; 是否本次跑已改 = 0;
        if(/*启=>改*/设ζ启ζwalkSpeed == 1 ) { pl1.getAttribute( Attributes.MOVEMENT_SPEED ).setBaseValue( walkSpeed ); }/*if*/
        if(/*启=>改*/设ζ启ζflySpeed == 1 ) { pl1.getAbilities( ).setFlyingSpeed( flySpeed ); }/*if*/
      }/*if*/
    }/*if*/

  }/*void*/
  /**/;//!从`豆包▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  public static void 改hp大限( Player pl1, double d1 ) {/*2026年2月28日00时49分54*/
    // 获取最大HP属性实例
    var maxHealthAttribute = pl1.getAttribute( Attributes.MAX_HEALTH );
    if( maxHealthAttribute != null ) {
      // 方法1：直接设置基础值（永久修改）
      maxHealthAttribute.setBaseValue( d1 ); // 40点 = 20颗心，默认20.0D[[__LINK_ICON]](https://docs.neoforged.net/docs/entities/livingentity/?f_link_type=f_linkinlinenote&flow_extra=eyJpbmxpbmVfZGlzcGxheV9wb3NpdGlvbiI6MCwiZG9jX3Bvc2l0aW9uIjowLCJkb2NfaWQiOiI0M2Y0ZDA2MTdlZjJmYTI4Lc_跳1MjY4MjBkYmFjZmY3OWQifQ%3D%3D&inline_doc_id=43f4d0617ef2fa28-7526820dbacff79d)
      {//修改后同步HP，避免当前HP超过新的最大HP
        //pl1.setHealth( pl1.getMaxHealth( ) );
      }
    }/*if*/
  }/*void*/

  /**/;//▬弃▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬

}/*class*/