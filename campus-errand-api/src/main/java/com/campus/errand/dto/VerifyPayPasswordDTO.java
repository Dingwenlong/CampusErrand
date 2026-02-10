package com.campus.errand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyPayPasswordDTO {

    @NotBlank(message = "支付密码不能为空")
    @Size(min = 6, max = 6, message = "支付密码必须为6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    private String payPassword;

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }
}
