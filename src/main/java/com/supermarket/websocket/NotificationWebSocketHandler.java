package com.supermarket.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket通知处理器
 * 用于向客户端推送消息（订单状态变更、系统通知等）
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // userId -> WebSocketSession
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("WebSocket连接建立: userId={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("WebSocket连接关闭: userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client can send heartbeat or other messages
        log.debug("收到WebSocket消息: {}", message.getPayload());
    }

    /**
     * 向指定用户推送消息
     */
    public void sendToUser(Long userId, String message) {
        WebSocketSession session = SESSIONS.get(String.valueOf(userId));
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("推送消息给用户{}: {}", userId, message);
            } catch (IOException e) {
                log.error("推送消息失败: userId={}, error={}", userId, e.getMessage());
            }
        }
    }

    /**
     * 向所有在线用户广播消息
     */
    public void broadcast(String message) {
        SESSIONS.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("广播消息失败: {}", e.getMessage());
                }
            }
        });
    }

    /**
     * 获取当前在线用户数
     */
    public int getOnlineCount() {
        return (int) SESSIONS.values().stream().filter(WebSocketSession::isOpen).count();
    }

    private String getUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null && query.contains("userId=")) {
            for (String param : query.split("&")) {
                if (param.startsWith("userId=")) {
                    return param.substring(7);
                }
            }
        }
        return null;
    }
}
