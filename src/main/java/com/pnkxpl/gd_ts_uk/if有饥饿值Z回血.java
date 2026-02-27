package com.pnkxpl.gd_ts_uk;

import com.ibm.icu.impl.coll.UVector32;
import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.libJava.QmViJiSr;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.effect.MobEffectInstance;
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
import static net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED;
import static net.minecraft.world.effect.MobEffects.REGENERATION;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.MAX_DISTANCE;
//import static com.pnkxpl.tc_ts_tpatc.core.WanderingTraderExpressDelivery.MODID;
//*final and =&&

public class if有饥饿值Z回血 {
  //!记录__设置
  static int 设ζt隔 = 20;

  //!记录
  static int tζ上1 = -1500;
  public static void 从configG量( ) {
    设ζt隔 = Config.设ζif有饥饿值Z回血ζt隔.get( );
  }/*void*/
  public static void 读A改( Player pl1 ) {/*2026年2月28日03时27分46*/
    if( pl1.tickCount - tζ上1 >= 设ζt隔 ) {//!时隔够=>彳亍
      tζ上1 = pl1.tickCount;
      从configG量( );
      aRegeneration( pl1 );
    } else if(/*重置*/ pl1.tickCount < tζ上1 ) { tζ上1 = 0; }/*if*/
  }/*void*/
  /**/;//!从`豆包▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  //!21t=不回血, 50t=回1
  public static void aRegeneration( Player pl1 ) {/*2026年2月28日03时16分59*/
    MobEffectInstance ef1 = new MobEffectInstance(
        REGENERATION,  // 效果（自己换）
        50,                // 时长（tick）20=1秒 → 10秒
        0,                      // 等级 0=1级, 1=2级...
        true,                  // 是否隐藏粒子
        false                    // 是否显示图标
    );
    pl1.addEffect( ef1 );
  }/*void*/
  /**/;//▬弃▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬

}/*class*/