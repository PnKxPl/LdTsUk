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
      .comment( "//gPlayer --> onTick__for:gEntities(检rX,检rZ,检rY)( "
          , "if( lightValue>if亮大这Z烧怪 )=>( burn, if( lightValue>出伤ζ亮减 )=>hurt( inWall, (lightValue -出伤ζ亮减) *出伤乘 ) )"
          , ")    " )
      .translation( "" )
      .defineInRange( "", 0, 0, 15000 );/*max不<min*/
  /*!记录__设置*/
  public static final ModConfigSpec.ConfigValue< List< Integer > > 测试list = bui读设件
      .comment( "Spawn position (x,y,z)" )
      .define( "spawn_pos", List.of( 100, 64, 200 ) );
  public static final ModConfigSpec.ConfigValue< List< ? extends Integer > > intList = bui读设件
      .comment( "这是一个整数列表，例如：[1, 2, 3, 4]" )
      .translation( "modid.config.int_list" )
      .defineList( "int_list",
          Arrays.asList( 10, 20, 30 ),//!默认值
          o -> o instanceof Integer );

  public static final ModConfigSpec.IntValue 设ζ启ζif亮Z烧怪 = bui读设件
      .defineInRange( "设ζ启ζif亮Z烧怪亍", 1, 0, 1 );
  public static final ModConfigSpec.IntValue 在首几t不彳亍 = bui读设件
      .defineInRange( "(没用)在首几t不彳亍", 15, 0, 15000 );

  public static final ModConfigSpec.IntValue if亮大这Z烧怪 = bui读设件
      .defineInRange( "if亮大这Z烧怪", 6, -15000, 15000 );
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
  //✅ 完整的日志输出控制开关
  public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGS = bui读设件
      .comment( "启用调试日志输出" )
      .translation( "config.wandering_trader_express_delivery.enable_debug_logs" )
      .define( "enableDebugLogs", false );
  public static final ModConfigSpec.BooleanValue ENABLE_INFO_LOGS = bui读设件
      .comment( "启用信息日志输出" )
      .translation( "config.wandering_trader_express_delivery.enable_info_logs" )
      .define( "enableInfoLogs", false );
  public static final ModConfigSpec.BooleanValue ENABLE_WARN_LOGS = bui读设件
      .comment( "启用警告日志输出" )
      .translation( "config.wandering_trader_express_delivery.enable_warn_logs" )
      .define( "enableWarnLogs", false );
  public static final ModConfigSpec.BooleanValue ENABLE_ERROR_LOGS = bui读设件
      .comment( "启用错误日志输出" )
      .translation( "config.wandering_trader_express_delivery.enable_error_logs" )
      .define( "enableErrorLogs", true );

  public static final ModConfigSpec SPEC = bui读设件.build( );/*上移=进存闪退*/
  //!r
  public static int r在首几t不彳亍( ) { return 在首几t不彳亍.get( ); }/*int*/
  public static int r出伤ζ亮减( ) { return 出伤ζ亮减.get( ); }/*int*/
  public static int r出伤乘( ) { return 出伤乘.get( ); }/*int*/
  public static int r设ζt隔ζ检ent( ) { return 设ζt隔ζ检ent.get( ); }/*int*/
  public static int r检rX( ) { return 检rX.get( ); }/*int*/
  public static int r检rZ( ) { return 检rZ.get( ); }/*int*/
  public static int r检rY( ) { return 检rY.get( ); }/*int*/
  //✅ 完整的日志开关获取方法
  public static boolean enableDebugLogs( ) {
    return ENABLE_DEBUG_LOGS.get( );
  }
  public static boolean enableInfoLogs( ) {
    return ENABLE_INFO_LOGS.get( );
  }
  public static boolean enableWarnLogs( ) {
    return ENABLE_WARN_LOGS.get( );
  }
  public static boolean enableErrorLogs( ) {
    return ENABLE_ERROR_LOGS.get( );
  }

  public static long r分转ticks( double minutes ) {
    return ( long ) ( minutes * 60 * 20 );
  }
};