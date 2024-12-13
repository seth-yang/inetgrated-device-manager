package org.dreamwork.integrated.common.device.manager.model;

import java.io.Serializable;

public class Reply implements Serializable {
    public int code;
    public String message;
    public Object data;

    public Reply (int code) {
        this.code = code;
    }

    public Reply (int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Reply (int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}