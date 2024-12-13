package org.dreamwork.integrated.common.device.manager.api.model.database.schema;

import org.dreamwork.persistence.DatabaseSchema;

public class MqttLogSchema extends DatabaseSchema {
    public MqttLogSchema () {
        tableName = "mqtt_log";
        fields = new String[] {"id", "topic", "payload", "send_time"};
    }

    @Override
    public String getCreateDDL () {
        return "create table mqtt_log (\n" +
                "    id                          varchar(32),\n" +
                "    topic                       varchar(128),\n" +
                "    payload                     text,\n" +
                "    send_time                   timestamp\n" +
                ") partition by range (send_time);\n" +
                "create index idx_mqtt_time on mqtt_log (send_time);\n" +
                "create table raw_data_log_default partition of raw_data_log default;\n" +
                "alter table raw_data_log_default add primary key (id);\n" +
                "select f_create_partition ('raw_data_log', current_timestamp::timestamp)";
    }

    @Override
    public String getPrimaryKeyName () {
        return "id";
    }
}
