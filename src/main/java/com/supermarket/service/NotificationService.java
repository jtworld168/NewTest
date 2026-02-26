package com.supermarket.service;

import com.supermarket.enums.OrderStatus;

/**
 * 消息推送服务接口
 */
public interface NotificationService {

    /**
     * 推送订单状态变更通知
     */
    void notifyOrderStatusChange(Long userId, Long orderId, OrderStatus newStatus);

    /**
     * 推送系统通知给指定用户
     */
    void notifyUser(Long userId, String title, String content);

    /**
     * 广播系统消息给所有在线用户
     */
    void broadcastMessage(String title, String content);
}
