package org.dreamwork.integrated.common.device.manager.api.model.database.schema;

import org.dreamwork.persistence.DatabaseSchema;

public class DownlinkLogSchema extends DatabaseSchema {
    public DownlinkLogSchema () {
        tableName = "downlink_log";
        fields = new String[] {
                "id", "imei", "module_name", "serial_no", "downlink_time", "downlink_status",
                "reply_time", "command", "reply"
        };
    }

    @Override
    public String getCreateDDL () {
        return "create table downlink_log (\n" +
                "    id                          varchar(32),\n" +
                "    imei                        varchar(32),\n" +
                "    module_name                 varchar(64),\n" +
                "    serial_no                   int,\n" +
                "    downlink_time               timestamp,\n" +
                "    downlink_status             varchar(16),\n" +
                "    reply_time                  timestamp,\n" +
                "    command                     text,\n" +
                "    reply                       text\n" +
                ") partition by range (downlink_time);\n" +
                "create index idx_dl_time on downlink_log (downlink_time);\n" +
                "create table downlink_log_default partition of downlink_log default;\n" +
                "alter table downlink_log_default add primary key (id);\n" +
                "select f_create_partition ('downlink_log', current_timestamp::timestamp);";
    }

    @Override
    public String getPrimaryKeyName () {
        return "id";
    }
}
