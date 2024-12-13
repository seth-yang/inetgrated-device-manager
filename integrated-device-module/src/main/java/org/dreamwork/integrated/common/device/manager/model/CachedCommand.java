package org.dreamwork.integrated.common.device.manager.model;

import org.dreamwork.integrated.common.device.manager.api.model.database.DownlinkLog;
import org.dreamwork.integrated.common.device.manager.api.model.downlink.DownlinkCommand;

import java.util.concurrent.locks.Condition;

public class CachedCommand {
    public String imei;
    public long serialNo;
    public DownlinkCommand command;
    public DownlinkLog log;
    public Long timestamp;
    public final long issuedAt;
    public Condition condition;

    public CachedCommand (String imei, long serialNo, DownlinkCommand command, DownlinkLog log) {
        this.imei = imei;
        this.serialNo = serialNo;
        this.command = command;
        this.log = log;
        this.timestamp = System.currentTimeMillis ();
        issuedAt = System.currentTimeMillis ();
    }
}