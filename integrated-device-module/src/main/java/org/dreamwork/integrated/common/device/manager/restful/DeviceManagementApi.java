package org.dreamwork.integrated.common.device.manager.restful;

import com.google.gson.Gson;
import org.dreamwork.integrated.common.device.manager.api.model.database.Device;
import org.dreamwork.integrated.common.device.manager.api.model.database.DeviceCommand;
import org.dreamwork.integrated.common.device.manager.api.model.database.DeviceTypeConfig;
import org.dreamwork.integrated.common.device.manager.api.services.IDeviceManageService;
import org.dreamwork.integrated.common.device.manager.model.BatchRegistrationPayload;
import org.dreamwork.integrated.common.device.manager.model.BatchRegistrationResult;
import org.dreamwork.integrated.common.device.manager.model.Reply;
import org.dreamwork.integrated.common.device.manager.service.impl.BackgroundExecutor;
import org.dreamwork.integration.api.IModuleContext;
import org.dreamwork.integration.httpd.annotation.AContextAttribute;
import org.dreamwork.integration.httpd.annotation.ARequestBody;
import org.dreamwork.integration.httpd.annotation.ARestfulAPI;
import org.dreamwork.integration.httpd.annotation.AWebParam;
import org.dreamwork.integration.httpd.support.ResponseEntity;
import org.dreamwork.integration.httpd.support.RestfulException;
import org.dreamwork.concurrent.broadcast.ILocalBroadcastService;
import org.dreamwork.tools.wrappers.okhttp.HttpClient;
import org.dreamwork.util.CollectionCreator;
import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dreamwork.integrated.common.device.manager.api.services.IDeviceManageService.DEVICE_REGISTERED;
import static org.dreamwork.integrated.common.device.manager.api.services.IDeviceManageService.DEVICE_REMOVED;
import static org.dreamwork.integrated.common.device.manager.util.Const.Error.*;
import static org.dreamwork.integration.api.IModule.CONTEXT_ATTRIBUTE_KEY;
import static org.dreamwork.integration.httpd.support.ParameterLocation.Path;
import static javax.servlet.http.HttpServletResponse.*;

@Resource
@ARestfulAPI ("/device")
@SuppressWarnings ("unused")
public class DeviceManagementApi {
    @Resource
    private IDeviceManageService service;
    @Resource
    private ILocalBroadcastService broadcaster;

    private final Logger logger = LoggerFactory.getLogger (DeviceManagementApi.class);

    @ARestfulAPI (urlPattern = "/batch-register", method = "post")
    public ResponseEntity<?> batchRegister (@ARequestBody BatchRegistrationPayload payload,
                                            @AContextAttribute(CONTEXT_ATTRIBUTE_KEY) IModuleContext context) {
        if (payload == null) {
            return new ResponseEntity<> (SC_NOT_ACCEPTABLE, "无效的数据");
        }
        if (payload.devices == null || payload.devices.length == 0) {
            return new ResponseEntity<> (SC_NOT_ACCEPTABLE, "设备信息缺失");
        }

        BackgroundExecutor executor = context.findService (BackgroundExecutor.class);
        String taskId = StringUtil.uuid ();
        executor.post (() -> {
            List<Device> devices = new ArrayList<> ();
            Map<String, String> details = new HashMap<> ();
            int success = 0, failed = 0, total = 0;
            for (BatchRegistrationPayload.BatchDevice bd : payload.devices) {
                if (bd.devices != null) {
                    for (String imei : bd.devices) {
                        if (service.isDevicePresent (imei)) {
                            details.put (imei, "设备已存在");
                            failed ++;
                        } else {
                            Device device = new Device ();
                            device.setImei (imei);
                            device.setVendor (bd.vendor);
                            device.setCategory (bd.category);
                            device.setModel (bd.model);
                            device.setModule (bd.module);
                            devices.add (device);
                            success ++;
                        }
                        total ++;
                    }
                }
            }

            if (!devices.isEmpty ()) {
                service.saveDevices (devices.toArray (new Device[0]));
            }

            BatchRegistrationResult brr = new BatchRegistrationResult ();
            brr.taskId = taskId;
            if (details.isEmpty ()) {
                brr.code = 0;
                brr.message = "success";
            } else {
                brr.detail = details;
                if (failed < total) {
                    brr.code = 1;
                    brr.message = "partially-success";
                } else {
                    brr.code = -1;
                    brr.message = "failed";
                }
            }

            if (!StringUtil.isEmpty (payload.url)) {
                try {
                    HttpClient.post (payload.url, new Gson ().toJson (brr), HttpClient.JSON);
                } catch (IOException ex) {
                    logger.warn (ex.getMessage (), ex);
                }
            }
        });

        Map<Object, Object> map = CollectionCreator.asMap ("task_id", taskId);
        if (StringUtil.isEmpty (payload.url)) {
            return new ResponseEntity<> (SC_ACCEPTED, "请求已接受，由于没有提供回调地址，执行结果将不会被通知!", map);
        } else {
            return new ResponseEntity<> (SC_ACCEPTED, "Accepted", map);
        }
    }

