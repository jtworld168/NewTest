package com.supermarket.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.enums.OrderStatus;
import com.supermarket.service.NotificationService;
import com.supermarket.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    @Override
    public void notifyOrderStatusChange(Long userId, Long orderId, OrderStatus newStatus) {
        String statusText = switch (newStatus) {
            case PENDING -> "待支付";
            case PAID -> "已支付";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
        };

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "ORDER_STATUS");
        message.put("orderId", orderId);
        message.put("status", newStatus.name());
        message.put("title", "订单状态更新");
        message.put("content", "您的订单 #" + orderId + " 状态已更新为：" + statusText);
        message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            String json = objectMapper.writeValueAsString(message);
            webSocketHandler.sendToUser(userId, json);
        } catch (Exception e) {
            log.error("发送订单状态通知失败: userId={}, orderId={}", userId, orderId, e);
        }
    }

    @Override
    public void notifyUser(Long userId, String title, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "SYSTEM");
        message.put("title", title);
        message.put("content", content);
        message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            String json = objectMapper.writeValueAsString(message);
            webSocketHandler.sendToUser(userId, json);
        } catch (Exception e) {
            log.error("发送用户通知失败: userId={}", userId, e);
        }
    }

    @Override
    public void broadcastMessage(String title, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "BROADCAST");
        message.put("title", title);
        message.put("content", content);
        message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            String json = objectMapper.writeValueAsString(message);
            webSocketHandler.broadcast(json);
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }
}
