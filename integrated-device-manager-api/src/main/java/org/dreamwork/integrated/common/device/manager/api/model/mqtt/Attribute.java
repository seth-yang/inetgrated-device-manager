package org.dreamwork.integrated.common.device.manager.api.model.mqtt;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Attribute implements Serializable {
    @SerializedName ("attrCode")
    public String code;

    @SerializedName ("value")
    public Object value;

    public Attribute () {
    }

    public Attribute (String code, Object value) {
        this.code = code;
        this.value = value;
    }
}
