package org.dreamwork.integrated.common.device.manager.restful;

import org.dreamwork.integrated.common.device.manager.api.model.database.Device;
import org.dreamwork.integrated.common.device.manager.api.model.database.DownlinkLog;
import org.dreamwork.integrated.common.device.manager.api.model.database.MqttLog;
import org.dreamwork.integrated.common.device.manager.api.model.database.RawDataLog;
import org.dreamwork.integrated.common.device.manager.service.impl.WebUIService;
import org.dreamwork.integration.api.IModuleContext;
import org.dreamwork.integration.httpd.annotation.AFormItem;
import org.dreamwork.integration.httpd.annotation.ARestfulAPI;
import org.dreamwork.util.IDataCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

@ARestfulAPI ("/ui")
public class WebUI {
    private final Logger logger = LoggerFactory.getLogger (WebUI.class);

    private final WebUIService ui;

    public WebUI (IModuleContext context) {
        ui = context.findService (WebUIService.class);
    }

    @ARestfulAPI ("/devices")
    public IDataCollection<Device> queryDevice (@AFormItem (value = "pageNo", defaultValue = "1") int pageNo,
                                                @AFormItem (value = "pageSize", defaultValue = "10") int pageSize,
                                                @AFormItem ("imei") String imei,
                                                @AFormItem ("module") String moduleName,
                                                @AFormItem ("protocol") String protocol,
                                                @AFormItem ("vendor") String vendor,
                                                @AFormItem ("category") String category,
                                                @AFormItem ("model") String model,
                                                @AFormItem ("start") Date start, @AFormItem ("end") Date end) {
        return null;
    }

    @ARestfulAPI ("/logs/raw")
    public IDataCollection<RawDataLog> queryRawLog (@AFormItem (value = "pageNo", defaultValue = "1") int pageNo,
                                                    @AFormItem (value = "pageSize", defaultValue = "10") int pageSize,
                                                    @AFormItem ("module") String moduleName,
                                                    @AFormItem ("protocol") String protocol,
                                                    @AFormItem ("vendor") String vendor,
                                                    @AFormItem ("category") String category,
                                                    @AFormItem ("model") String model,
                                                    @AFormItem ("start") Date start, @AFormItem ("end") Date end) {
        return null;
    }

    @ARestfulAPI ("/logs/downlink")
    public IDataCollection<DownlinkLog> queryDownlinkLog (@AFormItem (value = "pageNo", defaultValue = "1") int pageNo,
                                                          @AFormItem (value = "pageSize", defaultValue = "10") int pageSize,
                                                          @AFormItem ("imei") String imei,
                                                          @AFormItem ("module") String moduleName,
                                                          @AFormItem ("protocol") String protocol,
                                                          @AFormItem ("vendor") String vendor,
                                                          @AFormItem ("category") String category,
                                                          @AFormItem ("model") String model,
                                                          @AFormItem ("start") Date start, @AFormItem ("end") Date end) {
        return null;
    }

    @ARestfulAPI ("/logs/mqtt")
    public IDataCollection<MqttLog> queryMqttLog (@AFormItem (value = "pageNo", defaultValue = "1") int pageNo,
                                                  @AFormItem (value = "pageSize", defaultValue = "10") int pageSize,
                                                  @AFormItem ("imei") String imei,
                                                  @AFormItem ("module") String moduleName,
                                                  @AFormItem ("protocol") String protocol,
                                                  @AFormItem ("vendor") String vendor,
                                                  @AFormItem ("category") String category,
                                                  @AFormItem ("model") String model,
                                                  @AFormItem ("start") Date start, @AFormItem ("end") Date end) {
        return null;
    }

    @ARestfulAPI ("/modules")
    public List<String> getModules () {

        return null;
    }

    @ARestfulAPI ("/protocols")
    private List<String> getProtocols () {
        return null;
    }

    @ARestfulAPI ("/basic-data")
    public Map<String, Object> getBasicData () {
        return null;
    }
}