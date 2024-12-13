package org.dreamwork.integrated.common.device.manager.api.model.database.schema;

import org.dreamwork.persistence.DatabaseSchema;

public class DownlinkSerialSchema extends DatabaseSchema {
    public DownlinkSerialSchema () {
        tableName = "downlink_serial";
        fields = new String[] {"id", "imei", "module_name", "current_value"};
    }

    @Override
    public String getCreateDDL () {
        return "create table downlink_serial (\n" +
                "    id                          varchar(32)         not null primary key,\n" +
                "    imei                        varchar(64),\n" +
                "    module_name                 varchar(64),\n" +
                "    current_value               int                 default 1\n" +
                ")\n" +
                "create index idx_ds_module on downlink_serial (module_name);\n" +
                "create index idx_ds_imei on downlink_serial (imei);";
    }

    @Override
    public String getPrimaryKeyName () {
        return "id";
    }
}
