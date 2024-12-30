package org.dreamwork.integrated.common.device.manager.service.impl;

import org.dreamwork.integrated.common.device.manager.api.model.DownlinkStatus;
import org.dreamwork.integrated.common.device.manager.api.model.database.Device;
import org.dreamwork.integrated.common.device.manager.api.model.database.DownlinkLog;
import org.dreamwork.integrated.common.device.manager.api.model.downlink.CommandCache;
import org.dreamwork.integrated.common.device.manager.model.CachedCommand;
import org.dreamwork.integrated.common.device.manager.api.model.downlink.DownlinkCommand;
import org.dreamwork.integrated.common.device.manager.api.model.downlink.DownlinkResult;
import org.dreamwork.integrated.common.device.manager.api.services.IDeviceManageService;
import org.dreamwork.integrated.common.device.manager.api.services.IDownlinkService;
import org.dreamwork.integrated.common.device.manager.api.services.ISessionManager;
import org.dreamwork.integration.api.annotation.AConfigured;
import org.dreamwork.integration.api.services.IDatabaseService;
import org.dreamwork.integration.api.services.IRedisManager;
import org.dreamwork.integration.api.services.IRedisService;
import org.apache.mina.core.session.IoSession;
import org.dreamwork.db.PostgreSQL;
import org.dreamwork.util.CollectionCreator;
import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.dreamwork.integrated.common.device.manager.api.model.DownlinkStatus.*;
import static org.dreamwork.integrated.common.device.manager.util.Const.Error.*;
import static org.dreamwork.integrated.common.device.manager.util.EntityHelper.isNotEmpty;
import static org.dreamwork.integrated.common.device.manager.util.EntityHelper.map2commandCache;

@Resource
public class DownlinkServiceImpl implements IDownlinkService {
    private final Logger logger = LoggerFactory.getLogger (DownlinkServiceImpl.class);

    /** 指令的默认缓存时间：3 天 */
    private final long lifetime = 3600 * 24 * 3;
    @Resource
    private IDeviceManageService manager;
    @Resource
    private ISessionManager sessions;
    private final ExecutorService executor = Executors.newFixedThreadPool (129);

    private final Lock locker = new ReentrantLock ();
    private final Map<String, CachedCommand> pool = new ConcurrentHashMap<> ();

    private static final String DOWNLINK_SERIALS = "downlink.serials";
    private static final long   TIMEOUT = 30000L;   // 30s.

    private IRedisService redis;

    @Resource
    private IRedisManager redisManager;
    @Resource
    private IDatabaseService databaseManager;

    private PostgreSQL postgres;

    @AConfigured ("${com.hothink.integrated.device.manager.redis.ref}")
    private String REDIS_REF;

    @PostConstruct
    public void init () {
        redis = redisManager.getByName (REDIS_REF);
        postgres = new PostgreSQL (databaseManager.getDataSource ("jdbc/integrated_projects"));
    }

