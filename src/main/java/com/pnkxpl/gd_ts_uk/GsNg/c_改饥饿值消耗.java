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
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
//*import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.block;
import static net.minecraft.world.InteractionHand.MAIN_HAND;
import static net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED;
import static net.minecraft.world.effect.MobEffects.REGENERATION;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.MAX_DISTANCE;
public class c_改饥饿值消耗 {
  //函__pl.(causeFoodExhaustion=饥减   )
  public static void 饥加(/*3*/ Player pl1, int 饥, float 饱 ) {/*2026年3月2日04时41分57*/
    pl1.getFoodData( ).eat( 饥, ( float ) 饱 );
  }/*void*/
  public static void if跑Z饥加(/*3*/ Player pl1, int 饥, float 饱 ) {/*2026年3月2日04时35分05*/
    if( pl1.isSprinting( ) ) {
      pl1.getFoodData( ).eat( 饥, ( float ) 饱 );
    }/*if*/
  }/*void*/

  public static void _2026年3月2日04时09分36( Player pl1, float f1 ) {
  }/*void*/

}
