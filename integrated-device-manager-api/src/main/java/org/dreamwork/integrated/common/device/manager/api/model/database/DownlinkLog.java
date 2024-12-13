package org.dreamwork.integrated.common.device.manager.api.model.database;

import org.dreamwork.integrated.common.device.manager.api.model.DownlinkStatus;
import org.dreamwork.integrated.common.device.manager.api.model.database.schema.DownlinkLogSchema;
import org.dreamwork.persistence.ISchema;
import org.dreamwork.persistence.ISchemaField;

import java.io.Serializable;
import java.sql.Timestamp;

@ISchema (DownlinkLogSchema.class)
public class DownlinkLog implements Serializable {
    @ISchemaField (id = true, name = "id")
    private String id;

    @ISchemaField (name = "imei")
    private String imei;

    @ISchemaField (name = "module_name")
    private String module;

    @ISchemaField (name = "serial_no")
    private Long serialNo;

    @ISchemaField (name = "downlink_time")
    private Timestamp downlinkTime;

    @ISchemaField (name = "downlink_status")
    private DownlinkStatus status;

    @ISchemaField (name = "reply_time")
    private Timestamp replyTime;

    @ISchemaField (name = "command")
    private String command;

    @ISchemaField (name = "reply")
    private String reply;

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getImei () {
        return imei;
    }

    public void setImei (String imei) {
        this.imei = imei;
    }

    public String getModule () {
        return module;
    }

    public void setModule (String module) {
        this.module = module;
    }

    public Long getSerialNo () {
        return serialNo == null ? 1 : serialNo;
    }

    public void setSerialNo (long serialNo) {
        this.serialNo = serialNo;
    }

    public Timestamp getDownlinkTime () {
        return downlinkTime;
    }

    public void setDownlinkTime (Timestamp downlinkTime) {
        this.downlinkTime = downlinkTime;
    }

    public DownlinkStatus getStatus () {
        return status;
    }

    public void setStatus (DownlinkStatus status) {
        this.status = status;
    }

    public Timestamp getReplyTime () {
        return replyTime;
    }

    public void setReplyTime (Timestamp replyTime) {
        this.replyTime = replyTime;
    }

    public String getCommand () {
        return command;
    }

    public void setCommand (String command) {
        this.command = command;
    }

    public String getReply () {
        return reply;
    }

    public void setReply (String reply) {
        this.reply = reply;
    }
}