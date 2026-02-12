package com.campus.errand.handler;

import com.campus.errand.common.Result;
import com.campus.errand.common.ResultCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void runtimeExceptionShouldReturnBusinessMessage() {
        Result<Void> result = handler.handleRuntimeException(new RuntimeException("支付密码错误"));

        assertThat(result.getCode()).isEqualTo(ResultCode.ERROR.getCode());
        assertThat(result.getMessage()).isEqualTo("支付密码错误");
    }

    @Test
    void runtimeExceptionWithoutMessageShouldUseDefaultMessage() {
        Result<Void> result = handler.handleRuntimeException(new RuntimeException());

        assertThat(result.getCode()).isEqualTo(ResultCode.ERROR.getCode());
        assertThat(result.getMessage()).isEqualTo(ResultCode.ERROR.getMessage());
    }

    @Test
    void genericExceptionShouldReturnSystemError() {
        Result<Void> result = handler.handleException(new Exception("unexpected"));

        assertThat(result.getCode()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(result.getMessage()).isEqualTo(ResultCode.SYSTEM_ERROR.getMessage());
    }
}
