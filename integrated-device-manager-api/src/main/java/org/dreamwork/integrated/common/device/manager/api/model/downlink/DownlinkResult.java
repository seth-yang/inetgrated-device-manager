package org.dreamwork.integrated.common.device.manager.api.model.downlink;

import java.io.Serializable;

public class DownlinkResult implements Serializable {
    public int code;
    public long serial;
    public String message;

    public DownlinkResult () {}

    public DownlinkResult (int code, long serial, String message) {
        this.code = code;
        this.serial = serial;
        this.message = message;
    }
}