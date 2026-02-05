package com.campus.errand.dto;

import jakarta.validation.constraints.NotNull;

public class TaskCancelDTO {

    @NotNull(message = "取消类型不能为空")
    private Integer cancelType;

    private String reason;

    public Integer getCancelType() {
        return cancelType;
    }

    public void setCancelType(Integer cancelType) {
        this.cancelType = cancelType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
