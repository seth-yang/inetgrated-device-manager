package org.dreamwork.integrated.common.device.manager.api.model.mqtt;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class MqttMessage implements Serializable {
    @SerializedName ("imei")
    public String imei;

    @SerializedName ("reportTime")
    public Date reportTime;

    @SerializedName ("attr")
    public List<Attribute> attributes;

    @SerializedName ("event")
    public Event event;

    @SerializedName ("reply")
    public Reply reply;

    @SerializedName ("secondary")
    public boolean secondary;

    public static final MqttMessage QUIT = new MqttMessage ();
}