package org.dreamwork.integrated.common.device.manager.api.model.downlink;

import java.io.Serializable;

public abstract class DownlinkCommand implements Serializable {
    public Long serialNo;
    public String imei;

    public abstract boolean isCacheable ();

    public abstract byte[] toByteArray ();
}