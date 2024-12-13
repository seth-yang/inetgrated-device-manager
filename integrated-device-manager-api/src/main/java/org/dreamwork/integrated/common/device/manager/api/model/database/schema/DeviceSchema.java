package org.dreamwork.integrated.common.device.manager.api.model.database.schema;

import org.dreamwork.persistence.*;

public class DeviceSchema extends DatabaseSchema {
	public  DeviceSchema () {
		tableName = "device";
		fields = new String[] {"imei", "module_name", "protocol", "vendor", "device_category", "device_model", "register_time"};
	}

	@Override
	public String getPrimaryKeyName () {
		return "imei";
	}

	@Override
	public String getCreateDDL () {
		return "create table device (" +
"    imei                        varchar(64)                     not null primary key,   " +
"    module_name                 varchar(64),                                            " +
"    protocol                    varchar(64),                                            " +
"    vendor                      varchar(64),                                            " +
"    device_category             varchar(64),                                            " +
"    device_model                varchar(64),                                            " +
"    register_time               timestamp                                               " +
")";
	}
}