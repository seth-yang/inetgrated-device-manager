package org.dreamwork.integrated.common.device.manager.api.model.database;

import org.dreamwork.integrated.common.device.manager.api.model.database.schema.*;
import org.dreamwork.persistence.DatabaseSchema;

public class DeviceManagerSchema {
	public static void registerAllSchemas () {
		DatabaseSchema.register(DeviceSchema.class);
		DatabaseSchema.register(RawDataLogSchema.class);
		DatabaseSchema.register(DeviceCommandSchema.class);
		DatabaseSchema.register(DownlinkLogSchema.class);
		DatabaseSchema.register(DownlinkSerialSchema.class);
		DatabaseSchema.register(DeviceTypeConfigSchema.class);
		DatabaseSchema.register(MqttLogSchema.class);
	}
}
