package org.dreamwork.integrated.common.device.manager.api.services;

import org.dreamwork.integrated.common.device.manager.api.model.DownlinkStatus;
import org.dreamwork.integrated.common.device.manager.api.model.NetworkProtocol;
import org.dreamwork.integrated.common.device.manager.api.model.database.*;

import java.util.List;
import java.util.Map;

@SuppressWarnings ("unused")
public interface IDeviceManageService {
    String DISTRIBUTED_LIST_KEY = "distributed.messages.raw";
    int DEVICE_REGISTERED       = 0x70000000;
    int DEVICE_BATCH_REGISTERED = 0x70000001;
    int DEVICE_REMOVED          = 0x70000002;
    int DEVICE_BATCH_REMOVED    = 0X70000003;

    Device getByImei (String imei);

    /**
     * 将指定的原始日志数组 {@code logs} 添加到批处理器中.
     *
     * @param logs 原始数据日志
     */
    void save (RawDataLog... logs);

    /**
     * 将指定的下发日志数组 {@code logs} 添加到批处理器中。
     * <p>值得一提的是：对于某些即时回复的指令，可能需要更新 {@link DownlinkLog} 的某些属性，
     * 为了保证数据一致性，需要在 {@link IDeviceManageService#lock(Runnable) IDeviceManageService.lock(Runnable)} 中执行这些更新动作。
     *
     * @param logs 下发命令日志
     */
    void save (DownlinkLog... logs);

    /**
     * 当设备注册成功后，会发出 category = {@code ${module_name}}，what = {@link #DEVICE_REGISTERED DEVICE_REGISTERED} 的广播，
     * 参数位 {@code device} 本身
     * @param device 被添加的设备
     */
    void save (Device device);
    void saveDevices (Device... devices);

    void save (DeviceCommand command);

    /**
     * 当参数 {@code devices} 只有<strong>一个</strong>元素时，且被成功删除，将会发出
     * category = {@code ${module_name}}，what = {@link #DEVICE_REMOVED DEVICE_REMOVED} 的广播，
     * 参数为 {@code device} 本身
     * @param devices 即将被删除的设备 imei 列表
     */
    void deleteDevice (String... devices);

    /**
     * 根据指定的设备 imei 查找有效的 (可下发的) 命令
     * @param deviceId 指定的设备 imei
     * @return 改imei下所有可下发的命令
     */
    List<DeviceCommand> getIssuableCommands (String deviceId);

    /**
     * 获取指定模块id的设备
     * @param moduleId 指定的模块id
     * @return 设备列表
     */
    List<Device> getDevicesByModuleId (String moduleId);

    /**
     * 更新命令下发的状态
     * @param commands 待更新的命令
     */
    void setCommandDispatched (DeviceCommand... commands);

    boolean isDevicePresent (String imei);

    boolean isDevicePresent (String imei, String module);
    boolean isDevicePresent (String imei, String vendor, String category, String model);

    /**
     * 维护一个下发序列的当前值
     * <p>每个设备的下发都单独维护成一个下发序列。每个序列都将有一个自增的下发id
     * @param imei  设备号
     * @param value 当前的下发序列最新值
     */
    void updateDownlinkSerial (String moduleName, String imei, int value);

    long getNextDownlinkSerial (String moduleName, String imei, long maxValue);

    /**
     * 获取指定设备的下发序列最新值
     *
     * @param moduleName 模块名称
     * @param imei       设备imei
     * @return 当前值
     */
    int getCurrentValue (String moduleName, String imei);
    List<DownlinkLog> getUnRepliedLogs (String moduleName, String imei);

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
     * 以文本的方式缓存待下行的指令
     * @param moduleName 模块名称
     * @param imei       设备号
     * @param command    序列化后的指令
     * @param lifetime   缓存的存活时间，单位为秒
     */
    void cacheCommand (String moduleName, String imei, String command, long lifetime);

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
    Map<Long, String> getCachedCommand (String module, String imei);

    /**
     * 在日志处理线程内安全执行 runner.run ()，避免内置的日志处理程序和 runner.run () 内部逻辑造成数据竞争
     * @param runner 任务
     */
    void lock (Runnable runner);

    /**
     * 注册设备类型配置。
     * <p>关联模块/通信协议和三元组</p>
     * @param vendor     三元组之厂商
     * @param category   三元组之设备类型
     * @param model      三元组之设备型号
     * @param moduleName 集成框架的模块名称
     * @param protocol   设备连接的网络协议
     */
    void registerDeviceType (String vendor, String category, String model, String moduleName, NetworkProtocol protocol);
//    void registerDeviceType (DeviceTypeConfig config);

    DeviceTypeConfig getDeviceTypeConfig (String vendor, String category, String model);
}