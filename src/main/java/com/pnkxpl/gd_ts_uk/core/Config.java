package com.pnkxpl.gd_ts_uk.core;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;
public class Config {
  //!Builder只能注册1个
  private static final ModConfigSpec.Builder bui读设件ζif亮Z烧怪 = new ModConfigSpec.Builder( );
  //*private static final ModConfigSpec.Builder bui读设件ζ跳同tpA跳 = new ModConfigSpec.Builder( );
  //*private static final ModConfigSpec.Builder bui读设件ζ放块在块前 = new ModConfigSpec.Builder( );
  //!填
  //✅ 完整的日志输出控制开关
  public static final ModConfigSpec.ConfigValue< List< ? extends Boolean > > ENABLE_DEBUG_LOGS__ENABLE_INFO_LOGS__ENABLE_WARN_LOGS__ENABLE_ERROR_LOGS = bui读设件ζif亮Z烧怪
      .defineList( "( ENABLE_DEBUG_LOGS, ENABLE_INFO_LOGS, ENABLE_WARN_LOGS, ENABLE_ERROR_LOGS )", Arrays.asList( false, false, false, true ),
          o -> o instanceof Boolean );
  public static final ModConfigSpec.Builder 分段1说明 = bui读设件ζif亮Z烧怪
      .comment( "//名__g =get;    t =tick;    设ζ启 =设置ζ是否开启(0=关,1=开)(0=off,1=on)   " )
      .push( "if亮Z烧怪    light =burnMonster" )
      .comment( "//gPlayer --> onTick__for:gEntities(检rX,检rZ,检rY)( "
          , "if( lightValue>if亮大这Z烧怪 )=>( burn, if( lightValue>出伤ζ亮减 )=>hurt( inWall, (lightValue -出伤ζ亮减) *出伤乘 ) )"
          , ")    " );
  /*!记录__设置*/
  /// public static final ModConfigSpec.IntValue 在首几t不彳亍 = bui读设件ζif亮Z烧怪.defineInRange( "(没用)在首几t不彳亍", 15, 0, 15000 );
  public static final ModConfigSpec.IntValue 设ζ启ζif亮Z烧怪 = bui读设件ζif亮Z烧怪.defineInRange( "设ζ启", 1, 0, 1 );
  public static final ModConfigSpec.ConfigValue< List< ? extends Integer > > if亮大这Z烧怪__出伤ζ亮减__出伤 = bui读设件ζif亮Z烧怪
      .defineList( "( if亮大这Z烧怪, 出伤ζ亮减, 出伤乘 )", Arrays.asList( 6, 10, 4 ),//!默认值
          o -> o instanceof Integer );
  public static final ModConfigSpec.ConfigValue< List< ? extends Integer > > 设ζt隔ζ检ent__检rY__检rX__检rZ = bui读设件ζif亮Z烧怪
      .defineList( "( t隔ζ检ent, 检rY, 检rX, 检rZ乘 )", Arrays.asList( 20, 32, 64, 64 ),
          o -> o instanceof Integer );

  public static final ModConfigSpec.Builder 分段2 = bui读设件ζif亮Z烧怪.pop( ).push( "跳 =tpA跳    jump =(tp ~ ~1 ~)andJump" );
  public static final ModConfigSpec.IntValue 设ζ启ζ跳同tpA跳 = bui读设件ζif亮Z烧怪.defineInRange( "设ζ启", 1, 0, 1 );

  public static final ModConfigSpec.Builder 分段3 = bui读设件ζif亮Z烧怪.pop( ).push( "放块在块前    placeFront " );
  public static final ModConfigSpec.IntValue 设ζ启ζ放块在块前 = bui读设件ζif亮Z烧怪.defineInRange( "设ζ启", 1, 0, 1 );
  public static final ModConfigSpec.ConfigValue< List< ? extends Integer > > 设ζ多选ζ放块在块の向 = bui读设件ζif亮Z烧怪
      ///.comment( "设ζ多选ζ放块在块の向__0=全, 1=北, 2=南, 3=西, 4=东    0=all, 1=north, 2=south, 3=west, 4=east")
      .comment( "设ζ多选ζ放块在块の向__0=全, 1=前, 2=后, 3=左, 4=右    0=all, 1=fornt, 2=back, 3=left, 4=right" )
      .defineList( "( 5元 )", Arrays.asList( 1, 1, 1, 1, 1 ),
          o -> o instanceof Integer );

  public static final ModConfigSpec.Builder 分段ζattribute = bui读设件ζif亮Z烧怪.pop( ).push( "attribute" );
  public static final ModConfigSpec.IntValue 设ζ启ζattribute = bui读设件ζif亮Z烧怪.defineInRange( "设ζ启", 1, 0, 1 );
  public static final ModConfigSpec.IntValue 设ζattributeζt隔 = bui读设件ζif亮Z烧怪.defineInRange( "t隔", 50, 0, 15000 );
  public static final ModConfigSpec.ConfigValue< List< ? extends Integer > > 设ζattributeζ启 = bui读设件ζif亮Z烧怪
      .defineList( "启( hp, walkSpeed, flySpeed, attackSpeed, sweep )", Arrays.asList( 1, 1, 1, 1, 1 ),//*Arrays.asList( 1,1,1,1,1),
          o -> o instanceof Integer );
  //!方注__(Number o -> o instanceof( (Integer)是整u /(Double)必须<1) )); 不能(( double )Number)
  //*public static final ModConfigSpec.ConfigValue< ? extends List< ? extends Number > > 设ζattribute = bui读设件ζif亮Z烧怪
  //*    .defineList( "值( hp, walkSpeed, flySpeed, attackSpeed, sweep )", Arrays.asList( 100.1, 0.15, 0.2, -1.1, 1.1 ),
  //*        o -> o instanceof Double );
  public static final ModConfigSpec.ConfigValue< ? extends List< ? extends String > > 设ζattribute = bui读设件ζif亮Z烧怪
      .defineList( "值( hp, walkSpeed, flySpeed, attackSpeed, sweep )", Arrays.asList( "100", "0.15", "0.2", "-1", "1" ),
          o -> o instanceof String );

  public static final ModConfigSpec SPEC1 = bui读设件ζif亮Z烧怪.build( );/*上移=进存闪退*/
  //*public static final ModConfigSpec SPEC2 = bui读设件ζ跳同tpA跳.build( );
  //*public static final ModConfigSpec SPEC3 = bui读设件ζ放块在块前.build( );


  //!r
  //✅ 完整的日志开关获取方法
  public static boolean enableDebugLogs( ) {
    return ENABLE_DEBUG_LOGS__ENABLE_INFO_LOGS__ENABLE_WARN_LOGS__ENABLE_ERROR_LOGS.get( ).get( 0 );
  }
  public static boolean enableInfoLogs( ) { return ENABLE_DEBUG_LOGS__ENABLE_INFO_LOGS__ENABLE_WARN_LOGS__ENABLE_ERROR_LOGS.get( ).get( 1 ); }
  public static boolean enableWarnLogs( ) { return ENABLE_DEBUG_LOGS__ENABLE_INFO_LOGS__ENABLE_WARN_LOGS__ENABLE_ERROR_LOGS.get( ).get( 2 ); }
  public static boolean enableErrorLogs( ) { return ENABLE_DEBUG_LOGS__ENABLE_INFO_LOGS__ENABLE_WARN_LOGS__ENABLE_ERROR_LOGS.get( ).get( 3 ); }

  public static long r分转tick( double minutes ) {
    return ( long ) ( minutes * 60 * 20 );
  }
};