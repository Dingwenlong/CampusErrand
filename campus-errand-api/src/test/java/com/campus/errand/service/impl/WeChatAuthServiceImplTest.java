package com.campus.errand.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeChatAuthServiceImplTest {

    @Test
    void code2SessionShouldUseMockWhenEnabled() {
        WeChatAuthServiceImpl service = new WeChatAuthServiceImpl();
        ReflectionTestUtils.setField(service, "mockEnabled", true);

        var session = service.code2Session("abc123");

        assertThat(session.getOpenid()).isEqualTo("dev_openid_abc123");
        assertThat(session.getUnionid()).isNull();
    }

    @Test
    void code2SessionShouldFailWhenProductionConfigMissing() {
        WeChatAuthServiceImpl service = new WeChatAuthServiceImpl();
        ReflectionTestUtils.setField(service, "mockEnabled", false);
        ReflectionTestUtils.setField(service, "appId", "");
        ReflectionTestUtils.setField(service, "appSecret", "");

        assertThatThrownBy(() -> service.code2Session("abc123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("微信登录配置缺失");
    }
}
