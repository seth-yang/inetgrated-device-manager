package org.dreamwork.integrated.common.device.manager.api.model.database;

import com.google.gson.annotations.SerializedName;
import org.dreamwork.integrated.common.device.manager.api.model.database.schema.MqttLogSchema;
import org.dreamwork.persistence.ISchema;
import org.dreamwork.persistence.ISchemaField;
import org.dreamwork.util.StringUtil;

import java.io.Serializable;
import java.sql.Timestamp;

@ISchema (MqttLogSchema.class)
public class MqttLog implements Serializable {
    // "id", "topic", "payload", "send_time"
    @ISchemaField (id = true, name = "id")
    private String id;

    @ISchemaField (name = "topic")
    private String topic;

    @ISchemaField (name = "payload")
    private String payload;

    @ISchemaField (name = "send_time")
    @SerializedName ("send_time")
    private Timestamp sendTime;

    public MqttLog () {}

    public MqttLog (String topic, String payload) {
        this.id = StringUtil.uuid ();
        this.topic = topic;
        this.payload = payload;
        this.sendTime = new Timestamp (System.currentTimeMillis ());
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getTopic () {
        return topic;
    }

    public void setTopic (String topic) {
        this.topic = topic;
    }

    public String getPayload () {
        return payload;
    }

    public void setPayload (String payload) {
        this.payload = payload;
    }

    public Timestamp getSendTime () {
        return sendTime;
    }

    public void setSendTime (Timestamp sendTime) {
        this.sendTime = sendTime;
    }
}