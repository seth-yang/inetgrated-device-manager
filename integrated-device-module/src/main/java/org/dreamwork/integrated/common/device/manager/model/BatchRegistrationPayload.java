package org.dreamwork.integrated.common.device.manager.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BatchRegistrationPayload implements Serializable {
    @SerializedName ("callback_url")
    public String url;

    @SerializedName ("devices")
    public BatchDevice[] devices;

    public static final class BatchDevice implements Serializable {
        public String vendor, category, model, module;
        public String[] devices;
    }
}