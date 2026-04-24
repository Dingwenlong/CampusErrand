package com.campus.errand.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminVerifyRejectDTO {

    @NotBlank(message = "驳回原因不能为空")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
