package com.campus.errand.service.impl;

import com.campus.errand.service.WeChatAuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Service
public class WeChatAuthServiceImpl implements WeChatAuthService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatAuthServiceImpl.class);

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.wechat.app-id:}")
    private String appId;

    @Value("${app.wechat.app-secret:}")
    private String appSecret;

    @Value("${app.auth.wechat-mock-enabled:false}")
    private boolean mockEnabled;

    @PostConstruct
    public void init() {
        logger.info("=== WeChatAuthServiceImpl 初始化 ===");
        logger.info("mockEnabled: {}", mockEnabled);
        logger.info("appId: {}", appId == null || appId.isBlank() ? "空" : "已配置(" + appId.substring(0, Math.min(8, appId.length())) + "...)");
        logger.info("appSecret: {}", appSecret == null || appSecret.isBlank() ? "空" : "已配置(" + appSecret.substring(0, Math.min(8, appSecret.length())) + "...)");
    }

    @Override
    @SuppressWarnings("unchecked")
    public WeChatSession code2Session(String code) {
        logger.info("=== code2Session 被调用 ===");
        logger.info("code: {}", code);
        logger.info("mockEnabled: {}", mockEnabled);
        logger.info("appId: {}", appId == null || appId.isBlank() ? "空" : "已配置");
        logger.info("appSecret: {}", appSecret == null || appSecret.isBlank() ? "空" : "已配置");
        
        if (mockEnabled) {
            logger.info("使用mock模式");
            // 如果code是数字，用它来选择测试用户
            if (code.matches("\\d+")) {
                return new WeChatSession("dev_openid_" + code, null);
            }
            // 否则固定返回dev_openid_5（有余额的用户）
            return new WeChatSession("dev_openid_5", null);
        }
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            logger.error("微信登录配置缺失！appId或appSecret为空");
            throw new RuntimeException("微信登录配置缺失");
        }

        String url = UriComponentsBuilder.fromHttpUrl(CODE2SESSION_URL)
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        
        logger.info("请求微信接口URL: {}", url.replace(appSecret, "******"));

        Map<String, Object> response;
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            String responseBody = responseEntity.getBody();
            logger.info("微信接口响应状态码: {}", responseEntity.getStatusCode());
            logger.info("微信接口响应内容: {}", responseBody);
            
            response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        } catch (RestClientException e) {
            logger.error("请求微信接口失败", e);
            throw new RuntimeException("微信登录服务暂不可用: " + e.getMessage());
        } catch (Exception e) {
            logger.error("解析微信接口响应失败", e);
            throw new RuntimeException("微信登录响应解析失败: " + e.getMessage());
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