    @ARestfulAPI (method = "post")
    public Reply register (@ARequestBody Device device) {
        if (StringUtil.isEmpty (device.getImei ())) {
            return new Reply (ERR_MISSING_IMEI, "设备IMEI缺失");
        }
        if (StringUtil.isEmpty (device.getVendor ()) ||
            StringUtil.isEmpty (device.getCategory ()) ||
            StringUtil.isEmpty (device.getModel ())) {
            return new Reply (ERR_MISSING_VENDOR, "三元组缺失或不完整");
        }

        if (service.isDevicePresent (device.getImei ())) {
            return new Reply (ERR_DEVICE_EXISTS, "设备已注册");
        }
        // 补齐module和protocol
        if (StringUtil.isEmpty (device.getModule ()) || StringUtil.isEmpty (device.getProtocol ())) {
            DeviceTypeConfig dtc = service.getDeviceTypeConfig (device.getVendor (), device.getCategory (), device.getModel ());
            if (dtc != null) {
                device.setModule (dtc.getModule ());
                device.setProtocol (dtc.getProtocol ().name ());
            } else {
                return new Reply (ERR_UNKNOWN_DEVICE_TYPE, "未知的设备三元组");
            }
        }
        service.save (device);
        if (broadcaster != null) {
            broadcaster.broadcast (device.getModule (), DEVICE_REGISTERED, device);
        }
        return new Reply (ERR_OK);
    }


    @ARestfulAPI (urlPattern = "/${imei}", method = "delete")
    public Reply remove (@AWebParam (name = "imei", location = Path) String imei) {
        if (StringUtil.isEmpty (imei)) {
            throw new RestfulException ("无效的参数", ERR_INVALID_PARAMETER);
        }

        if (imei.contains (",")) {
            String[] temp = imei.split (",");
            List<String> list = new ArrayList<> (temp.length);
            for (String id : temp) {
                if (!StringUtil.isEmpty (id)) {
                    list.add (id.trim ());
                }
            }
            service.deleteDevice (list.toArray (new String[0]));
        } else {
            Device device = service.getByImei (imei);
            if (device != null) {
                service.deleteDevice (imei.trim ());
                if (broadcaster != null) {
                    broadcaster.broadcast (device.getModule (), DEVICE_REMOVED, device);
                }
            }
        }
        return new Reply (ERR_OK);
    }

    @ARestfulAPI (urlPattern = "/${imei}", method = "post")
    public ResponseEntity<?> issue (@AWebParam (name = "imei", location = Path) String imei, @ARequestBody String body) {
        if (!service.isDevicePresent (imei)) {
            return new ResponseEntity<> (SC_NOT_FOUND, "设备[" + imei + "]不存在");
        }

        DeviceCommand command = new DeviceCommand ();
        command.setImei (imei);
        command.setCommand (body);
        command.setPerformed (false);
        command.setReceiveTime (new Timestamp (System.currentTimeMillis ()));
        service.save (command);
        // 把command.id 当作 request_id 返回，以便后续的应答对应
        Map<String, Integer> map = CollectionCreator.asMap ("request_id", command.getId ());
        return new ResponseEntity<> (SC_ACCEPTED, null, map);
    }
}