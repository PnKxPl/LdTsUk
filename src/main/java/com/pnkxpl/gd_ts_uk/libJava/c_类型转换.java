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

/*名__Cs_DbBk=从豆包*/
public class c_类型转换 {
  public int rI( String s1 ) { return Integer.parseInt( s1 ); }/*int*/
  public float rF( String s1 ) { return Float.parseFloat( s1 ); }/*float*/
  public double rD( String s1 ) { return Double.parseDouble( s1 ); }/*double*/


}/*class*/