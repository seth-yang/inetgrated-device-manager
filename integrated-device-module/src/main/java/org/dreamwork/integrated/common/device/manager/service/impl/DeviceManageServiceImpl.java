package org.dreamwork.integrated.common.device.manager.service.impl;

import org.dreamwork.integrated.common.device.manager.api.model.DownlinkStatus;
import org.dreamwork.integrated.common.device.manager.api.model.NetworkProtocol;
import org.dreamwork.integrated.common.device.manager.api.model.database.*;
import org.dreamwork.integrated.common.device.manager.api.services.IDeviceManageService;
import org.dreamwork.integrated.common.device.manager.util.EntityHelper;
import org.dreamwork.integration.api.IModuleContext;
import org.dreamwork.integration.api.IntegrationException;
import org.dreamwork.integration.api.annotation.AConfigured;
import org.dreamwork.integration.api.services.IDatabaseService;
import org.dreamwork.integration.api.services.IRedisManager;
import org.dreamwork.integration.api.services.IRedisService;
import org.dreamwork.concurrent.BatchProcessor;
import org.dreamwork.db.ITransaction;
import org.dreamwork.db.PostgreSQL;
import org.dreamwork.persistence.DatabaseSchema;
import org.dreamwork.util.IDisposable;
import org.dreamwork.util.ITypedMap;
import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.dreamwork.integrated.common.device.manager.util.EntityHelper.buildTripleKey;
import static org.dreamwork.integrated.common.device.manager.util.EntityHelper.isNotEmpty;

@Resource
public class DeviceManageServiceImpl implements IDeviceManageService, IDisposable {

    private BatchProcessor<Object> processor;
    private PostgreSQL postgres;

    private final Lock locker = new ReentrantLock ();

    private final Logger logger = LoggerFactory.getLogger (DeviceManageServiceImpl.class);

    private final Map<String, DeviceTypeConfig> deviceTypes = new HashMap<> ();

    private IRedisService redis;

    @Resource
    private IRedisManager manager;

    @Resource
    private IModuleContext context;

    @AConfigured ("${org.dreamwork.integrated.device.manager.redis.ref}")
    private String REDIS_REF;

    @AConfigured ("${org.dreamwork.integrated.logger.batch.capacity}")
    private int capacity = 64;

    @AConfigured ("${org.dreamwork.integrated.logger.batch.timeout}")
    private int timeout = 60000;

