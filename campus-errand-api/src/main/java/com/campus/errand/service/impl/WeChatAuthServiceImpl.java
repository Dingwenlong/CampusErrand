package com.campus.errand.service.impl;

import com.campus.errand.service.WeChatAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class WeChatAuthServiceImpl implements WeChatAuthService {

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.wechat.app-id:}")
    private String appId;

    @Value("${app.wechat.app-secret:}")
    private String appSecret;

    @Value("${app.auth.wechat-mock-enabled:false}")
    private boolean mockEnabled;

    @Override
    @SuppressWarnings("unchecked")
    public WeChatSession code2Session(String code) {
        if (mockEnabled) {
            return new WeChatSession("dev_openid_" + code, null);
        }
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            throw new RuntimeException("微信登录配置缺失");
        }

        String url = UriComponentsBuilder.fromHttpUrl(CODE2SESSION_URL)
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();

        Map<String, Object> response;
        try {
            response = restTemplate.getForObject(url, Map.class);
        } catch (RestClientException e) {
            throw new RuntimeException("微信登录服务暂不可用");
        }

        if (response == null) {
            throw new RuntimeException("微信登录无响应");
        }
        Object errcode = response.get("errcode");
        if (errcode != null && !"0".equals(String.valueOf(errcode))) {
            throw new RuntimeException("微信登录失败：" + response.getOrDefault("errmsg", errcode));
        }
        Object openid = response.get("openid");
        if (openid == null || String.valueOf(openid).isBlank()) {
            throw new RuntimeException("微信登录未返回openid");
        }
        Object unionid = response.get("unionid");
        return new WeChatSession(String.valueOf(openid), unionid == null ? null : String.valueOf(unionid));
    }
}
