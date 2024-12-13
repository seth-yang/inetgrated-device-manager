package org.dreamwork.integrated.common.device.manager.api.model.database;

import org.dreamwork.integrated.common.device.manager.api.model.database.schema.DeviceCommandSchema;
import org.dreamwork.persistence.*;

@ISchema (DeviceCommandSchema.class)
public class DeviceCommand {
	@ISchemaField (name = "id", id = true)
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("id")
	private Integer id;

	@ISchemaField (name = "imei")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("imei")
	private String imei;

	@ISchemaField (name = "command")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("command")
	private String command;

	@ISchemaField (name = "receive_time")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("receive_time")
	private java.sql.Timestamp receiveTime;

	@ISchemaField (name = "performed")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("performed")
	private Boolean performed;

	@ISchemaField (name = "downlink_time")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("downlink_time")
	private java.sql.Timestamp downlinkTime;

	@ISchemaField (name = "downlink_state")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("downlink_state")
	private String downlinkState;

	@ISchemaField (name = "downlink_detail")
	@com.google.gson.annotations.Expose
	@com.google.gson.annotations.SerializedName ("downlink_detail")
	private String downlinkDetail;

	public Integer getId () {
		return id;
	}

	public void setId (Integer id) {
		this.id = id;
	}

	public String getImei () {
		return imei;
	}

	public void setImei (String imei) {
		this.imei = imei;
	}

	public String getCommand () {
		return command;
	}

	public void setCommand (String command) {
		this.command = command;
	}

	public java.sql.Timestamp getReceiveTime () {
		return receiveTime;
	}

	public void setReceiveTime (java.sql.Timestamp receiveTime) {
		this.receiveTime = receiveTime;
	}

	public Boolean isPerformed () {
		return performed;
	}

	public void setPerformed (Boolean performed) {
		this.performed = performed;
	}

	public java.sql.Timestamp getDownlinkTime () {
		return downlinkTime;
	}

	public void setDownlinkTime (java.sql.Timestamp downlinkTime) {
		this.downlinkTime = downlinkTime;
	}

	public String getDownlinkState () {
		return downlinkState;
	}

	public void setDownlinkState (String downlinkState) {
		this.downlinkState = downlinkState;
	}

	public String getDownlinkDetail () {
		return downlinkDetail;
	}

	public void setDownlinkDetail (String downlinkDetail) {
		this.downlinkDetail = downlinkDetail;
	}

	@Override
	public boolean equals (java.lang.Object o) {
		if (this == o) return true;
		if (o == null || getClass () != o.getClass ()) return false;
		DeviceCommand that = (DeviceCommand) o;
		return id != null && id.equals (that.id);
	}

	@Override
	public int hashCode () {
		return id != null ? id.hashCode () : 0;
	}
}