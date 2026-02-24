package com.supermarket.listener;

import com.supermarket.entity.Order;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.enums.OrderStatus;
import com.supermarket.service.OrderService;
import com.supermarket.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis Key 过期监听器
 * 监听 order:expire:{orderId} 类型的 key 过期事件
 * 当 key 过期时，自动将对应的 PENDING 订单取消
 *
 * Redis 需要开启键空间通知: notify-keyspace-events Ex
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RedisConnectionFactory.class)
public class OrderExpireListener implements MessageListener {

    private static final String ORDER_EXPIRE_PREFIX = "order:expire:";

    private final OrderService orderService;
    private final UserCouponService userCouponService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (!expiredKey.startsWith(ORDER_EXPIRE_PREFIX)) {
            return;
        }

        String orderIdStr = expiredKey.substring(ORDER_EXPIRE_PREFIX.length());
        try {
            Long orderId = Long.parseLong(orderIdStr);
            Order order = orderService.getOrderById(orderId);

            if (order != null && order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELLED);
                orderService.updateOrder(order);
                log.info("订单超时自动取消: orderId={}", orderId);
            }
        } catch (NumberFormatException e) {
            log.warn("无效的订单过期 key: {}", expiredKey);
        }
    }
}
