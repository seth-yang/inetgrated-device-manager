package org.dreamwork.integrated.common.device.manager.api.model.database;

import org.dreamwork.integrated.common.device.manager.api.model.database.schema.RawDataLogSchema;
import org.dreamwork.persistence.*;

@ISchema (RawDataLogSchema.class)
public class RawDataLog {
	@ISchemaField (name = "id", id = true)
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("id")
	private String id;

	@ISchemaField (name = "module_name")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("module")
	private String module;

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

	@ISchemaField (name = "remote_address")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("remote_address")
	private String remote;

	@ISchemaField (name = "raw_data")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("raw_data")
	private String rawData;

	@ISchemaField (name = "receive_time")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("receive_time")
	private java.sql.Timestamp receiveTime;

	public String getId () {
		return id;
	}

	public void setId (String id) {
		this.id = id;
	}

	public String getModule () {
		return module;
	}

	public void setModule (String module) {
		this.module = module;
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

	public String getRemote () {
		return remote;
	}

	public void setRemote (String remote) {
		this.remote = remote;
	}

	public String getRawData () {
		return rawData;
	}

	public void setRawData (String rawData) {
		this.rawData = rawData;
	}

	public java.sql.Timestamp getReceiveTime () {
		return receiveTime;
	}

	public void setReceiveTime (java.sql.Timestamp receiveTime) {
		this.receiveTime = receiveTime;
	}

	@Override
	public boolean equals (java.lang.Object o) {
		if (this == o) return true;
		if (o == null || getClass () != o.getClass ()) return false;
		RawDataLog that = (RawDataLog) o;
		return id != null && id.equals (that.id);
	}

	@Override
	public int hashCode () {
		return id != null ? id.hashCode () : 0;
	}
}