package org.dreamwork.integrated.common.device.manager.service.impl;

import org.dreamwork.integrated.common.device.manager.api.model.database.MqttLog;
import org.dreamwork.integrated.common.device.manager.api.services.IMqttLogService;

import javax.annotation.Resource;

@Resource
public class MqttLogServiceImpl implements IMqttLogService {
    @Resource
    private DeviceManageServiceImpl shadow;

    @Override
    public void save (MqttLog... logs) {
        shadow.save (logs);
    }
}
