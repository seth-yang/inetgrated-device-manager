package org.dreamwork.integrated.common.device.manager.api.model.mqtt;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Event implements Serializable {
    @SerializedName ("eventCode")
    public String code;

    @SerializedName ("alertVal")
    public String value;

    public Event () {
    }

    public Event (String code, String value) {
        this.code = code;
        this.value = value;
    }
}