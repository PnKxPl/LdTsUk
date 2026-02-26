2026年2月21日__neoForge记录.c
👍Neoforged Events List Chinese | Neoforged 事件列表的中文翻译  https://duoduo70.github.io/Neoforged-Events-List-Chinese/
模组生成器 - NeoForged 项目--- Mod Generator - The NeoForged project - Google Chrome 测试版  https://neoforged.net/mod-generator/
//问句格式__neoforge+<>;    注__名有空格=能识别(左要};)(目录内名=右1,绿色显示在左1);
struct 豆包{
  struct onRightClickBlock{/*2026年2月21日05时2413秒*/
    我给你一份NeoForge 1.20.x 最标准、最稳定、直接能用的
      onRightClickBlock  写法 + 常见用法模板，你直接复制改一改就能用。
      1. 事件完整写法（最常用）
      java
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
      public class YourEventHandler{
      @SubscribeEvent
        public void onRightClickBlock( RightClickBlock event ){
        //只在服务器执行（99% 逻辑都放这里）
        if( event.getLevel( ).isClientSide( ) ) return;
        Player player = event.getEntity( );
        Level level = event.getLevel( );
        BlockPos pos = event.getPos( );
        BlockState state = level.getBlockState( pos );
        //========== 你的逻辑写这里 ==========
        //示例：右键方块时发送消息
        player.sendSystemMessage( Component.literal(
          "你右键了: " + state.getBlock( ).getName( ).getString( )
        ) );
        //如果你想**取消原版行为**（比如不让开箱子、不让用床）
        //event.setCanceled(true);
      }
    }
    2. 必须在 MOD 主类注册
      java
      @Mod( "your_mod_id" )
      public class YourModClass{
      public YourModClass( ){
        NeoForge.EVENT_BUS.register(new YourEventHandler( ));
      }
    }
    3. 常用变量速查
      -  event.getEntity( )  → 玩家
      -  event.getLevel( )  → 世界
      -  event.getPos( )  → 被点击的方块坐标
      -  event.getFace( )  → 点击的面（上/下/东/西/南/北）
      -  event.getHitVec( )  → 点击位置的精确坐标
      -  event.getItemStack( )  → 玩家手里的物品
      -  event.setCanceled( true )  → 取消原版右键行为
      4. 超实用示例：判断点击的方块
      java
      if( state.is( Blocks.CHEST ) ){
        player.sendSystemMessage( Component.literal( "你点了箱子!" ) );
        event.setCanceled( true ); //不让打开
      }
    你要做什么功能？
      比如：右键放技能、右键触发红石、右键给物品、右键破坏方块？我可以直接给你对应成品代码。
  };
  struct RegisterTicketControllersEvent{/*2026年2月21日05时34分14*/
    RegisterTicketControllersEvent  是 NeoForge 1.20.1+ 用于注册自定义区块加载 Ticket 控制器的事件，用于实现区块强制加载（ChunkLoader）、实体/方块持久化加载等功能 。
      一、核心概念
      - TicketController：管理一组区块加载 Ticket 的控制器，支持按实体/方块/UUID作为 Owner 管理 Ticket
      - Ticket：区块加载凭证，控制区块是否强制加载、是否强制随机刻
      - RegisterTicketControllersEvent：在**模组总线（modBus）**触发，用于注册自定义  TicketController 
      二、完整注册示例（可直接复制）
      1. 定义全局 TicketController（推荐）
      java
      //你的模组主类
      @Mod( "your_mod_id" )
      public class YourMod{
      //全局可访问的 TicketController（方块/实体作为Owner）
      public static final TicketController<BlockPos> BLOCK_TICKET_CONTROLLER =
        TicketController.forBlockPos( "your_mod_id:block_loader" );
      public static final TicketController<UUID> ENTITY_TICKET_CONTROLLER =
        TicketController.forUuid( "your_mod_id:entity_loader" );
      public YourMod( IEventBus modBus, IEventBus forgeBus ){
        //注册事件（必须在modBus）
        modBus.register(this);
      }
      //注册 TicketController（核心）
      @SubscribeEvent
        public void registerTicketControllers( RegisterTicketControllersEvent event ){
        event.register(BLOCK_TICKET_CONTROLLER);
        event.register(ENTITY_TICKET_CONTROLLER);
      }
    }
    2. 常用操作：添加/移除区块加载
      java
      //服务器端使用（必须！）
      public void forceLoadChunk( ServerLevel level, BlockPos ownerPos, ChunkPos chunkPos ){
      //获取控制器
      TicketController<BlockPos> controller = YourMod.BLOCK_TICKET_CONTROLLER;
      //添加Ticket：强制加载+强制随机刻（isForceTicks=true）
      controller.addTicket( level, ownerPos, chunkPos, 3, true );
    }
    public void unforceLoadChunk( ServerLevel level, BlockPos ownerPos, ChunkPos chunkPos ){
      TicketController<BlockPos> controller = YourMod.BLOCK_TICKET_CONTROLLER;
      //移除Ticket
      controller.removeTicket( level, ownerPos, chunkPos );
      //移除所有该Owner的Ticket
      //controller.removeAll(level, ownerPos);
    }
    三、关键方法速查
      java
      //添加Ticket
      controller.addTicket(
        ServerLevel level,       //世界
        OWNER owner,              //所有者（BlockPos/UUID）
        ChunkPos chunk,           //目标区块
        int level,                //加载等级（3=常规强制加载）
        boolean isForceTicks       //是否强制随机刻（方块/实体tick）
      );
    //移除Ticket
    controller.removeTicket( ServerLevel level, OWNER owner, ChunkPos chunk );
    //移除所有者所有Ticket
    controller.removeAll( ServerLevel level, OWNER owner );
    //检查是否加载
    controller.hasTicket( ServerLevel level, OWNER owner, ChunkPos chunk );
    四、重要注意事项
      - 必须服务器端：所有操作仅在  ServerLevel  有效，客户端调用无效果
      - Owner 类型： forBlockPos( ) （方块）、 forUuid( ) （实体/玩家）、 forLong( ) （自定义ID）
      - 加载等级： 3  = 强制加载（与  /forceload  同级）； 0  = 仅标记
      - isForceTicks： true  = 区块即使无玩家也会执行随机刻（方块/实体逻辑）
      - 生命周期：世界卸载时自动清理，无需手动处理
      五、常见场景
      - 自定义区块加载器方块：右键开启/关闭强制加载
      - 实体持久化：让实体在无玩家时仍保持加载与逻辑运行
      - 多区块机器：跨区块的多方块结构保持加载
      需要我给你写一个完整的自定义区块加载器方块示例（含右键开关、NBT保存、自动清理）吗？
  };
  struct EventHandler{/*2026年2月22日00时37分25*/
    NeoForge EventHandler 快速上手
      NeoForge 事件系统用于监听游戏行为（如玩家登录、实体受伤、方块破坏），核心是事件类 + 订阅方法 + 注册到总线 。
      一、核心概念
      - 事件总线：
      -  NeoForge.EVENT_BUS ：游戏运行时事件（实体、方块、玩家等）
      -  modBus ：模组生命周期事件（注册、配置加载）
      - 注解：
      -  @SubscribeEvent ：标记事件处理方法
      -  @EventBusSubscriber ：自动注册类到总线（推荐）
      二、写法一：自动注册（推荐）
      用  @EventBusSubscriber  自动注册，方法必须 static 。
      java
      package com.yourname.yourmod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
    //自动注册到事件总线，指定modid方便调试
    @EventBusSubscriber( modid = "yourmodid" )
      public class ModEvents{
      //玩家登录事件
      @SubscribeEvent
        public static void onPlayerLogin( PlayerEvent.PlayerLoggedInEvent event ){
        event.getEntity( ).sendSystemMessage( net.minecraft.network.chat.Component.literal( "欢迎回来！" ) );
      }
      //实体受伤事件（服务器端生效）
      @SubscribeEvent
        public static void onLivingDamage( LivingDamageEvent event ){
        if( !event.getEntity( ).level( ).isClientSide( ) ){
          //受伤时额外回1点血
          event.getEntity( ).heal( 1.0F );
        }
      }
    }
    三、写法二：手动注册
      在主类构造器手动注册，适合非静态方法（实例注册） 。
      java
      package com.yourname.yourmod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
    @Mod( "yourmodid" )
      public class YourMod{
      public YourMod( IEventBus modBus ){
        //手动注册事件类
        NeoForge.EVENT_BUS.register(ModEvents.class);
        //也可注册实例（方法非static）
        //NeoForge.EVENT_BUS.register(new ModEvents());
      }
    }
    四、常见事件类型
      - 实体： LivingDamageEvent 、 LivingDeathEvent 、 LivingHealEvent 
      - 玩家： PlayerLoggedInEvent 、 PlayerInteractEvent 、 PlayerBreakSpeedEvent 
      - 方块： BlockBreakEvent 、 BlockPlaceEvent 、 BlockStateChangeEvent 
      - 世界： LevelLoadEvent 、 ChunkDataEvent.Load 
      - 注册： RegisterEvent （物品/方块/实体注册）
      五、关键规则
      1. 方法参数必须是具体事件类（不能是父类  Event ）
      2.  @EventBusSubscriber  注册的方法必须 static
      3. 区分客户端/服务器：用  level( ).isClientSide( )  判断
      4. 新版 NeoForge（≥21.6.6）无需指定  bus  参数，自动识别
      六、取消/修改事件
      多数事件可取消或修改结果：
      java
      @SubscribeEvent
      public static void onBlockBreak( BlockBreakEvent event ){
      //取消破坏
      event.setCanceled( true );
      //修改掉落物
      event.setDrops(/* 自定义物品列表 */ );
    }
    需要我给你一个可直接复制的完整模板（含主类+事件类+常用事件示例）吗？
  };
  struct 修改生物ai{/*2026年2月22日05时02分56*/
    在 NeoForge 里修改生物 AI，核心是操作 goalSelector（行为目标）与 targetSelector（仇恨目标），或用 Brain（1.19+ 新系统），下面给你完整可直接用的写法。
      一、基础概念（必看）
      - MobEntity：所有有 AI 的生物基类（僵尸、村民、动物等）
      - GoalSelector：管理生物“做什么”（游荡、攻击、逃跑、繁殖）
      - TargetSelector：管理生物“打谁”（玩家、怪物、动物）
      - Goal：单个行为任务（如  MeleeAttackGoal 、 PanicGoal ）
      - 优先级：数字越小优先级越高（0最高）
      二、自定义生物：添加/修改 AI（最常用）
      1. 新建实体类（以僵尸为例）
      java
      public class CustomZombie extends Zombie{
      public CustomZombie( EntityType<? extends Zombie> type, Level level ){
        super( type, level );
      }
      //重写注册AI的方法
      @Override
        protected void registerGoals( ){
        //1. 先清空默认AI（可选，想完全重写就加）
        this.goalSelector.removeAllGoals( goal -> true );
        this.targetSelector.removeAllGoals( goal -> true );
        //2. 添加自定义行为（优先级从高到低）
        //攻击玩家（优先级2）
        this.goalSelector.addGoal( 2, new MeleeAttackGoal( this, 1.0D, true ) );
        //游荡（优先级5）
        this.goalSelector.addGoal( 5, new RandomStrollGoal( this, 0.8D ) );
        //看向玩家（优先级8）
        this.goalSelector.addGoal( 8, new LookAtPlayerGoal( this, Player.class, 8.0F ) );
        //随机看（优先级8）
        this.goalSelector.addGoal( 8, new RandomLookAroundGoal( this ) );
        //3. 添加仇恨目标（谁会被打）
        this.targetSelector.addGoal( 1, new HurtByTargetGoal( this ) );
        this.targetSelector.addGoal( 2, new NearestAttackableTargetGoal<>( this, Player.class, true ) );
      }
    }
    2. 注册你的实体（主类）
      java
      @Mod( ExampleMod.MOD_ID )
      public class ExampleMod{
      public static final String MOD_ID = "examplemod";
      public ExampleMod( ){
        IEventBus bus = FMLJavaModLoadingContext.get( ).getModEventBus( );
        bus.addListener( this::registerEntities );
      }
      private void registerEntities( final RegisterEntitiesEvent event ){
        event.register(EntityType.Builder.of( CustomZombie::new, MobCategory.MONSTER )
          .sized( 0.6F, 1.95F )
          .build( "custom_zombie" ));
      }
    }
    三、修改原版生物 AI（不新建实体）
      用 EntityJoinLevelEvent 拦截原版生物，动态修改其 AI。
      java
      @SubscribeEvent
      public void onEntityJoinWorld( EntityJoinLevelEvent event ){
      Level level = event.getLevel( );
      Entity entity = event.getEntity( );
      //只处理服务端、僵尸
      if( !level.isClientSide( ) && entity instanceof Zombie zombie ){
        //移除原版攻击目标
        zombie.targetSelector.removeAllGoals( g->g instanceof NearestAttackableTargetGoal );
        //添加新目标：只攻击村民
        zombie.targetSelector.addGoal( 2, new NearestAttackableTargetGoal<>( zombie, Villager.class, true ) );
        //移除原版游荡，改成更快游荡
        zombie.goalSelector.removeAllGoals( g->g instanceof RandomStrollGoal );
        zombie.goalSelector.addGoal( 5, new RandomStrollGoal( zombie, 1.2D ) );
      }
    }
    四、写一个完全自定义的 Goal（高级）
      让生物做原版没有的行为（比如靠近玩家就给 buff）。
      java
      //自定义Goal：给附近玩家加速度
      public class GiveSpeedGoal extends Goal{
      private final Mob mob;
      private static final int RANGE = 5;
      public GiveSpeedGoal( Mob mob ){
        this.mob = mob;
        //设置可中断（被攻击时会停止）
        this.setFlags( EnumSet.of( Flag.MOVE, Flag.LOOK ) );
      }
      //1. 是否可以开始执行
      @Override
        public boolean canUse( ){
        return !mob.isDeadOrDying( ) && mob.level.getNearestPlayer( mob, RANGE ) != null;
      }
      //2. 是否继续执行
      @Override
        public boolean canContinueToUse( ){
        return canUse( );
      }
      //3. 执行逻辑（每tick）
      @Override
        public void tick( ){
        Player player = mob.level.getNearestPlayer( mob, RANGE );
        if( player != null ){
          //给玩家加速度效果
          player.addEffect( new MobEffectInstance( MobEffects.MOVEMENT_SPEED, 20, 1, false, false ) );
        }
      }
    }
    //注册到生物
    this.goalSelector.addGoal( 3, new GiveSpeedGoal( this ) );
    五、1.19+ Brain 系统（村民/猪灵等）
      部分生物用 Brain 而非 GoalSelector，修改方式不同。
      java
      //以村民为例，修改其行为
      @SubscribeEvent
      public void onBrainInit( BrainEvent.Init event ){
      Brain<?> brain = event.getBrain( );
      LivingEntity entity = event.getEntity( );
      if( entity instanceof Villager ){
        //移除原版睡觉行为
        brain.removeAllBehaviors( );
        //添加自定义行为
        brain.addBehavior( Activity.IDLE, new RunOne<>(
          new InteractWithDoor( ),
          new CustomVillagerWork( ) //你的自定义行为
        ) );
      }
    }
    六、常用 Goal 速查
      - 攻击： MeleeAttackGoal 、 RangedAttackGoal 、 LeapAtTargetGoal 
      - 移动： RandomStrollGoal 、 WaterAvoidingRandomStrollGoal 、 FollowOwnerGoal 
      - 防御： PanicGoal 、 DefendVillageTargetGoal 
      - 社交： BreedGoal 、 TemptGoal （被食物吸引）
      - 目标： HurtByTargetGoal 、 NearestAttackableTargetGoal 、 OwnerHurtByTargetGoal 
      七、常见问题
      - AI 不生效：检查是否在服务端修改（ !level.isClientSide( ) ）、优先级是否冲突
      - 原版行为删不掉：用  goalSelector.removeAllGoals( g->g instanceof 目标Goal类 )
      - Brain 生物：村民、猪灵、蜂等，必须用 Brain API 而非 GoalSelector
      需要我给你一个可直接运行的完整示例（含自定义生物+自定义Goal+事件修改原版僵尸AI）吗？
  };
  struct 设置MobCategory{/*2026年2月22日13时12前*/
    在 NeoForge 中给实体设置 MobCategory（生物分类），核心是在 EntityType.Builder 中传入枚举值，决定生物的生成、消失、和平模式行为 。
      一、MobCategory 枚举（常用）
      java
      //net.minecraft.world.entity.MobCategory
      MobCategory.MONSTER;          //敌对生物（僵尸、骷髅）
    MobCategory.CREATURE;          //被动生物（羊、牛）
    MobCategory.AMBIENT;           //环境生物（蝙蝠）
    MobCategory.AXOLOTS;           //美西螈
    MobCategory.WATER_CREATURE;    //水生生物（鱼、鱿鱼）
    MobCategory.UNDERGROUND_WATER_CREATURE; //地下水生（发光鱿鱼）
    MobCategory.WATER_AMBIENT;     //水生环境（海豚）
    MobCategory.MISC;              //杂项（村民、铁傀儡）
    二、完整注册示例（NeoForge 标准）
      1. 实体类（示例：MyZombie）
      java
      //你的实体类
      public class MyZombie extends Zombie{
      public MyZombie( EntityType<? extends Zombie> type, Level level ){
        super( type, level );
      }
    }
    2. 实体注册类（关键：设置 MobCategory）
      java
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
      public class ModEntities{
      //1. 创建 DeferredRegister
      public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create( ForgeRegistries.ENTITY_TYPES, "你的模组ID" );
      //2. 注册实体：Builder.of(工厂, MobCategory)
      public static final RegistryObject<EntityType<MyZombie>> MY_ZOMBIE =
        ENTITIES.register("my_zombie", () ->
          EntityType.Builder.of( MyZombie::new, MobCategory.MONSTER )
          .sized( 0.6F, 1.95F )  //碰撞箱
          .build( "my_zombie" )
          );
    }
    3. 主类注册
      java
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
      @Mod( "你的模组ID" )
      public class MyMod{
      public MyMod( ){
        //注册实体
        ModEntities.ENTITIES.register(FMLJavaModLoadingContext.get( ).getModEventBus( ));
      }
    }
    三、常见用途
      - 生成规则：决定生物在哪些群系、何时生成
      - 消失规则：决定玩家远离后是否消失
      - 和平模式： CREATURE / AMBIENT  等在和平模式保留， MONSTER  被移除
      - 生成上限：每个分类有全局上限（如 MONSTER=70）
      四、常见错误
      - 忘记传入 MobCategory → 编译报错
      - 用错分类（如把被动生物设为 MONSTER）→ 和平模式消失
      - 旧版 Forge 写法（EntityClassification）→ NeoForge 已统一为 MobCategory
      需要我给你补充一个自定义 MobCategory（新增分类）的完整代码示例吗？
  };
  struct 让敌对生物变成被动生物{/*2026年2月22日13时34分24*/
    我直接给你 NeoForge 1.20+ 能用、最简单、最稳 的方案：
      把原本敌对的生物 → 改成被动、不攻击玩家。
      核心思路（只改这 3 点就够）
      1. 把 MobCategory 从  MONSTER  改成  CREATURE （被动生物分类）
      2. 去掉 目标选择器（不找玩家打）
      3. 可选：加 和平模式不消失、不会主动攻击
      完整可复制代码（直接套）
      1. 实体注册：改成被动分类
      java
      public static final RegistryObject<EntityType<YourEntity>> YOUR_ENTITY =
      ENTITIES.register("your_entity", () ->
        EntityType.Builder.of( YourEntity::new, MobCategory.CREATURE ) //这里改成 CREATURE
        .sized( 0.6F, 1.95F )
        .build( "your_entity" )
        );
    2. 实体类：去掉 AI 攻击逻辑（关键）
      java
      public class YourEntity extends Zombie{ //你继承什么敌对生物就改什么
      public YourEntity( EntityType<? extends Zombie> type, Level level ){
        super( type, level );
      }
      //重写目标选择器 → 不攻击玩家
      @Override
        protected void registerGoals( ){
        //先清空父类（原版敌对AI）
        this.goalSelector.getAvailableGoals( ).clear( );
        this.targetSelector.getAvailableGoals( ).clear( );
        //只加被动生物的基础AI
        this.goalSelector.addGoal( 0, new FloatGoal( this ) );
        this.goalSelector.addGoal( 1, new PanicGoal( this, 1.2D ) );
        this.goalSelector.addGoal( 2, new RandomStrollGoal( this, 1.0D ) );
        this.goalSelector.addGoal( 3, new LookAtPlayerGoal( this, Player.class, 6.0F ) );
        this.goalSelector.addGoal( 4, new RandomLookAroundGoal( this ) );
      }
      //可选：强制永远不会攻击
      @Override
        public boolean isAggressive( ){
        return false;
      }
    }
    3. 如果你想「修改原版生物」而不是新生物（超常用）
      用 NeoForge 事件 直接把原版僵尸/骷髅变成被动：
      java
      @SubscribeEvent
      public void onAddGoal( GoalEvent.AddGoalEvent event ){
      if( event.getEntity( ) instanceof Zombie zombie ){
        //移除所有攻击玩家的 AI
        if( event.getGoal( ) instanceof MeleeAttackGoal
          || event.getGoal( ) instanceof NearestAttackableTargetGoal<?> ){
          event.setCanceled( true );
        }
      }
    }
    4. 进阶：彻底和平（不会受伤、不会跑）
      java
      //不会主动逃跑
      @Override
      protected boolean shouldPanic( ){
      return false;
    }
    //不会被标记为敌人
    @Override
      public boolean isPreventingPlayerRest( ){
      return false;
    }
    你告诉我 2 个信息，我能直接给你「完全适配你版本」的代码
      1. 你用的 NeoForge 版本（1.20.1？1.21？）
      2. 你要改：
      - A 自己写的新生物
      - B 原版生物（僵尸/骷髅/苦力怕等）
      我可以直接给你 Copy-Paste 就能用 的成品类。
  };
  struct 取消事件{/*2026年2月22日14时28分43*/
    我给你一份NeoForge 里通用、最清晰的「取消事件」写法，不管是实体、AI、攻击、生成、刷怪都能用。
      一、最核心：怎么取消事件
      只要事件实现了 ICancellable 接口，就能取消：
      java
      event.setCanceled( true );
    或者（有些旧写法）：
      java
      event.setCancel( false ); //取消
    二、你现在最需要的：取消「生物攻击/敌对行为」
      1. 取消生物攻击玩家事件
      java
      @SubscribeEvent
      public void onLivingAttack( LivingAttackEvent event ){
      if( event.getEntity( ) instanceof Player player
        && event.getSource( ).getEntity( ) instanceof LivingEntity attacker ){
        //让这个攻击不生效
        event.setCanceled( true );
      }
    }
    2. 取消生物锁定玩家目标（彻底不打你）
      java
      @SubscribeEvent
      public void onSetTarget( LivingSetAttackTargetEvent event ){
      LivingEntity entity = event.getEntity( );
      LivingEntity target = event.getTarget( );
      if( target instanceof Player ){
        //强制不让它锁定玩家
        entity.setTarget( null );
      }
    }
    3. 取消敌对 AI Goal（最干净）
      java
      @SubscribeEvent
      public void onAddGoal( GoalEvent.AddGoalEvent event ){
      if( event.getEntity( ) instanceof Monster ){
        //取消攻击目标、近战攻击
        if( event.getGoal( ) instanceof NearestAttackableTargetGoal<?>
          || event.getGoal( ) instanceof MeleeAttackGoal ){
          event.setCanceled( true );
        }
      }
    }
    三、常用取消事件大全（直接抄）
      取消自然生成
      java
      @SubscribeEvent
      public void onNaturalSpawn( LivingSpawnEvent.SpecialSpawn event ){
      event.setCanceled( true );
    }
    取消生物消失
      java
      @SubscribeEvent
      public void onDespawn( LivingSpawnEvent.AllowDespawn event ){
      event.setCanceled( true );
    }
    取消玩家受到伤害
      java
      @SubscribeEvent
      public void onPlayerHurt( LivingHurtEvent event ){
      if( event.getEntity( ) instanceof Player ){
        event.setCanceled( true );
      }
    }
    四、判断能不能取消的方法
      按住 Ctrl 点进事件类，看有没有：
      java
      implements ICancellable
      有 → 可以  setCanceled( true )
      没有 → 不能取消，只能改值
      你告诉我你要取消什么具体行为，我直接给你能直接塞进你模组里跑的完整代码。
      auto LivingDamageEvent = [&]( )->void{/*2026年2月22日14时30分39*/
      我给你 NeoForge 1.20+ 最标准、最稳 的  LivingDamageEvent  使用 + 取消伤害写法，直接复制就能用。
        LivingDamageEvent 是什么
        - 实体即将受到伤害时触发
        - 可以：取消伤害、修改伤害、判断来源
        - 属于 ICancellable → 可以  setCanceled( true )
        1. 完整：取消伤害
        java
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
        @Mod.EventBusSubscriber( modid = "你的模组ID" )
        public class ModEvents{
        @SubscribeEvent
          public void onLivingDamage( LivingDamageEvent event ){
          LivingEntity entity = event.getEntity( );
          DamageSource source = event.getSource( );
          //例子1：玩家受到任何伤害 → 取消
          if( entity instanceof Player ){
            event.setCanceled( true ); //伤害不生效
          }
          //例子2：只取消 怪物攻击玩家
          if( entity instanceof Player
            && source.getEntity( ) instanceof LivingEntity attacker
            && attacker instanceof Monster ){
            event.setCanceled( true );
          }
        }
      }
      2. 修改伤害值（不取消，只改血量）
        java
        @SubscribeEvent
        public void onLivingDamage( LivingDamageEvent event ){
        LivingEntity entity = event.getEntity( );
        //让玩家受到的伤害减半
        if( entity instanceof Player ){
          event.setAmount( event.getAmount( ) / 2 );
        }
      }
      3. 关键区别（一定要懂）
        - LivingAttackEvent：攻击刚发起时
        - LivingDamageEvent：伤害即将生效时（护甲、抗性都算完了）
        👉 想真正取消伤害，用 LivingDamageEvent 最稳。
        4. 你现在的需求？
        我可以直接给你精准代码：
        1. 取消所有敌对生物对玩家的伤害？
        2. 还是某种生物（僵尸/骷髅/苦力怕）不打玩家？
        3. 还是你的自定义生物变成友好、不伤人？
        告诉我，我给你即插即用版。
      }/*void*/;
  };
  struct 让10hp显示为一颗心{/*2026年2月22日14时45分55*/
    我给你 NeoForge 1.20+ 直接能用、最干净 的方案：
      让 10 HP = 1 颗心（也就是 1 点伤害 = 0.1 颗心）。
      核心原理
      - 原版：1 颗心 = 2 HP
      - 你要：1 颗心 = 10 HP
      - 只需要在渲染时缩放血量显示就行，不改实际血量。
      直接复制可用代码
      java
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
      @Mod.EventBusSubscriber( modid = "你的模组ID", bus = Mod.EventBusSubscriber.Bus.MOD )
      public class HealthRenderEvents{
      //替换血量渲染
      @SubscribeEvent
        public void onRenderHealth( RenderGuiOverlayEvent.Pre event ){
        if( !event.getOverlay( ).id( ).equals( net.minecraft.client.gui.Gui.OVERLAY_HEALTH ) ){
          return;
        }
        event.setCanceled( true ); //取消原版血量渲染
        renderCustomHealth( event.getGuiGraphics( ), event.getPartialTick( ) );
      }
      //自定义：10HP = 1心
      private void renderCustomHealth( GuiGraphics guiGraphics, float partialTick ){
        minecraft.client.gui.Gui gui = minecraft.getInstance( ).gui;
        Player player = minecraft.getInstance( ).player;
        float health = player.getHealth( );
        //10 HP = 1 心
        int customHearts = (int)(health / 10);
        //原版是 2HP=1心，所以要乘回去让原版渲染逻辑认
        float renderHealth = customHearts * 2.0F;
        //用原版方法渲染，但血量是我们算好的
        gui.renderHealth( guiGraphics, player, 0, 0, 20, 9, renderHealth, player.getMaxHealth( ) );
      }
    }
    如果你想 真正内部血量 = 10HP=1心
      （伤害、血量计算都按 10HP=1心）
      java
      @SubscribeEvent
      public void onLivingDamage( LivingDamageEvent event ){
      //让所有伤害 ÷5，因为 10HP=1心，原版 2HP=1心
      event.setAmount( event.getAmount( ) / 5.0F );
    }
    你要哪种？
      1. 只改界面显示（实际血量不变）
      2. 真正改游戏数值（10点才掉1心）
      我可以直接给你对应版本的完整类文件。
  };
  struct useFancyGraphics( ){/*2026年2月22日15时00分51*/
    在 NeoForge（Java 版）里，useFancyGraphics 就是判断玩家是否开启了「精美图形（Fancy）」的开关，对应视频设置里的 Graphics: Fancy / Fast。
      一、获取当前状态（1.20+）
      java
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
      //在客户端代码中调用
      Options options = Minecraft.getInstance( ).options;
    boolean useFancy = options.graphicsMode( ).get( ).isFancy( );
    二、常见用途（直接用）
      1. 渲染时判断是否开精美
      java
      //比如树叶、云朵、粒子、自定义渲染
      if( options.graphicsMode( ).get( ).isFancy( ) ){
        //精美模式：透明树叶、动态云、复杂粒子
      } else{
        //快速模式：不透明、简单渲染
      }
    2. 强制/修改（不推荐，会覆盖玩家设置）
      java
      //强制设为 Fancy
      options.graphicsMode( ).set( net.minecraft.client.GraphicsMode.FANCY );
    //强制设为 Fast
    options.graphicsMode( ).set( net.minecraft.client.GraphicsMode.FAST );
    三、与旧版 Forge 区别
      - 旧版： Minecraft.getInstance( ).options.useFancyGraphics 
      - 新版（1.19+ / NeoForge）：改用  graphicsMode( ).get( ).isFancy( )
      四、常见场景示例
      - 自定义树叶渲染：精美模式显示透明
      - 自定义云朵：精美模式显示体积云
      - 性能优化：快速模式简化粒子
      需要我给你一个完整的客户端事件示例，在渲染时根据  useFancy  切换效果吗？
  };
  struct 预测跳跃落点{/*2026年2月25日19时11分03前*/
    在 NeoForge（Minecraft Java 模组开发）中预测玩家/实体跳跃落点，核心是模拟抛物线运动 + 步进式碰撞检测，用游戏物理参数逐帧推演直到落地。
      一、Minecraft 跳跃物理（关键参数）
      - 重力： 0.08 （每 tick 向下速度 +0.08）
      - 空气阻力： 0.98 （每 tick 速度 × 0.98）
      - 跳跃初速度： 0.42 （Y 轴起跳速度）
      - 1 tick = 1/20 秒（固定步长）
      二、核心思路（伪代码）
      java
      //输入：起跳位置、速度、实体尺寸
      Vec3 pos = player.position( );
    Vec3 vel = player.getDeltaMovement( );
    AABB bb = player.getBoundingBox( );
    //模拟每 tick，直到落地
    for( int t = 0; t < 100; t++ ){ //最多模拟 5 秒
      //1. 应用重力与阻力
      vel = vel.add( 0, -0.08, 0 );
      vel = vel.scale( 0.98 );
      //2. 计算下一位置
      Vec3 nextPos = pos.add( vel );
      AABB nextBB = bb.move( nextPos );
      //3. 碰撞检测：是否碰到地面/方块
      if( level.noCollision( player, nextBB ) == false ){
        //找到落点：pos 即为着陆点
        System.out.println( "落点：" + pos );
        break;
      }
      //4. 更新位置
      pos = nextPos;
    }
    三、NeoForge 常用 API
      - 获取实体状态：
      -  entity.position( )  → 当前坐标  Vec3 
      -  entity.getDeltaMovement( )  → 当前速度  Vec3 
      -  entity.getBoundingBox( )  → 碰撞盒  AABB 
      - 碰撞检测：
      -  Level.noCollision( Entity, AABB )  → 无碰撞返回  true 
      -  Level.getBlockState( BlockPos )  → 检查方块类型
      四、完整示例（预测玩家落点）
      java
      public static BlockPos predictLanding( ServerPlayer player ){
      Level level = player.level( );
      Vec3 pos = player.position( );
      Vec3 vel = player.getDeltaMovement( );
      AABB bb = player.getBoundingBox( );
      for( int i = 0; i < 100; i++ ){
        //物理更新
        vel = vel.add( 0, -0.08, 0 );
        vel = vel.scale( 0.98 );
        Vec3 nextPos = pos.add( vel );
        AABB nextBB = bb.move( nextPos );
        //碰撞检测
        if( !level.noCollision( player, nextBB ) ){
          //落地：取脚下方块
          return new BlockPos(
            Math.floor( pos.x ),
            Math.floor( pos.y - 0.1 ),
            Math.floor( pos.z )
          );
        }
        pos = nextPos;
      }
      return null; //未落地（虚空/高空）
    }
    五、优化与注意
      - 精度：步进  1 tick  足够；需更高精度可拆分为子步（如 4 次 / tick）
      - 特殊情况：
      - 跳跃中被推/撞：需实时重新计算
      - 粘液块/蹦床：修改重力/反弹系数
      - 飞行/创造模式：跳过预测
      - 客户端/服务端：落点预测建议在服务端执行，避免作弊
      要不要我给你一个可直接运行的 NeoForge 1.21 模组示例（含注册事件、按键触发显示落点粒子）？
  };
  struct getBoundingBox{
    在 NeoForge（1.21）里， getBoundingBox  对应  BoundingBox （旧版叫  AxisAlignedBB ），用于实体、方块、碰撞检测。
      一、核心概念
      -  BoundingBox ：轴对齐包围盒（AABB），表示 3D 空间中的立方体碰撞/选中区域
      - 常用对象： Entity 、 BlockState 、 Level 、 BlockEntity  都有相关方法
      二、获取实体的 BoundingBox（最常用）
      java
      //获取实体当前的 BoundingBox（随位置/尺寸动态更新）
      BoundingBox box = entity.getBoundingBox( );
    //常用属性（世界坐标）
    double minX = box.minX;
    double minY = box.minY;
    double minZ = box.minZ;
    double maxX = box.maxX;
    double maxY = box.maxY;
    double maxZ = box.maxZ;
    //尺寸
    double width = box.getXsize( );
    double height = box.getYsize( );
    double depth = box.getZsize( );
    三、手动创建 BoundingBox
      java
      //方式1：直接构造（minX, minY, minZ, maxX, maxY, maxZ）
      BoundingBox box = new BoundingBox( 0, 0, 0, 1, 2, 1 );
    //方式2：基于中心+尺寸（常用）
    //centerX, centerY, centerZ, halfX, halfY, halfZ
    BoundingBox box = BoundingBox.ofCenter( 0.5, 1, 0.5, 0.5, 1, 0.5 );
    四、常用操作（碰撞/检测）
      java
      //1. 扩展 BoundingBox（常用于范围检测）
      BoundingBox expanded = box.expandTowards( 1, 0, 1 ); //向各方向扩展
    //2. 判断点是否在盒内
    boolean inside = box.contains( 2, 1, 2 );
    //3. 判断两个盒是否相交
    boolean intersect = box.intersects( otherBox );
    //4. 射线与盒相交（用于瞄准/点击检测）
    Vec3 start = new Vec3( 0, 2, 0 );
    Vec3 end = new Vec3( 5, 2, 5 );
    Optional<Vec3> hit = box.clip( start, end );
    hit.ifPresent( pos -> {
      //命中点处理
    } );
    //5. 获取世界中范围内的实体
    List<Entity> entities = level.getEntitiesOfClass( Entity.class, box );
    五、自定义实体 Hitbox（覆盖默认）
      java
      public class MyEntity extends LivingEntity{
      public MyEntity( EntityType<? extends LivingEntity> type, Level level ){
        super( type, level );
      }
      //重写：设置固定尺寸（宽×高）
      @Override
        protected void defineSynchedData( ){
        super.defineSynchedData( );
      }
      @Override
        public void refreshDimensions( ){
        //宽1.0，高2.0（默认是按实体类型）
        this.setBoundingBox( new BoundingBox( -0.5, 0, -0.5, 0.5, 2, 0.5 ) );
      }
    }
    六、方块的碰撞盒
      java
      //获取方块状态的碰撞形状
      BlockState state = level.getBlockState( pos );
    VoxelShape shape = state.getCollisionShape( level, pos );
    //转为 BoundingBox（单个方块）
    BoundingBox blockBox = shape.bounds( ).move( pos );
    七、调试：显示 Hitbox
      - 游戏内按 F3 + B 显示所有实体/方块的 BoundingBox（蓝色线框）
      需要我给你写一个完整的 NeoForge 1.21 示例：自定义实体 + 自定义 Hitbox + 范围检测 nearby 实体的可运行代码吗？
  };
  struct 放块{/*2026年2月26日19时01分57*/
    NeoForge 1.21 放置方块完整指南
      以下提供四种常用放置方块场景的完整可运行代码，直接复制即可使用。
      一、基础方块放置（世界中设置方块）
      最基础的通过代码在指定位置放置方块，适用于命令、事件处理等场景。
      java
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
      //你的Mod主类注解
      @Mod.EventBusSubscriber( modid = "your_mod_id", bus = Mod.EventBusSubscriber.Bus.FORGE )
      public class BlockPlacementExamples{
      /**
      * 在指定位置放置方块
      * @param level 世界对象
      * @param pos 放置位置
      * @param state 要放置的方块状态
      * @return 是否放置成功
      */
      public static boolean placeBlock( Level level, BlockPos pos, BlockState state ){
        //检查世界是否是客户端（客户端不应修改世界）
        if( level.isClientSide( ) ) return false;
        //检查位置是否有效且可放置
        if( level.isInWorldBounds( pos ) && level.isEmptyBlock( pos ) ){
          //设置方块（第三个参数2表示更新邻居和通知客户端）
          level.setBlock( pos, state, 2 );
          //触发方块放置事件
          level.levelEvent( 2001, pos, Block.getId( state ) );
          return true;
        }
        return false;
      }
      //示例：每100 ticks在玩家位置上方5格放置一个钻石块
      @SubscribeEvent
        public static void onPlayerTick( TickEvent.PlayerTickEvent event ){
        if( event.player.tickCount % 100 == 0 && !event.player.level( ).isClientSide( ) ){
          BlockPos pos = event.player.blockPosition( ).above( 5 );
          boolean success = placeBlock( event.player.level( ), pos, Blocks.DIAMOND_BLOCK.defaultBlockState( ) );
          if( success ){
            event.player.sendSystemMessage( Component.literal( "已在上方放置钻石块！" ) );
          }
        }
      }
    }
    关键API说明：
      -  level.setBlock( pos, state, flags ) ：核心放置方法
      - flag 2：更新相邻方块、通知客户端、触发方块更新事件
      - flag 16：不通知客户端（仅服务器内部修改）
      -  level.isEmptyBlock( pos ) ：检查位置是否为空（可放置方块）
      -  level.isInWorldBounds( pos ) ：检查位置是否在世界边界内
      二、自定义放置法杖（物品右键放置方块）
      实现远程放置方块的自定义物品，类似“放置魔杖”功能。
      1. 创建放置法杖物品类
      java
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
      public class PlacementWandItem extends Item{
      private static final int MAX_DISTANCE = 60; //最大放置距离
      public PlacementWandItem( Properties properties ){
        super( properties );
      }
      @Override
        public InteractionResult useOn( UseOnContext context ){
        Player player = context.getPlayer( );
        Level level = context.getLevel( );
        if( player == null || level.isClientSide( ) ) return InteractionResult.PASS;
        //获取玩家视线所指的方块
        HitResult hitResult = player.pick( MAX_DISTANCE, 0, false );
        if( hitResult.getType( ) == HitResult.Type.BLOCK ){
          BlockHitResult blockHit = (BlockHitResult)hitResult;
          BlockPos targetPos = blockHit.getBlockPos( ).relative( blockHit.getDirection( ) );
          //使用玩家副手物品作为放置方块
          ItemStack offhandStack = player.getOffhandItem( );
          if( !offhandStack.isEmpty( ) && offhandStack.getItem( ) instanceof net.minecraft.world.item.BlockItem blockItem ){
            //放置方块
            boolean success = placeBlock( level, targetPos, blockItem.getBlock( ).defaultBlockState( ) );
            if( success ){
              offhandStack.shrink( 1 ); //消耗一个物品
              return InteractionResult.SUCCESS;
            }
          }
        }
        return InteractionResult.FAIL;
      }
    }
    2. 注册物品（在你的Mod主类中）
      java
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
      public class ModItems{
      public static final DeferredRegister<Item> ITEMS = DeferredRegister.create( ForgeRegistries.ITEMS, "your_mod_id" );
      public static final RegistryObject<Item> PLACEMENT_WAND = ITEMS.register("placement_wand",
        () -> new PlacementWandItem( new Item.Properties( ).stacksTo( 1 ) ));
    }
    三、监听方块放置事件（修改/阻止放置行为）
      通过事件监听自定义方块放置逻辑，如限制特定方块放置、替换放置的方块等。
      java
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
      @Mod.EventBusSubscriber( modid = "your_mod_id", bus = Mod.EventBusSubscriber.Bus.FORGE )
      public class BlockPlacementListener{
      /**
      * 方块放置前事件（可取消放置）
      */
      @SubscribeEvent
        public static void onBlockPlaceAttempt( BlockEvent.EntityPlaceEvent event ){
        BlockState placedState = event.getPlacedBlock( );
        //示例1：阻止玩家放置TNT
        if( placedState.is( Blocks.TNT ) ){
          event.setCanceled( true );
          event.getEntity( ).sendSystemMessage( Component.literal( "禁止放置TNT！" ) );
        }
        //示例2：将所有放置的石头替换为钻石块
        if( placedState.is( Blocks.STONE ) ){
          event.setPlacedBlock( Blocks.DIAMOND_BLOCK.defaultBlockState( ) );
        }
      }
      /**
      * 方块放置后事件（用于执行后续操作）
      */
      @SubscribeEvent
        public static void onBlockPlaced( BlockEvent.EntityMultiPlaceEvent event ){
        //为每个放置的方块添加粒子效果
        for( BlockPos pos : event.getReplacedBlockPositions( ) ){
          event.getLevel( ).addParticle(
            ParticleTypes.HAPPY_VILLAGER,
            pos.getX( )+0.5, pos.getY( )+0.5, pos.getZ( )+0.5,
            0, 0.5, 0
          );
        }
      }
    }
    四、方块放置最佳实践与注意事项
      1. 区分客户端与服务器
      - 永远不要在客户端（ level.isClientSide( ) 为true）修改世界
      - 方块放置逻辑应在服务器端执行，客户端仅处理视觉效果
      2. 正确处理方块状态
      - 使用 block.defaultBlockState( ) 获取默认状态
      - 复杂方块（如熔炉、门）需设置额外属性：
      java
      //放置一个面向北方的熔炉
      Blocks.FURNACE.defaultBlockState( ).setValue( FurnaceBlock.FACING, Direction.NORTH );
    3. 权限与限制检查
      - 检查玩家是否有权限在该位置放置方块： player.mayUseItemAt( pos, direction, stack )
      - 检查方块是否可替换： level.getBlockState( pos ).canBeReplaced( )
      4. 触发正确的事件与更新
      - 使用 level.setBlock( pos, state, 3 ) 同时更新邻居和通知客户端
      - 放置后调用 level.levelEvent( 2001, pos, Block.getId( state ) ) 触发粒子效果
      五、常见问题解决
      - 放置的方块不显示：确保在服务器端执行放置逻辑，且使用正确的flag参数
      - 方块无法被破坏：检查是否设置了正确的方块属性，如 strength( -1.0F ) 会让方块无法破坏
      - 放置后没有声音/粒子：调用 level.levelEvent( ) 触发相应效果
      需要我把上述代码整合为一个可直接导入的NeoForge 1.21模组项目模板（含注册、事件订阅和gradle配置）吗？
  };
  struct instanceof{/*2026年2月27日01时15分55*/
    用户问的是关于NeoForge中的instanceof用法。我需要搜索相关信息来了解在NeoForge模组开发中如何正确使用instanceof操作符，特别是在处理实体、事件或其他对象时的最佳实践。
      现在我需要整理关于NeoForge中instanceof用法的信息。instanceof是Java的操作符，在NeoForge模组开发中主要用于类型判断，特别是在事件处理、实体交互、物品识别等场景。我应该提供具体的代码示例，包括Java 16+的模式匹配语法，以及在不同场景下的最佳实践和注意事项。;
    NeoForge中的instanceof用法指南
      在NeoForge模组开发中，instanceof是Java核心操作符，用于判断对象是否为特定类/接口的实例，常配合模式匹配( Java 16+ ) 简化类型判断与转换，广泛用于事件处理、实体交互、物品识别等场景 。
      一、基础语法与空安全特性
      - 传统语法： object instanceof ClassName 
      java
      //判断实体是否为LivingEntity
      if( entity instanceof LivingEntity ){
        LivingEntity livingEntity = (LivingEntity)entity;
        //处理逻辑
      }
    - 模式匹配语法( Java 16+ )： object instanceof ClassName variableName （自动类型转换）
      java
      //无需显式强制转换，更简洁安全
      if( entity instanceof LivingEntity livingEntity ){
        livingEntity.heal( 2.0F ); //直接使用转换后的变量
      }
    - 空安全：对 null 对象使用instanceof返回 false ，不会抛出NullPointerException
      二、NeoForge常见应用场景
      1. 事件处理中的实体类型判断
      在实体相关事件中筛选特定实体类型：
      java
      @SubscribeEvent
      public void onEntitySpawn( EntityJoinLevelEvent event ){
      Entity entity = event.getEntity( );
      //只处理自然生成的生物
      if( entity instanceof Mob mob && mob.getSpawnType( ) == EntitySpawnReason.NATURAL ){
        mob.setCustomName( Component.literal( "野生生物" ) );
        event.setCanceled( true ); //取消自然生成
      }
    }
    2. 物品交互时的类型识别
      判断玩家手持物品类型：
      java
      @Override
      public InteractionResult useOn( UseOnContext context ){
      Player player = context.getPlayer( );
      ItemStack stack = player.getMainHandItem( );
      //判断是否为自定义工具
      if( stack.getItem( ) instanceof MyCustomTool tool ){
        tool.applySpecialEffect( context.getLevel( ), context.getClickedPos( ) );
        return InteractionResult.SUCCESS;
      }
      return InteractionResult.PASS;
    }
    3. 方块实体( BlockEntity )类型检查
      验证特定方块实体类型：
      java
      BlockEntity be = level.getBlockEntity( pos );
    if( be instanceof MyCustomBlockEntity myBE ){
      myBE.incrementCounter( ); //调用自定义方法
      level.sendBlockUpdated( pos, level.getBlockState( pos ), level.getBlockState( pos ), 3 );
    }
    4. 事件类型的精确匹配
      在通用事件处理中筛选子事件类型：
      java
      @SubscribeEvent
      public void onPlayerEvent( PlayerEvent event ){
      //只处理玩家克隆事件
      if( event instanceof PlayerEvent.Clone cloneEvent ){
        Player original = cloneEvent.getOriginal( );
        Player newPlayer = cloneEvent.getEntity( );
        //复制数据逻辑
      }
    }
    三、最佳实践与注意事项
      1. 优先使用instanceof而非标签：标签可能被数据包修改，instanceof更可靠，避免ClassCastException风险
      java
      //推荐：类型安全判断
      if( stack.getItem( ) instanceof MyCustomItem ){ ... }
    //不推荐：依赖标签（可能被修改）
    if( stack.is( ModTags.Items.MY_CUSTOM_ITEMS ) ){ ... }
    2. 处理接口类型：判断对象是否实现特定接口
      java
      if( entity instanceof FlyingAbility flyingEntity ){
        flyingEntity.setFlyingSpeed( 0.1F );
      }
    3. 层级判断原则：
      - 先判断更具体的子类，再判断父类
      - 避免过度使用instanceof，考虑使用多态替代
      4. 配合数据组件：复杂类型判断可结合数据组件
      java
      if( entity instanceof LivingEntity livingEntity &&
        livingEntity.getData( MyModDataComponents.SPECIAL_EFFECT ) ){
        //处理特殊效果实体
      }
    四、完整示例：实体伤害处理
      java
      @SubscribeEvent
      public void onEntityDamage( LivingHurtEvent event ){
      LivingEntity target = event.getEntity( );
      DamageSource source = event.getSource( );
      //判断攻击者是否为玩家且手持特定武器
      if( source.getEntity( ) instanceof Player attacker &&
        attacker.getMainHandItem( ).getItem( ) instanceof MyMagicSword ){
        //增强伤害
        event.setAmount( event.getAmount( ) * 1.5F );
        //为目标添加效果
        if( target instanceof Mob mob ){
          mob.addEffect( new MobEffectInstance( MobEffects.WEAKNESS, 200 ) );
        }
      }
    }
    总结
      instanceof是NeoForge开发中类型安全处理的核心工具，结合Java 16+模式匹配可大幅简化代码。合理使用它能处理事件筛选、实体交互、物品识别等场景，同时注意空安全、类型层级与多态原则，提升代码健壮性与可维护性。
      需要我把这些示例整理成可直接复制到1.21 NeoForge项目中运行的完整代码片段（含必要import和事件注册）吗？
  };
  struct getOnPos{/*2026年2月27日01时50分41*/
    用户现在问的是NeoForge里的getOnPos，首先我要明确这个方法是Entity的方法，1.21 NeoForge里的用法。
      首先，先说明getOnPos( ) 是什么：Entity 类的方法，返回实体站在的方块位置（BlockPos），就是脚下的方块坐标。
      然后，方法签名： public BlockPos getOnPos( )
      然后，核心用途：获取实体站立的方块位置，比如玩家、生物脚下的方块。
      然后，常见场景和代码示例，分几个：
      1. 基础获取（玩家/实体脚下位置）
      2. 事件里用（比如LivingHurtEvent、PlayerEvent）
      3. 判断脚下方块类型
      4. 和getBlockStateOn( ) 区分（这个是直接获取脚下方块状态，更方便）
      还要注意：
      - 实体在空中/水里时，getOnPos( ) 还是会返回下方最近的地面？不对，要准确：实体悬空时，返回的是其碰撞箱底部所在的位置，可能是空的。
      - 1.21 NeoForge 正常可用，属于原版方法，NeoForge没改。
      然后给代码示例，简洁，符合MC 1.21 NeoForge。
      还要区分 getOnPos( ) 和 blockPosition( )、getBlockPos( )：blockPosition是实体所在的方块位置（本体位置），getOnPos是脚下。
      现在组织语言，简洁，直击重点，因为用户是做模组开发，要直接能用的代码。;
    在 NeoForge 1.21 里， getOnPos( )  是  Entity  类的方法，作用只有一个：
      👉 获取实体脚下踩着的方块坐标（ BlockPos ）。
      一、方法信息
      java
      //来自 net.minecraft.world.entity.Entity
      public BlockPos getOnPos( )
      作用
      返回实体站立/碰撞箱底部所在的方块位置，也就是你看到的“脚下方块”。
      二、最常用写法（直接复制）
      1. 获取玩家脚下位置
      java
      Player player = ...;
    BlockPos pos = player.getOnPos( );
    2. 获取脚下方块状态
      java
      BlockState state = player.level( ).getBlockState( player.getOnPos( ) );
    3. 判断脚下是什么方块
      java
      if( state.is( Blocks.DIAMOND_BLOCK ) ){
        //玩家踩在钻石块上
      }
    三、常见场景示例（事件里）
      java
      @SubscribeEvent
      public void onPlayerTick( PlayerTickEvent.Post event ){
      Player player = event.getEntity( );
      BlockPos feetPos = player.getOnPos( );
      BlockState feetState = player.level( ).getBlockState( feetPos );
      if( feetState.is( Blocks.MAGMA_BLOCK ) ){
        player.setSecondsOnFire( 1 );
      }
    }
    四、容易搞混的对比
      -  entity.getOnPos( )  → 脚下方块
      -  entity.blockPosition( )  /  entity.getBlockPos( )  → 实体本身所在的方块
      你是要在玩家事件、实体 AI、还是物品右键里用  getOnPos( ) ？我可以直接给你对应场景的完整可用代码。
  };
  struct 朝向{/*2026年2月27日02时16分51*/
    用户现在问的是NeoForge里的朝向，首先要分清楚实体朝向（玩家/生物看的方向）和方块朝向（方块的6个方向），这两个是MC模组里最常用的，1.21 NeoForge的写法要准确。
      首先，先讲实体的朝向（Look方向）：获取玩家/实体看向的方向，用getViewVector( 1.0F )，还有获取旋转角yaw、pitch。
      然后是方块的朝向（Direction）：6个方向，DOWN, UP, NORTH, SOUTH, WEST, EAST，获取方块的朝向，设置方块朝向。
      还要给常用代码，直接复制能用的，比如获取玩家看向的方块、获取玩家朝向、方块朝向判断。
      首先分两大部分：
      1. 实体朝向（玩家/生物 视角方向）
      - 获取视角向量
      - 获取Yaw/Pitch
      - 获取玩家正对的方块
      2. 方块朝向（Direction 枚举）
      - 6个方向
      - 获取方块的朝向
      - 设置方块朝向
      - 方向判断
      还要注意1.21的API，比如level.getBlockState( pos ).getValue( BlockStateProperties.FACING )，这个是关键。
      然后给极简示例，适合模组开发直接用，不要太复杂，贴合之前的对话风格，都是直接复制的代码。
      还要区分水平朝向（只看前后左右，不看上下），这个常用。
      现在整理结构：
      NeoForge 1.21 「朝向」全用法
      分两类：实体视角朝向（玩家看哪）、方块朝向（方块朝哪）
      一、实体（玩家/生物）视角朝向
      1. 获取「看向的方向向量」
      java
      //获取实体视线向量 (1.0F是默认距离)
      Vec3 lookVec = player.getViewVector( 1.0F );
    2. 获取旋转角度（Yaw/Pitch）
      java
      float yaw = player.getYRot( );   //水平左右朝向 (偏航角)
    float pitch = player.getXRot( ); //上下俯仰角
    3. 获取玩家正对的方块
      java
      //玩家视线方向，距离5格内的方块
      HitResult hit = player.pick( 5.0D, 0.0F, false );
    if( hit.getType( ) == HitResult.Type.BLOCK ){
      BlockPos targetPos = ((BlockHitResult)hit).getBlockPos( );
      Direction face = ((BlockHitResult)hit).getDirection( ); //点击的面
    }
    4. 获取玩家水平朝向（只前后左右：北/南/东/西）
      java
      Direction horizontalDir = player.getDirection( );
    //结果是 NORTH / SOUTH / EAST / WEST
    二、方块朝向（Direction 枚举）
      MC方块用  net.minecraft.core.Direction ，共6个：
      DOWN, UP, NORTH, SOUTH, WEST, EAST 
      1. 获取方块的朝向
      java
      BlockState state = level.getBlockState( pos );
    //大部分可旋转方块用 FACING
    Direction blockFacing = state.getValue( BlockStateProperties.FACING );
    2. 设置方块朝向
      java
      //!把方块设置为朝北
      level.setBlock( pos, state.setValue( BlockStateProperties.FACING, Direction.NORTH ), 3 );
    3. 方向判断示例
      java
      if( blockFacing == Direction.NORTH ){
        //方块朝北
      }
    三、常用小技巧
      -  direction.getOpposite( )  → 获取反方向
      -  direction.getClockWise( )  → 顺时针转
      -  player.getOnPos( ).relative( direction )  → 朝向旁边的方块
      这样简洁，都是1.21 NeoForge能用的，直接复制，符合用户模组开发的需求。;
    NeoForge 1.21 「朝向」完整用法
      MC 里朝向分两类，模组开发最常用：
      1. 实体朝向：玩家/生物看哪里
      2. 方块朝向：方块朝哪个方向
      一、实体（玩家/生物）朝向
      1. 快速获取玩家水平朝向（前后左右）
      java
      //返回: NORTH / SOUTH / EAST / WEST
      Direction dir = player.getDirection( );
    2. 获取玩家视线方向向量
      java
      Vec3 lookVec = player.getViewVector( 1.0F );
    3. 获取旋转角度
      java
      float yaw = player.getYRot( );   //左右偏航角
    float pitch = player.getXRot( ); //上下俯仰角
    4. 获取玩家正盯着的方块
      java
      BlockHitResult hit = player.pick( 5.0D, 0.0F, false );
    BlockPos targetPos = hit.getBlockPos( );   //看向的方块位置
    Direction face = hit.getDirection( );      //点到的面
    二、方块朝向（Direction 枚举）
      6个方向：
      DOWN, UP, NORTH, SOUTH, WEST, EAST 
      1. 获取方块朝向
      java
      BlockState state = level.getBlockState( pos );
    Direction facing = state.getValue( BlockStateProperties.FACING );
    2. 设置方块朝向
      java
      level.setBlock(
        pos,
        state.setValue( BlockStateProperties.FACING, Direction.NORTH ),
        3
      );
    3. 方向常用方法
      java
      Direction opposite = dir.getOpposite( ); //反方向
    Direction clockWise = dir.getClockWise( ); //顺时针
    BlockPos nextPos = pos.relative( dir ); //朝向的下一个坐标
    你是要做：玩家看向触发、方块旋转、还是实体AI朝向？我直接给你对应场景的成品代码。
  };
  void config {/*2026年2月27日03时48分27*/
    用户询问的是NeoForge的配置（config）相关内容。我需要搜索NeoForge 1.21版本的配置系统信息，包括如何创建、加载和使用配置文件，以及最佳实践。
    我需要精读NeoForge官方文档关于配置系统的页面，获取最准确和详细的信息，包括配置类型、创建方法、注册流程、使用示例以及最佳实践。
    现在我有了足够的信息来提供一个全面的NeoForge配置系统指南。我将涵盖配置类型、创建配置类、注册配置、使用配置值、配置事件和配置屏幕等关键部分，并提供完整的可运行代码示例。;
  NeoForge 1.21 配置系统指南
    快速总览：NeoForge使用ModConfigSpec与TOML文件管理配置，支持STARTUP/CLIENT/COMMON/SERVER四种类型，通过builder模式定义值，在Mod构造器注册，用ConfigValue#get( )读取，可通过事件监听加载/重载，还能提供配置界面供玩家在游戏内修改 。
    一、核心概念
    - 配置类型( Type )
    - STARTUP: 客户端/服务器均加载，注册即读，不网络同步，默认后缀 -startup ，适合启动时确定的设置（如内容注册开关）
    - CLIENT: 仅客户端加载，不网络同步，默认后缀 -client ，适合视觉/音效等客户端专属设置
    - COMMON: 客户端/服务器均加载，不网络同步，默认后缀 -common ，适合通用逻辑设置
    - SERVER: 客户端/服务器均加载，网络同步，可按世界覆盖（ saves/<世界名>/serverconfig ），默认后缀 -server ，适合游戏规则/平衡性设置
    - 配置构建器( ModConfigSpec.Builder )
    - 用于定义配置值、分组（ push / pop ）、添加注释/翻译等元数据
    - 支持多种值类型：基础类型、范围值、白名单值、列表、枚举等
    二、完整实现步骤（含代码）
    1. 创建配置类（推荐模式）
    java
    // src/main/java/com/example/examplemod/config/ExampleConfig.java
    package com.example.examplemod.config;
import net.neoforged.fml.config.ModConfigSpec;
import java.util.List;
import java.util.Arrays;
  public class ExampleConfig{
    // 静态实例与规范，供外部访问
    public static final ExampleConfig INSTANCE;
    public static final ModConfigSpec SPEC;
    // 配置值字段（按类型分组）
    // 通用设置
    public final ModConfigSpec.ConfigValue<String> welcomeMessage;
    public final ModConfigSpec.IntValue maxPlayers;
    public final ModConfigSpec.BooleanValue enableFeatureX;
    // 进阶设置（分组示例）
    public final ModConfigSpec.DoubleValue damageMultiplier;
    public final ModConfigSpec.ConfigValue<List<String>> allowedItems;
    // 枚举示例
    public enum Difficulty{ EASY, NORMAL, HARD }
    public final ModConfigSpec.EnumValue<Difficulty> difficulty;
    // 构造器，接收builder构建配置
    private ExampleConfig( ModConfigSpec.Builder builder ){
      // 通用设置（无分组）
      welcomeMessage = builder
        .comment( "服务器启动时显示的欢迎消息" )
        .translation( "examplemod.config.welcome_message" )
        .define( "welcome_message", "欢迎来到我的服务器！" );
      maxPlayers = builder
        .comment( "最大玩家数量（1-100）" )
        .translation( "examplemod.config.max_players" )
        .defineInRange( "max_players", 20, 1, 100 );
      enableFeatureX = builder
        .comment( "启用特性X" )
        .translation( "examplemod.config.enable_feature_x" )
        .define( "enable_feature_x", true );
      // 进阶设置（分组）
      builder.push( "advanced" ); // 进入advanced分组
      damageMultiplier = builder
        .comment( "伤害倍率（0.1-2.0）" )
        .translation( "examplemod.config.advanced.damage_multiplier" )
        .defineInRange( "damage_multiplier", 1.0D, 0.1D, 2.0D );
      allowedItems = builder
        .comment( "允许使用的物品ID列表" )
        .translation( "examplemod.config.advanced.allowed_items" )
        .defineList( "allowed_items",
          Arrays.asList( "minecraft:diamond", "minecraft:iron_sword" ),
          o->o instanceof String ); // 验证元素为字符串
      difficulty = builder
        .comment( "游戏难度" )
        .translation( "examplemod.config.advanced.difficulty" )
        .defineEnum( "difficulty", Difficulty.NORMAL, Arrays.asList( Difficulty.values( ) ) );
      builder.pop( ); // 退出advanced分组
    }
    // 静态块构建实例与规范
    static {
      ModConfigSpec.Builder builder = new ModConfigSpec.Builder( );
      INSTANCE = new ExampleConfig( builder );
      SPEC = builder.build( );
    }
  }
  2. 在Mod主类注册配置
    java
    // src/main/java/com/example/examplemod/ExampleMod.java
    package com.example.examplemod;
import com.example.examplemod.config.ExampleConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
  @Mod( ExampleMod.MOD_ID )
    public class ExampleMod{
    public static final String MOD_ID = "examplemod";
    public ExampleMod( net.neoforged.fml.ModContainer container ){
      // 注册配置（可注册多个不同类型）
      container.registerConfig( ModConfig.Type.COMMON, ExampleConfig.SPEC );
      // 如需添加SERVER配置，可再注册一个：
      // container.registerConfig(ModConfig.Type.SERVER, ExampleServerConfig.SPEC, "examplemod-server.toml");
      // 注册配置事件监听器
      NeoForge.EVENT_BUS.addListener( this::onConfigLoad );
      NeoForge.EVENT_BUS.addListener( this::onConfigReload );
    }
    // 配置加载事件
    private void onConfigLoad( net.neoforged.fml.config.ModConfigEvent.Loading event ){
      if( event.getConfig( ).getSpec( ) == ExampleConfig.SPEC ){
        ExampleMod.LOGGER.info( "ExampleConfig loaded successfully!" );
        // 加载后执行初始化逻辑
      }
    }
    // 配置重载事件
    private void onConfigReload( net.neoforged.fml.config.ModConfigEvent.Reloading event ){
      if( event.getConfig( ).getSpec( ) == ExampleConfig.SPEC ){
        ExampleMod.LOGGER.info( "ExampleConfig reloaded! Applying changes..." );
        // 重载后更新运行时状态
      }
    }
  }
  3. 客户端注册配置界面
    java
    // src/main/java/com/example/examplemod/client/ExampleModClient.java
    package com.example.examplemod.client;
import com.example.examplemod.ExampleMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
  @Mod( value = ExampleMod.MOD_ID, dist = Dist.CLIENT )
    public class ExampleModClient{
    public ExampleModClient( net.neoforged.fml.ModContainer container ){
      // 注册默认配置界面
      container.registerExtensionPoint( IConfigScreenFactory.class, ConfigurationScreen::new );
    }
  }
  4. 使用配置值（代码中读取）
    java
    // 在任意需要的地方读取配置
    public void someGameLogic( ){
    // 读取字符串
    String message = ExampleConfig.INSTANCE.welcomeMessage.get( );
    // 读取整数
    int maxPlayers = ExampleConfig.INSTANCE.maxPlayers.get( );
    // 读取布尔值
    if( ExampleConfig.INSTANCE.enableFeatureX.get( ) ){
      // 执行特性X逻辑
    }
    // 读取分组中的值
    double damageMultiplier = ExampleConfig.INSTANCE.damageMultiplier.get( );
    // 读取列表
    List<String> allowedItems = ExampleConfig.INSTANCE.allowedItems.get( );
    // 读取枚举
    ExampleConfig.Difficulty difficulty = ExampleConfig.INSTANCE.difficulty.get( );
  }
  三、配置文件位置与格式
    - 默认路径：
    - 客户端： .minecraft/config/<modid>-<type>.toml 
    - 服务器： <server>/config/<modid>-<type>.toml 
    - SERVER类型世界覆盖： .minecraft/saves/<世界名>/serverconfig/<modid>-server.toml 
    - 生成的TOML示例（ examplemod-common.toml ）：
    toml
    # 服务器启动时显示的欢迎消息
    welcome_message = "欢迎来到我的服务器！"
    # 最大玩家数量（1-100）
    max_players = 20
    # 启用特性X
    enable_feature_x = true
    [advanced]
    # 伤害倍率（0.1-2.0）
    damage_multiplier = 1.0
    # 允许使用的物品ID列表
    allowed_items = ["minecraft:diamond", "minecraft:iron_sword"]
    # 游戏难度
    difficulty = "NORMAL"
    四、高级用法
    1. 自定义配置文件名
    java
    // 注册时指定文件名（第三个参数）
    container.registerConfig( ModConfig.Type.COMMON, ExampleConfig.SPEC, "my_custom_config.toml" );
  2. 配置验证与限制
    - 范围值： defineInRange （数字类型）
    - 白名单值： defineInList （值必须在指定集合内）
    - 列表验证： defineList  带元素验证器与列表大小验证
    - 枚举值： defineEnum （限制为指定枚举常量）
    3. 配置翻译
    - 使用 translation( "modid.config.key" ) 为配置项添加翻译键
    - 在 src/main/resources/assets/modid/lang/en_us.json 中添加对应翻译文本
    json
  {
    "examplemod.config.welcome_message": "Welcome Message",
    "examplemod.config.welcome_message.tooltip": "Message displayed when server starts"
  }
    4. 配置重载
    - 游戏内修改配置后点击“保存并重载”按钮触发 ModConfigEvent.Reloading 事件
    - 监听该事件以更新运行时状态（如UI、游戏规则等）
    五、最佳实践
    1. 配置类型选择
    - 客户端专属设置→CLIENT
    - 服务器专属且需同步→SERVER
    - 通用且无需同步→COMMON
    - 启动时确定且不随游戏运行更改→STARTUP
    2. 配置分组：使用 push( ) / pop( ) 将相关配置组织到同一节，提升可读性
    3. 默认值合理：设置符合多数玩家预期的默认值，同时通过注释说明用途与范围
    4. 事件监听：配置加载/重载时更新缓存与运行时状态，避免使用旧值
    5. 翻译支持：为所有配置项添加翻译，提升多语言用户体验
    需要我把上述示例整理成可直接复制到项目中的完整文件模板（含包路径、导入语句和必要注释）吗？
  };
};
struct 记录_旧の打字{ };