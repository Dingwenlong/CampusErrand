package com.campus.errand.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    // 存储用户ID与Session的映射关系
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            System.out.println("WebSocket连接成功 - 用户ID: " + userId);

            // 发送连接成功消息
            sendMessageToUser(userId, "{\"type\":\"connected\",\"message\":\"连接成功\"}");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserIdFromSession(session);
        String payload = message.getPayload();
        System.out.println("收到消息 - 用户ID: " + userId + ", 内容: " + payload);

        // 处理心跳消息
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            System.out.println("WebSocket连接关闭 - 用户ID: " + userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            System.out.println("WebSocket传输错误 - 用户ID: " + userId + ", 错误: " + exception.getMessage());
        }
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.out.println("发送消息失败 - 用户ID: " + userId + ", 错误: " + e.getMessage());
            }
        }
    }

    /**
     * 发送消息给多个用户
     */
    public static void sendMessageToUsers(java.util.List<Long> userIds, String message) {
        for (Long userId : userIds) {
            sendMessageToUser(userId, message);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public static void broadcastMessage(String message) {
        for (WebSocketSession session : userSessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.out.println("广播消息失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineCount() {
        return userSessions.size();
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? (Long) userId : null;
    }
}
