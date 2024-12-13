package org.dreamwork.integrated.common.device.manager.api.model.database.schema;

import org.dreamwork.persistence.*;

public class DeviceCommandSchema extends DatabaseSchema {
	public  DeviceCommandSchema () {
		tableName = "device_command";
		fields = new String[] {"id", "imei", "command", "receive_time", "performed", "downlink_time", "downlink_state", "downlink_detail"};
	}

	@Override
	public String getPrimaryKeyName () {
		return "id";
	}

	@Override
	public String getCreateDDL () {
		return "create table device_command (" +
"    id                          serial                     not null primary key," +
"    imei                   	 varchar(64)," +
"    command                     text,                                                   " +
"    receive_time                timestamp,                                              " +
"    performed                   boolean,                                                " +
"    downlink_time               timestamp,                                              " +
"    downlink_state              varchar(16),                                            " +
"    downlink_detail             text                                                    " +
");\n" +
"create index idx_dc_imei on device_command (imei)";
	}
}