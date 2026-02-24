package com.pnkxpl.gd_ts_uk.core;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;
public class Config {
  private static final ModConfigSpec.Builder bui读设件 = new ModConfigSpec.Builder( );
  //!填
  public static final ModConfigSpec.IntValue 说明 = bui读设件
      .comment( "//名__g=get; t=tick;    " )
      .comment("//gPlayer --> onTick__for:gEntities(检rX,检rZ,检rY)( "
          ,"if( lightValue>if亮大这Z烧怪 )=>burn, hurt( inWall, (怪块亮级 -出伤ζ亮减) *出伤乘 )"
      ,")    ")
      .translation( "" )
      .defineInRange( "", 0, 0, 15000 );
  /*!记录__设置*/
  public static final ModConfigSpec.IntValue 在首几t不彳亍 = bui读设件
      .defineInRange( "(没用)在首几t不彳亍", 15, 0, 15000 );
  public static final ModConfigSpec.IntValue if亮大这Z烧怪 = bui读设件
      .defineInRange( "if亮大这Z烧怪", 10, 0, 15000 );
  public static final ModConfigSpec.IntValue 出伤ζ亮减 = bui读设件
      .defineInRange( "出伤ζ亮减", 10, 0, 15000 );
  public static final ModConfigSpec.IntValue 出伤乘 = bui读设件
      .defineInRange( "出伤乘", 4, 0, 15000 );
  public static final ModConfigSpec.IntValue 设ζt隔ζ检ent = bui读设件
      .defineInRange( "设ζt隔ζ检ent", 20, 0, 15000 );
  public static final ModConfigSpec.IntValue 检rX = bui读设件
      .defineInRange( "检rX", 64, 0, 15000 );
  public static final ModConfigSpec.IntValue 检rZ = bui读设件
      .defineInRange( "检rZ", 64, 0, 15000 );
  public static final ModConfigSpec.IntValue 检rY = bui读设件
      .defineInRange( "检rY", 32, 0, 15000 );

  public static final ModConfigSpec SPEC = bui读设件.build( );/*上移=进存闪退*/

  //!r
  public static int r在首几t不彳亍( ) { return 在首几t不彳亍.get( ); }/*int*/
  public static int r出伤ζ亮减( ) { return 出伤ζ亮减.get( ); }/*int*/
  public static int r出伤乘( ) { return 出伤乘.get( ); }/*int*/
  public static int r设ζt隔ζ检ent( ) { return 设ζt隔ζ检ent.get( ); }/*int*/
  public static int r检rX( ) { return 检rX.get( ); }/*int*/
  public static int r检rZ( ) { return 检rZ.get( ); }/*int*/
  public static int r检rY( ) { return 检rY.get( ); }/*int*/


  public static long r分转ticks( double minutes ) {
    return ( long ) ( minutes * 60 * 20 );
  }

};