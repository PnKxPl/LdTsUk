package com.pnkxpl.gd_ts_uk.utils;

import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.UUID;

/**
 * 通用工具类 - 提供跨管理器的公共方法
 * 修复：提取重复代码，提高代码复用性
 * 优化：减少代码重复，统一工具方法实现
 * 新增：集中管理所有重复的工具方法，提高代码可维护性
 * 修正：数据目录生成逻辑 - 改为使用存档的data文件夹
 */
public class CommonUtils {

    /**
     * 获取玩家实例
     * 修复：统一玩家获取逻辑，避免重复实现
     * @param playerId 玩家UUID
     * @return 玩家实例，如果玩家不存在或服务器未就绪则返回null
     */
    //!MinecraftServer=ServerStartingEvent.getServer();
    //入不(MinecraftServer serv1)
    public static Player getPlayer( UUID playerId) {
        if (WanderingTraderExpressDelivery.instance != null &&
                WanderingTraderExpressDelivery.instance.server != null) {
            return WanderingTraderExpressDelivery.instance.server.getPlayerList().getPlayer(playerId);
        }
        return null;
    }

    /**
     * 获取玩家所在的世界
     * 修复：统一世界获取逻辑，避免重复实现
     * @param playerId 玩家UUID
     * @return 玩家所在的服务器世界，如果玩家不存在则返回null
     */
    public static ServerLevel getPlayerLevel(UUID playerId) {
        Player player = getPlayer(playerId);
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer.serverLevel();
        }
        return null;
    }

    /**
     * 获取主世界
     * 修复：统一主世界获取逻辑，避免重复实现
     * @return 服务器主世界实例，如果服务器未就绪则返回null
     */
    public static ServerLevel getOverworld() {
        if (WanderingTraderExpressDelivery.instance != null) {
            return WanderingTraderExpressDelivery.instance.server.overworld();
        }
        return null;
    }

    /**
     * 获取当前世界时间
     * 修复：统一时间获取逻辑，避免重复实现
     * @return 当前世界时间（游戏刻），如果无法获取则返回0
     */
    public static long getCurrentWorldTime() {
        ServerLevel overworld = getOverworld();
        if (overworld != null) {
            return overworld.getGameTime();
        }
        return 0;
    }

    /**
     * 获取存档数据目录
     * 修正：改为使用存档的data文件夹，不再使用世界UUID区分
     * 新增：确保数据与特定存档关联
     * @param overworld 主世界实例
     * @return 模组数据目录路径
     */
    public static Path getModDataDir(ServerLevel overworld) {
        //使用存档的data文件夹
        Path dataDir = overworld.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve("wandering_trader_express_delivery");

        LoggerUtil.info("使用存档数据目录: {}", dataDir);
        return dataDir;
    }
}