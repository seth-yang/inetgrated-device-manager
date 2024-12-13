package org.dreamwork.integrated.common.device.manager;

import org.dreamwork.integration.api.IModule;
import org.dreamwork.integration.api.annotation.AModule;

import javax.annotation.Resource;

@Resource
@AModule ({
        "org.dreamwork.integrated.common.device.manager.restful",
        "org.dreamwork.integrated.common.device.manager.service.impl",
})
public class DeviceManagementModule implements IModule {
    public static final String VERSION = "1.0.0";
}