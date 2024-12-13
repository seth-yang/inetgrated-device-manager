package org.dreamwork.integrated.common.device.manager.api.model.database;

import org.dreamwork.integrated.common.device.manager.api.model.database.schema.DeviceSchema;
import org.dreamwork.persistence.*;

@ISchema (DeviceSchema.class)
public class Device {
	@ISchemaField (name = "imei", id = true)
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("imei")
	private String imei;

	@ISchemaField (name = "module_name")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("module")
	private String module;

	@ISchemaField (name = "protocol")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("protocol")
	private String protocol;

	@ISchemaField (name = "vendor")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("vendor")
	private String vendor;

	@ISchemaField (name = "device_category")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("category")
	private String category;

	@ISchemaField (name = "device_model")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("model")
	private String model;

	@ISchemaField (name = "register_time")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("register_time")
	private java.sql.Timestamp registerTime;

	public String getImei () {
		return imei;
	}

	public void setImei (String imei) {
		this.imei = imei;
	}

	public String getModule () {
		return module;
	}

	public void setModule (String module) {
		this.module = module;
	}

	public String getProtocol () {
		return protocol;
	}

	public void setProtocol (String protocol) {
		this.protocol = protocol;
	}

	public String getVendor () {
		return vendor;
	}

	public void setVendor (String vendor) {
		this.vendor = vendor;
	}

	public String getCategory () {
		return category;
	}

	public void setCategory (String category) {
		this.category = category;
	}

	public String getModel () {
		return model;
	}

	public void setModel (String model) {
		this.model = model;
	}

	public java.sql.Timestamp getRegisterTime () {
		return registerTime;
	}

	public void setRegisterTime (java.sql.Timestamp registerTime) {
		this.registerTime = registerTime;
	}

	@Override
	public boolean equals (java.lang.Object o) {
		if (this == o) return true;
		if (o == null || getClass () != o.getClass ()) return false;
		Device that = (Device) o;
		return imei != null && imei.equals (that.imei);
	}

	@Override
	public int hashCode () {
		return imei != null ? imei.hashCode () : 0;
	}
}