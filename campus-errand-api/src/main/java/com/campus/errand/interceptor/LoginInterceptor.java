package com.campus.errand.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.campus.errand.common.Result;
import com.campus.errand.common.ResultCode;
import com.campus.errand.util.JwtUtil;
import com.campus.errand.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
        if (token == null || !token.startsWith("Bearer ")) {
            writeErrorResponse(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        token = token.substring(7);
        
        try {
            Long userId = JwtUtil.getUserId(token);
            String openid = JwtUtil.getOpenid(token);
            
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
