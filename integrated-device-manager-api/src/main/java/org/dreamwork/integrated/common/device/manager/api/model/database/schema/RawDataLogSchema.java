package org.dreamwork.integrated.common.device.manager.api.model.database.schema;

import org.dreamwork.persistence.*;

public class RawDataLogSchema extends DatabaseSchema {
	public  RawDataLogSchema () {
		tableName = "raw_data_log";
		fields = new String[] {
				"id", "module_name", "vendor", "device_category",
				"device_model", "remote_address", "raw_data", "receive_time"
		};
	}

	@Override
	public String getPrimaryKeyName () {
		return "null";
	}

	@Override
	public String getCreateDDL () {
		return "create table raw_data_log (" +
"    id                          varchar(32)," +
"    module_name                 varchar(64)," +
"    vendor                      varchar(64)," +
"    device_category             varchar(64)," +
"    device_model                varchar(64)," +
"    remote_address              varchar(64)," +
"    raw_data                    text,       " +
"    receive_time                timestamp   " +
") partition by range (receive_time);\n" +
"create table raw_data_log_default partition of raw_data_log default;\n" +
"alter table raw_data_log_default add primary key (id);\n" +
"select f_create_partition ('raw_data_log', current_timestamp::timestamp);";
	}
}