    @Override
    public Future<DownlinkResult> downlink (String imei, DownlinkCommand command, Long maxSerialNo, String cachedLogId) {
        return executor.submit (() -> {
            if (logger.isTraceEnabled ()) {
                logger.trace ("trying to downlink command to device {}", imei);
            }
            Device device = manager.getByImei (imei);
            if (device == null) {
                return new DownlinkResult (ERR_DEVICE_NOT_FOUND, 0L, "device not found");
            }

            IoSession session = sessions.get (imei);
            String moduleName = device.getModule ();
            if (session == null) {
                if (command.isCacheable ()) {
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("no active session, but this command can be cached.");
                    }
                    // 当前不可下发，但允许缓存. 将它缓存起来
                    DownlinkLog downlinkLog = getDownlinkLog (moduleName, maxSerialNo, imei, Cached, command);
                    cacheCommand (moduleName, imei, downlinkLog.getCommand (), lifetime);
                    manager.save (downlinkLog);
                    return new DownlinkResult (0, command.serialNo, "Created");
                } else {
                    if (logger.isTraceEnabled ()) {
                        logger.warn ("there's no active session within imei {}", imei);
                    }
                    // 设备不活动，且命令不可缓存
                    return new DownlinkResult (ERR_DEVICE_IS_NOT_ACTIVE, 0L, "device is not active.");
                }
            } else {
                DownlinkLog downlinkLog;
                if (StringUtil.isEmpty (cachedLogId)) {
                    // 未缓存过，需要计算序列号，以及新建日志
                    downlinkLog = getDownlinkLog (moduleName, maxSerialNo, imei, Executing, command);
                } else {
                    // 从数据库里恢复日志
                    downlinkLog = getDownlinkLog (moduleName, imei, command.serialNo.intValue ());
                }

                String key = imei + '.' + command.serialNo;
                CachedCommand cache = new CachedCommand (imei, command.serialNo, command, downlinkLog);
                cache.condition = locker.newCondition ();
                pool.put (key, cache);
                // 下行
                session.write (command);
                if (logger.isTraceEnabled ()) {
                    logger.trace (">>>>>>>>>> command[{}] for {} flushed.", command.serialNo, imei);
                }
                locker.lock ();
                try {
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("acquire a new lock ...");
                    }
                    cache.condition = locker.newCondition ();
                    if (!cache.condition.await (TIMEOUT, TimeUnit.MILLISECONDS)) {
                        downlinkLog.setStatus (Timeout);
                        // 已经超时
                        return new DownlinkResult (ERR_COMMAND_TIMEOUT, command.serialNo, "timeout");
                    }
                    downlinkLog.setStatus (Success);
                    return new DownlinkResult (ERR_OK, command.serialNo, "Success");
                } finally {
                    locker.unlock ();
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("the locke released.");
                    }
                    pool.remove (key);
                    deleteCachedCommand (moduleName, imei, cache.timestamp);
                    manager.save (downlinkLog);
                }
            }
        });
    }

    @Override
    public DownlinkResult downlinkAndWait (String imei, DownlinkCommand command, Long maxSerialNo,
                                           long time, TimeUnit unit, String cachedLogId) {
        Future<DownlinkResult> future = downlink (imei, command, maxSerialNo, cachedLogId);
        try {
            return future.get (time, unit);
        } catch (ExecutionException ex) {
            logger.warn (ex.getMessage (), ex);
            return new DownlinkResult (ERR_INTERNAL_ERROR, 0L, ex.getMessage ());
        } catch (TimeoutException | InterruptedException ex) {
            if (logger.isTraceEnabled ()) {
                logger.warn (ex.getMessage (), ex);
            }
            logger.warn ("downlink timeout");
            return new DownlinkResult (ERR_COMMAND_TIMEOUT, 0L, "timeout");
        }
    }

    @Override
    public void cacheCommand (String moduleName, String imei, String command, long lifetime) {
        long timestamp = System.currentTimeMillis ();
        String key = buildCachedCommandKey (moduleName, imei, timestamp);
        Map<String, String> body = CollectionCreator.asMap (
                "command", command, "timestamp", String.valueOf (timestamp)
        );
        redis.writeMap (key, body);
        if (lifetime > 0) {
            redis.setExpiredIn (key, (int) lifetime);
        }
    }

    @Override
    public void deleteCachedCommand (String moduleName, String imei, long timestamp) {
        String key = buildCachedCommandKey (moduleName, imei, timestamp);
        redis.delete (key);
        if (logger.isTraceEnabled ()) {
            logger.trace ("the cached command [{}] removed.", key);
        }
    }

    @Override
    public Map<Long, CommandCache> getCachedCommand (String module, String imei) {
        String pattern = "downlink.cache." + module + "." + imei + ".*";
        Map<String, Map<String, String>> map1 = redis.queryAsMaps (pattern);
        if (isNotEmpty (map1)) {
            Map<Long, CommandCache> tree = new TreeMap<> ();
            map1.forEach ((k, v) -> {
                CommandCache cache = map2commandCache (v);
                if (cache != null) {
                    tree.put (cache.cachedTimestamp, cache);
                }
            });
            return tree;
        }

        return Collections.emptyMap ();
    }

    @Override
    public long getNextDownlinkSerial (String moduleName, String imei, long maxValue) {
        String member = moduleName + '.' + imei;
        long number = (long) redis.increase (DOWNLINK_SERIALS, member);
        if (number > maxValue) {
            redis.resetCounter (DOWNLINK_SERIALS, member);
            return 0;
        }
        return number;
    }

    @Override
    public DownlinkLog getDownlinkLog (String moduleName, String imei, int serialNo) {
        String sql =
                "  SELECT * FROM downlink_log " +
                        "   WHERE imei = ? AND module_name = ? and serial_no = ? " +
                        "ORDER BY downlink_time DESC " +
                        "   LIMIT 1";
        List<DownlinkLog> logs = postgres.list (DownlinkLog.class, sql, imei, moduleName, serialNo);
        return logs != null && !logs.isEmpty () ? logs.get (0) : null;
    }

    @Override
    public void replyDownlinkLog (String moduleName, String imei, int serialNo, DownlinkStatus status, String content) {
        String sql =
                "UPDATE downlink_log SET " +
                        "    downlink_status = ?," +
                        "    reply_time = current_timestamp," +
                        "    reply = ? " +
                        " WHERE id = (SELECT id FROM downlink_log " +
                        "              WHERE imei = ? AND module_name = ? AND serial_no = ? " +
                        "           ORDER BY downlink_time DESC LIMIT 1)";
        postgres.executeUpdate (sql, status.name (), content, imei, moduleName, serialNo);
    }

    @Override
    public void reply (String imei, long serialNo, DownlinkStatus status, String raw) {
        String key = imei + '.' + serialNo;
        CachedCommand cache = pool.get (key);
        if (logger.isTraceEnabled ()) {
            logger.trace ("<<<<<<<<<<: command[{}] for {} replied with status: {}", serialNo, imei, status);
        }
        if (cache != null) {
            if (cache.condition != null) {
                locker.lock ();
                try {
                    cache.condition.signalAll ();
                } finally {
                    locker.unlock ();
                }
                if (logger.isTraceEnabled ()) {
                    logger.trace ("the locker released.");
                }
            }

            cache.log.setReplyTime (new Timestamp (System.currentTimeMillis ()));
            cache.log.setStatus (status);
            cache.log.setReply (raw);
            if (!StringUtil.isEmpty (cache.log.getId ())) {
                // 日志曾经被保存过，需要更新
                manager.save (cache.log);
            }
        }
    }

    @PreDestroy
    @Override
    public void dispose () {
        if (logger.isTraceEnabled ()) {
            logger.trace ("disposing downlink service");
        }
        executor.shutdownNow ();
    }

    private DownlinkLog getDownlinkLog (String moduleName, Long maxSerialNo, String imei,
                                        DownlinkStatus status, DownlinkCommand command) {
        if (command.serialNo == null) {
            long max = maxSerialNo == null ? Long.MAX_VALUE : maxSerialNo;
            command.serialNo = getNextDownlinkSerial (moduleName, imei, max);
        }
        DownlinkLog downlinkLog = IDownlinkService.createDownlinkLog (imei, moduleName);
        byte[] data = command.toByteArray ();
        downlinkLog.setCommand (StringUtil.byte2hex (data, false));
        downlinkLog.setSerialNo (command.serialNo);
        downlinkLog.setStatus (status);

        return downlinkLog;
    }

    private String buildCachedCommandKey (String moduleName, String imei, long timestamp) {
        return "downlink.cache." + moduleName + "." + imei + "." + timestamp;
    }
}