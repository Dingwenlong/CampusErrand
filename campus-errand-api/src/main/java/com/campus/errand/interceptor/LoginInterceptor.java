package com.campus.errand.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.campus.errand.common.Result;
import com.campus.errand.common.ResultCode;
import com.campus.errand.util.JwtUtil;
import com.campus.errand.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);

    private final JwtUtil jwtUtil;

    @Autowired
    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            writeErrorResponse(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        token = token.substring(7);

        // 处理 mock token（用户二登录）
        if (token.startsWith("mock_user2_token_")) {
            log.info("Mock token detected, user2 login");
            UserContext.setUserId(2L);
            UserContext.setOpenid("mock_user2_openid");
            return true;
        }

        try {
            Long userId = jwtUtil.getUserId(token);
            String openid = jwtUtil.getOpenid(token);

            if (userId == null) {
                writeErrorResponse(response, ResultCode.TOKEN_INVALID);
                return false;
            }

            UserContext.setUserId(userId);
            UserContext.setOpenid(openid);

            return true;
        } catch (JWTVerificationException e) {
            log.error("Token验证失败: {}", e.getMessage());
            writeErrorResponse(response, ResultCode.TOKEN_EXPIRED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    private void writeErrorResponse(HttpServletResponse response, ResultCode resultCode) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(Result.error(resultCode)));
    }
}
