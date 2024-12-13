package org.dreamwork.integrated.common.device.manager.api.model.database;

import com.google.gson.annotations.SerializedName;
import org.dreamwork.integrated.common.device.manager.api.model.NetworkProtocol;
import org.dreamwork.integrated.common.device.manager.api.model.database.schema.DeviceTypeConfigSchema;
import org.dreamwork.persistence.ISchema;
import org.dreamwork.persistence.ISchemaField;

import java.io.Serializable;

@ISchema (DeviceTypeConfigSchema.class)
public class DeviceTypeConfig implements Serializable {
    @ISchemaField (id = true, name = "id")
    @SerializedName ("id")
    private String id;

    @ISchemaField (name = "module_name")
    @SerializedName ("module")
    private String module;

    @ISchemaField (name = "device_protocol")
    @SerializedName ("protocol")
    private NetworkProtocol protocol;

    @ISchemaField (name = "vendor")
    @SerializedName ("vendor")
    private String vendor;

    @ISchemaField (name = "device_category")
    @SerializedName ("category")
    private String category;

    @ISchemaField (name = "device_model")
    @SerializedName ("model")
    private String model;

    public DeviceTypeConfig () {
    }

    public DeviceTypeConfig (String module, NetworkProtocol protocol, String vendor, String category, String model) {
        this.module = module;
        this.protocol = protocol;
        this.vendor = vendor;
        this.category = category;
        this.model = model;
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

    public NetworkProtocol getProtocol () {
        return protocol;
    }

    public void setProtocol (NetworkProtocol protocol) {
        this.protocol = protocol;
    }

    public String getVendor () {
        return vendor;
    }

    public void setVendor (String vendor) {
        this.vendor = vendor;
    }

    public String getCategory () {
        return category;
    }

    public void setCategory (String category) {
        this.category = category;
    }

    public String getModel () {
        return model;
    }

    public void setModel (String model) {
        this.model = model;
    }
}
