package org.dreamwork.integrated.common.device.manager.api.model.database;

import org.dreamwork.integrated.common.device.manager.api.model.database.schema.DownlinkSerialSchema;
import org.dreamwork.persistence.ISchema;
import org.dreamwork.persistence.ISchemaField;
import org.dreamwork.util.StringUtil;

import java.io.Serializable;

@ISchema (DownlinkSerialSchema.class)
public class DownlinkSerial implements Serializable {
    public DownlinkSerial () {
    }

    public DownlinkSerial (String module, String imei) {
        this.id = StringUtil.uuid ();
        this.imei = imei;
        this.module = module;
    }

    public DownlinkSerial (String module, String imei, int value) {
        this.id = StringUtil.uuid ();
        this.imei = imei;
        this.value = value;
        this.module = module;
    }

    @ISchemaField (name = "id", id = true)
    private String id;

    @ISchemaField (name = "imei")
    private String imei;

    @ISchemaField (name = "module_name")
    private String module;

    @ISchemaField (name = "current_value")
    private Integer value;

    public String getImei () {
        return imei;
    }

    public void setImei (String imei) {
        this.imei = imei;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getModule () {
        return module;
    }

    public void setModule (String module) {
        this.module = module;
    }

    public int getValue () {
        return value == null ? 1 : value;
    }

    public void setValue (int value) {
        this.value = value;
    }

    public DownlinkSerial increment () {
        value ++;
        return this;
    }
}
