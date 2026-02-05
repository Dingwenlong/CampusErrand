package com.campus.errand.websocket;

import com.campus.errand.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Autowired
    public WebSocketInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String token = servletRequest.getParameter("token");

            if (token != null && !token.isEmpty()) {
                try {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    if (userId != null) {
                        attributes.put("userId", userId);
                        return true;
                    }
                } catch (Exception e) {
                    System.out.println("WebSocket Token验证失败: " + e.getMessage());
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
