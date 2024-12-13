package org.dreamwork.integrated.common.device.manager.api.model.mqtt;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Reply implements Serializable {
    @SerializedName ("serialCode")
    public String serial;

    @SerializedName ("result")
    public Boolean result;

    @SerializedName ("attr")
    public List<Attribute> attributes;

    public Reply () {
    }

    public Reply (String serial, Boolean result) {
        this.serial = serial;
        this.result = result;
    }
}
