package org.dreamwork.integrated.common.device.manager.api.model.downlink;

public class CommandCache {
    /** 报文主体的 hex string，或其他序列化形式 */
    public String command;
    /** 命令报文被缓存的时间戳 */
    public long cachedTimestamp;
    /** 下行日志的主键，可能没有 */
    public String logId;
}
