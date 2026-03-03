package com.pnkxpl.gd_ts_uk.utils;

import com.pnkxpl.gd_ts_uk.core.Config;
import com.pnkxpl.gd_ts_uk.core.WanderingTraderExpressDelivery;

/**
 * 完整的日志工具类 - 封装所有日志输出，支持所有级别的配置开关
 * 优化：提供统一的日志输出接口，支持所有级别控制
 */
public class LoggerUtil {


    /**
     * 输出调试日志
     */
    public static void debug(String message, Object... args) {
        if (Config.enableDebugLogs()) {
            WanderingTraderExpressDelivery.LOGGER.debug(message, args);
        }
    }

    /**
     * 输出信息日志
     */
    public static void info(String message, Object... args) {
        if (Config.enableInfoLogs()) {
            WanderingTraderExpressDelivery.LOGGER.info(message, args);
        }
    }

    /**
     * 输出警告日志
     */
    public static void warn(String message, Object... args) {
        if (Config.enableWarnLogs()) {
            WanderingTraderExpressDelivery.LOGGER.warn(message, args);
        }
    }

    /**
     * 输出错误日志（受开关控制）
     */
    public static void error(String message, Object... args) {
        if (Config.enableErrorLogs()) {
            WanderingTraderExpressDelivery.LOGGER.error(message, args);
        }
    }

    /**
     * 输出错误日志（带异常，受开关控制）
     */
    public static void error(String message, Throwable throwable, Object... args) {
        if (Config.enableErrorLogs()) {
            WanderingTraderExpressDelivery.LOGGER.error(message, throwable, args);
        }
    }

    /**
     * 输出调试日志（带条件检查）
     */
    public static void debugIf(boolean condition, String message, Object... args) {
        if (condition && Config.enableDebugLogs()) {
            WanderingTraderExpressDelivery.LOGGER.debug(message, args);
        }
    }

    /**
     * 输出信息日志（带条件检查）
     */
    public static void infoIf(boolean condition, String message, Object... args) {
        if (condition && Config.enableInfoLogs()) {
            WanderingTraderExpressDelivery.LOGGER.info(message, args);
        }
    }

    /**
     * 输出警告日志（带条件检查）
     */
    public static void warnIf(boolean condition, String message, Object... args) {
        if (condition && Config.enableWarnLogs()) {
            WanderingTraderExpressDelivery.LOGGER.warn(message, args);
        }
    }

    /**
     * 输出错误日志（带条件检查）
     */
    public static void errorIf(boolean condition, String message, Object... args) {
        if (condition && Config.enableErrorLogs()) {
            WanderingTraderExpressDelivery.LOGGER.error(message, args);
        }
    }

    /**
     * 输出重要信息日志（不受开关控制，用于关键事件）
     */
    public static void importantInfo(String message, Object... args) {
        WanderingTraderExpressDelivery.LOGGER.info("🔔 " + message, args);
    }

    /**
     * 输出关键错误日志（不受开关控制，用于必须关注的错误）
     */
    public static void criticalError(String message, Object... args) {
        WanderingTraderExpressDelivery.LOGGER.error("🚨 " + message, args);
    }

    /**
     * 输出关键错误日志（带异常，不受开关控制）
     */
    public static void criticalError(String message, Throwable throwable, Object... args) {
        WanderingTraderExpressDelivery.LOGGER.error("🚨 " + message, throwable, args);
    }

    /**
     * 检查调试日志是否启用
     */
    public static boolean isDebugEnabled() {
        return Config.enableDebugLogs();
    }

    /**
     * 检查信息日志是否启用
     */
    public static boolean isInfoEnabled() {
        return Config.enableInfoLogs();
    }

    /**
     * 检查警告日志是否启用
     */
    public static boolean isWarnEnabled() {
        return Config.enableWarnLogs();
    }

    /**
     * 检查错误日志是否启用
     */
    public static boolean isErrorEnabled() {
        return Config.enableErrorLogs();
    }
}