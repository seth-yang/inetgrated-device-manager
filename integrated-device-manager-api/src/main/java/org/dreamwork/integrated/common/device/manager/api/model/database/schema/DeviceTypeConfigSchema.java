package org.dreamwork.integrated.common.device.manager.api.model.database.schema;

import org.dreamwork.persistence.DatabaseSchema;

public class DeviceTypeConfigSchema extends DatabaseSchema {
    public DeviceTypeConfigSchema () {
        tableName = "device_type_config";
        fields = new String[] {
                "id", "module_name", "device_protocol",
                "vendor", "device_category", "device_model"
        };
    }

    @Override
    public String getCreateDDL () {
        return "create table device_type_config (\n" +
                "    id                          varchar(32)     not null primary key," +
                "    module_name                 varchar(64),\n" +
                "    device_protocol             varchar(16),\n" +
                "    vendor                      varchar(64),\n" +
                "    device_category             varchar(64),\n" +
                "    device_model                varchar(64)\n" +
                ")";
    }

    @Override
    public String getPrimaryKeyName () {
        return "id";
    }
}
