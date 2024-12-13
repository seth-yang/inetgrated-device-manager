package org.dreamwork.integrated.common.device.manager.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class BatchRegistrationResult implements Serializable {
    public int code;                    // 0 成功，-1:完全失败, 1 部分成功
    @SerializedName ("task_id")
    public String taskId;               // 异步接口的任务id
    public String message;              // success - 成功; failed - 完全失败; partially-success 部分成功
    public Map<String, String> detail;  // 记录了每台设备失败的具体原因
}