    @PostConstruct
    public void init () throws IntegrationException {
        DeviceManagerSchema.registerAllSchemas ();
        {
            IDatabaseService service = context.findService (IDatabaseService.class);
            postgres = new PostgreSQL (service.getDataSource ("jdbc/integrated_projects"));
            checkAndCreateSchemas ();
        }

        // 加载所有的设备配置
        loadDeviceConfigType ();

        redis = manager.getByName (REDIS_REF);

        if (redis != null) {
            loadRegisteredDevices ();
        }
        processor = new BatchProcessor<Object> ("raw-log-saver", capacity, capacity, timeout) {
            @Override
            protected void process (List<Object> data) {
                if (!data.isEmpty ()) {
                    List<MqttLog> mqtt   = new ArrayList<> ();
                    List<RawDataLog> raw = new ArrayList<> ();
                    List<DownlinkLog> downlinkForSave   = new ArrayList<> (),
                                      downLinkForUpdate = new ArrayList<> ();
                    lock (() -> {
                        for (Object o : data) {
                            if (o instanceof MqttLog) {
                                mqtt.add ((MqttLog) o);
                            } else if (o instanceof RawDataLog) {
                                raw.add ((RawDataLog) o);
                            } else if (o instanceof DownlinkLog) {
                                DownlinkLog log = (DownlinkLog) o;
                                if (StringUtil.isEmpty (log.getId ())) {
                                    log.setId (StringUtil.uuid ());
                                    downlinkForSave.add (log);
                                } else {
                                    downLinkForUpdate.add (log);
                                }
                            }
                        }

                        if (!mqtt.isEmpty ()) {
                            postgres.save (mqtt, false);
                        }
                        if (!raw.isEmpty ()) {
                            postgres.save (raw, false);
                        }
                        if (!downlinkForSave.isEmpty ()) {
                            postgres.save (downlinkForSave, false);
                        }
                        if (!downLinkForUpdate.isEmpty ()) {
                            postgres.update (downLinkForUpdate.toArray (new Object[0]));
                        }
                    });
                }
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor ();
        processor.start (executor);
        executor.shutdown ();
    }

    @Override
    @PreDestroy
    public void dispose () {
        if (logger.isTraceEnabled ()) {
            logger.trace ("disposing device manager");
        }
        deviceTypes.clear ();

        if (processor != null) {
            processor.dispose (true);
        }
    }

    @Override
    public Device getByImei (String deviceId) {
        Device device = readDeviceFromCache (deviceId);
        if (device == null) {
            device = postgres.getByPK (Device.class, deviceId);
            if (device != null) {
                cacheDevice (device);
            }
        }
        return device;
    }

    @Override
    public void save (RawDataLog... logs) {
        if (processor != null) {
            for (RawDataLog log : logs) {
                processor.add (log);
            }
        }
    }

    @Override
    public void save (DownlinkLog... logs) {
        if (processor != null) {
            for (DownlinkLog log : logs) {
                processor.add (log);
            }
        }
    }

    public void save (MqttLog... logs) {
        if (processor != null) {
            for (MqttLog log : logs) {
                processor.add (log);
            }
        }
    }

    @Override
    public void save (Device device) {
        if (device.getRegisterTime () == null) {
            device.setRegisterTime (new Timestamp (System.currentTimeMillis ()));
        }
        postgres.save (device, false);
        if (redis != null) {
            cacheDevice (device);
        }
    }

    public void saveDevices (Device... devices) {
        Arrays.stream(devices).forEach (device -> {
            if (device.getRegisterTime () == null) {
                device.setRegisterTime (new Timestamp (System.currentTimeMillis ()));
            }
        });
        postgres.save (Arrays.asList (devices), false);
        Arrays.stream (devices).forEach (this::cacheDevice);
    }

    @Override
    public void save (DeviceCommand command) {
        String sql = "select nextval('device_command_id_seq')";
        int serial = postgres.executeScale (sql);
        command.setId (serial);
        postgres.save (command, false);
    }

    @Override
    public void deleteDevice (String... devices) {
        if (devices.length == 0) {
            return;
        }

        List<Object> args = new ArrayList<> (devices.length);
        StringBuilder builder = new StringBuilder ();
        for (String imei : devices) {
            if (builder.length () > 0) {
                builder.append (',');
            }
            builder.append ('?');
            args.add (imei);
        }
        String sql = "SELECT * FROM device WHERE imei IN (" + builder + ')';
        List<Device> list = postgres.list (Device.class, sql, args.toArray (new Object[0]));
        if (!list.isEmpty ()) {
            ITransaction tx = null;
            try {
                tx = postgres.beginTransaction ();
                for (Device device : list) {
                    String imei = device.getImei (), moduleName = device.getModule ();
                    // 删除设备本身
                    sql = "DELETE FROM device WHERE imei = ? AND module_name = ?";
                    tx.executeUpdate (sql, imei, moduleName);

                    // 删除设备待下发命令
                    sql = "DELETE FROM device_command WHERE imei = ?";
                    tx.executeUpdate (sql, imei);

                    // 删除下行命令序列
                    sql = "DELETE FROM downlink_serial WHERE imei = ? AND module_name = ?";
                    tx.executeUpdate (sql, imei, moduleName);

                    // 删除下行日志
                    sql = "DELETE FROM downlink_log WHERE imei = ? AND module_name = ?";
                    tx.executeUpdate (sql, imei, moduleName);
                }
                if (redis != null) {
                    String[] keys = list.stream ()
                            .map (this::buildDeviceKey)
                            .toArray (String[]::new);
                    redis.delete (keys);
                }
                tx.commit ();
            } catch (Exception ex) {
                rollback (tx);
            } finally {
                close (tx);
            }
        }
    }

    @Override
    public List<DeviceCommand> getIssuableCommands (String deviceId) {
        String sql = "select * from device_command where imei = ? and performed = false order by receive_time";
        return postgres.list (DeviceCommand.class, sql, deviceId);
    }

    @Override
    public List<Device> getDevicesByModuleId (String moduleName) {
        String sql = "SELECT * FROM device WHERE module_name = ?";
        return postgres.list (Device.class, sql, moduleName);
    }

    @Override
    public void setCommandDispatched (DeviceCommand... commands) {
        postgres.update ((Object[]) commands);
    }

    @Override
    public boolean isDevicePresent (String imei) {
        String key = buildDeviceKey (imei);
        if (redis.present (key)) {
            return true;
        }

        String sql = "SELECT count(*) FROM device WHERE imei = ?";
        return postgres.executeScale (sql, imei) > 0;
    }

    public boolean isDevicePresent (String imei, String module) {
        String key = buildDeviceKey (imei);
        if (redis.present (key)) {
            return true;
        }

        String sql = "SELECT count(*)  FROM device WHERE imei = ? AND module_name = ?";
        return postgres.executeScale (sql, imei, module) > 0;
    }

    @Override
    public boolean isDevicePresent (String imei, String vendor, String category, String model) {
        String key = buildDeviceKey (imei);
        if (redis.present (key)) {
            return true;
        }

        String sql =
                "SELECT count(*)  FROM device WHERE imei = ? AND vendor = ? " +
                "   AND device_category = ? AND device_model = ?";
        return postgres.executeScale (sql, imei, vendor, category, model) > 0;
    }

    @Override
    public void updateDownlinkSerial (String moduleName, String imei, int value) {
        String sql = "UPDATE downlink_serial SET current_value = ? WHERE imei = ? AND module_name = ?";
        postgres.executeUpdate (sql, value, imei, moduleName);
    }

    private static final String DOWNLINK_SERIALS = "downlink.serials";
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
    public int getCurrentValue (String moduleName, String imei) {
        String sql = "SELECT * FROM downlink_serial WHERE imei = ? AND module_name = ?";
        DownlinkSerial serial = postgres.getSingle (DownlinkSerial.class, sql, imei, moduleName);
        if (serial == null) {
            // 不知道什么原因配置表没有，尝试从日志表恢复
            sql = "SELECT max(serial_no) as serial_no FROM downlink_log WHERE module_name = ? AND imei = ?";
            List<ITypedMap> list = postgres.list (sql, moduleName, imei);
            if (list == null || list.isEmpty ()) {
                // 还是没有，创建一个到配置表里
                serial = new DownlinkSerial (moduleName, imei);
            } else {
                ITypedMap map = list.get (0);
                Number n = map.value ("serial_no");
                serial = new DownlinkSerial (moduleName, imei, n.intValue ());
            }
            postgres.save (serial);
        }
        return serial.getValue ();
    }

    @Override
    public List<DownlinkLog> getUnRepliedLogs (String moduleName, String imei) {
        String sql =
                "  SELECT * FROM downlink_log " +
                "   WHERE imei = ? AND module_name = ? " +
                "     AND (downlink_status IS NULL OR downlink_status = 'Executing') " +
                "ORDER BY downlink_time ASC";
        return postgres.list (DownlinkLog.class, sql, imei, moduleName);
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
    public void cacheCommand (String moduleName, String imei, String command, long lifetime) {
        String key = "downlink.cache." + moduleName + "." + imei + "." + System.currentTimeMillis ();
        redis.set (key, command);
        if (lifetime > 0) {
            redis.setExpiredIn (key, (int) lifetime);
        }
    }

    @Override
    public void deleteCachedCommand (String moduleName, String imei, long timestamp) {
        String key = "downlink.cache." + moduleName + "." + imei + "." + timestamp;
        redis.delete (key);
    }

    @Override
    public Map<Long, String> getCachedCommand (String module, String imei) {
        String pattern = "downlink.cache." + module + "." + imei + ".*";
        Map<String, String> map = redis.query (pattern);
        if (isNotEmpty (map)) {
            Map<Long, String> tree = new TreeMap<> ();
            map.forEach ((k, v) -> {
                int index = k.lastIndexOf (".");
                String part = k.substring (index + 1);
                long timestamp = Long.parseLong (part);
                tree.put (timestamp, v);
            });
            return tree;
        }
        return Collections.emptyMap ();
    }

    @Override
    public void lock (Runnable runner) {
        try {
            locker.lock ();
            runner.run ();
        } catch (Throwable t) {
            logger.warn (t.getMessage (), t);
        } finally {
            locker.unlock ();
        }
    }

    @Override
    public synchronized void registerDeviceType (String vendor, String category, String model,
                                                 String moduleName, NetworkProtocol protocol) {
        String key = buildTripleKey (vendor, category, model);
        if (!deviceTypes.containsKey (key)) {
            // 查询数据库
            String sql = "SELECT * FROM device_type_config WHERE vendor = ? AND device_category = ? AND device_model = ?";
            DeviceTypeConfig config = postgres.getSingle (DeviceTypeConfig.class, sql, vendor, category, model);
            if (config == null) {
                // 还没有，插入到数据库中
                config = new DeviceTypeConfig (moduleName, protocol, vendor, category, model);
                config.setId (StringUtil.uuid ());
                postgres.save (config, false);
            }
            deviceTypes.put (key, config);
        } else {
            // 有了，校验是否一致，否则抛出错误
            DeviceTypeConfig loaded = deviceTypes.get (key);
            if (!loaded.getModule ().equals (moduleName) ||
                loaded.getProtocol () != protocol) {
                throw new IllegalStateException ("there's another device type config with different module or protocol");
            }
        }
    }

    @Override
    public DeviceTypeConfig getDeviceTypeConfig (String vendor, String category, String model) {
        return deviceTypes.get (buildTripleKey (vendor, category, model));
    }

    private void loadDeviceConfigType () {
        List<DeviceTypeConfig> types = postgres.get (DeviceTypeConfig.class);
        if (types != null && !types.isEmpty ()) types.forEach (type -> {
            String key = buildTripleKey (type);
            deviceTypes.put (key, type);
        });
    }

    private void loadRegisteredDevices () throws IntegrationException {
        // 从数据中加载所有已经注册的设备
        String sql = "SELECT * FROM device";
        try (Connection conn = postgres.getConnection ()) {
            PreparedStatement pstmt = conn.prepareStatement (sql);
            ResultSet rs = pstmt.executeQuery ();

            Class<? extends PostgreSQL> type = postgres.getClass ();
            Method[] methods = type.getSuperclass ().getDeclaredMethods ();
            Method build = null;
            for (Method method : methods) {
                if ("build".equals (method.getName ())) {
                    if (method.getParameterCount () == 2) {
                        build = method;
                        break;
                    }
                }
            }
            if (build != null) {
                if (!build.isAccessible ()) {
                    build.setAccessible (true);
                }

                while (rs.next ()) {
                    Device device = (Device) build.invoke (postgres, rs, Device.class);
                    cacheDevice (device);
                }
            }
        } catch (Exception ex) {
            logger.warn (ex.getMessage (), ex);
            throw new IntegrationException (ex);
        }
    }

    private void rollback (ITransaction tx) {
        if (tx != null) try {
            tx.rollback ();
        } catch (SQLException ignore) {}
    }

    private void close (ITransaction tx) {
        if (tx != null) try {
            tx.close ();
        } catch (Exception ignore) {}
    }

    private void checkAndCreateSchemas () {
        if (postgres == null) {
            logger.warn ("no database connection, give it up.");
            return;
        }

        if (logger.isTraceEnabled ()) {
            logger.trace ("creating database schemas for module integrated-device-manager.");
        }
        DatabaseSchema.MAP.forEach ((type, schema) -> {
            if (logger.isTraceEnabled ()) {
                logger.trace ("checking for schema {} ...", type);
            }
            if (!postgres.isTablePresent (schema.getTableName ())) {
                postgres.execute (schema.getCreateDDL ());
                if (logger.isTraceEnabled ()) {
                    logger.trace ("schema [{}] created.", type);
                }
            }
        });
    }

    private String buildDeviceKey (Device device) {
        return "integrated.devices." + device.getImei ();
    }

    private String buildDeviceKey (String imei) {
        return "integrated.devices." + imei;
    }

    private void cacheDevice (Device device) {
        String key = buildDeviceKey (device);
        Map<String, String> map = EntityHelper.device2map (device);
        if (isNotEmpty (map)) {
            redis.writeMap (key, map);
        }
    }

    private Device readDeviceFromCache (String imei) {
        String key = buildDeviceKey (imei);
        if (redis != null) {
            Map<String, String> map = redis.readAsMap (key);
            return EntityHelper.map2device (map);
        }

        return null;
    }
}