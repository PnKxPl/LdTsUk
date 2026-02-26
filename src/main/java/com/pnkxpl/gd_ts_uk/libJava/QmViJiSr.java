package com.pnkxpl.gd_ts_uk.libJava;
import java.util.ArrayList;

import com.ibm.icu.impl.coll.UVector32;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Vector;
//import static com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery.MODID;

/*名__c_QmViJiSr=前置计算*/
public class QmViJiSr {
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
  /**/;//▬读取属性▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
  public int 是否坐同( BlockPos bp1, BlockPos bp2 ) {/*2026年2月27日02时10分36*/
    if( bp1==null || bp2==null ) {return -1;}/*if*/
    if(bp1.getY() == bp2.getY() && bp1.getX() == bp2.getX() && bp1.getZ() == bp2.getZ() ) {
      return 1;
    }/*if*/
    return 0;
  }/*void*/



}/*class*/