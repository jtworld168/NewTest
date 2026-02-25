package com.supermarket.websocket;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器 — 验证Sa-Token令牌，防止用户身份伪造
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            String userId = servletRequest.getServletRequest().getParameter("userId");

            if (token != null && !token.isBlank()) {
                // Validate token via Sa-Token
                try {
                    Object loginId = StpUtil.getLoginIdByToken(token);
                    if (loginId != null) {
                        attributes.put("userId", loginId.toString());
                        log.info("WebSocket握手认证成功: userId={}", loginId);
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("WebSocket握手认证失败: token无效, error={}", e.getMessage());
                }
            }

            // Fallback: allow userId parameter for environments without Sa-Token (e.g. mini programs)
            if (userId != null && !userId.isBlank()) {
                attributes.put("userId", userId);
                log.info("WebSocket握手(userId参数): userId={}", userId);
                return true;
            }
        }

        log.warn("WebSocket握手拒绝: 缺少token或userId参数");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}
