package org.dreamwork.integrated.common.device.manager.api.services;

import org.dreamwork.integrated.common.device.manager.api.model.database.MqttLog;

public interface IMqttLogService {
    void save (MqttLog... logs);
}
