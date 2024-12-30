package org.dreamwork.integrated.common.device.manager.api.services;

import org.dreamwork.integrated.common.device.manager.api.model.DownlinkStatus;
import org.dreamwork.integrated.common.device.manager.api.model.database.DownlinkLog;
import org.dreamwork.integrated.common.device.manager.api.model.downlink.CommandCache;
import org.dreamwork.integrated.common.device.manager.api.model.downlink.DownlinkCommand;
import org.dreamwork.integrated.common.device.manager.api.model.downlink.DownlinkResult;
import org.dreamwork.util.IDisposable;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SuppressWarnings ("unused")
public interface IDownlinkService extends IDisposable {
    /**
     * 异步执行命令下发
     * @param imei        设备号
     * @param command     待下行的指令
     * @param maxSerialNo 序列号产生器允许的最大值，如果超出这个值，序列号将从 0 重新开始
     * @param cachedLogId 如果这条指令曾经被缓存，这个id指的是下行日志的数据库主键
     * @return 返回一个异步结果
     */
    Future<DownlinkResult> downlink (String imei, DownlinkCommand command, Long maxSerialNo, String cachedLogId);

    /**
     * 执行命令下发，并且等待响应结果，这个函数将阻塞当前线程
     * @param imei        设备号
     * @param command     待下行的指令
     * @param maxSerialNo 序列号产生器允许的最大值，如果超出这个值，序列号将从 0 重新开始
     * @param cachedLogId 如果这条指令曾经被缓存，这个id指的是下行日志的数据库主键
     * @param time        结果等待时长
     * @param unit        等待时长的时间单位
     * @return 响应结果
     */
    DownlinkResult downlinkAndWait (String imei, DownlinkCommand command, Long maxSerialNo,
                                    long time, TimeUnit unit, String cachedLogId);

    /**
     * 以文本的方式缓存待下行的指令
     * @param moduleName 模块名称
     * @param imei       设备号
     * @param command    序列化后的指令
     * @param lifetime   缓存的存活时间，单位为秒
     */
    void cacheCommand (String moduleName, String imei, String command, long lifetime);

    /**
     * 根据指定的模块名称，设备号以及下行序列号来获取最多一条下行日志
     * @param moduleName 模块名称
     * @param imei       设备号
     * @param serialNo   下行序列号
     * @return 匹配的下行日志
     */
    DownlinkLog getDownlinkLog (String moduleName, String imei, int serialNo);

    /**
     * 应答一条指定的下行日志
     * @param moduleName 模块名称
     * @param imei       设备号
     * @param serialNo   下行的序列号
     * @param status     应答状态
     * @param content    应答内容
     */
    void replyDownlinkLog (String moduleName, String imei, int serialNo, DownlinkStatus status, String content);

    /**
     * 删除一条已经缓存的指令
     * @param moduleName 模块名称
     * @param imei       设备号
     * @param timestamp  这条指令被缓存的时间戳
     */
    void deleteCachedCommand (String moduleName, String imei, long timestamp);

    /**
     * 获取指定设备的所有缓存指令
     * @param module 模块名称
     * @param imei   设备号
     * @return 该设备所有已缓存的指令。字典的索引是指令的缓存时间戳，值是命令的序列化文本
     */
    Map<Long, CommandCache> getCachedCommand (String module, String imei);

    long getNextDownlinkSerial (String moduleName, String imei, long maxValue);

    /**
     * 应答曾经下发的指令
     * @param imei       设备号
     * @param serialNo   指令序列号
     * @param status     执行状态
     * @param raw        原始报文的文本序列化形式。对于 二进制报文，通常是 hex string
     */
    void reply (String imei, long serialNo, DownlinkStatus status, String raw);

    static DownlinkLog createDownlinkLog (String imei, String name) {
        DownlinkLog log = new DownlinkLog ();
        log.setImei (imei);
        log.setModule (name);
        log.setDownlinkTime (new Timestamp (System.currentTimeMillis ()));
        return log;
    }
}