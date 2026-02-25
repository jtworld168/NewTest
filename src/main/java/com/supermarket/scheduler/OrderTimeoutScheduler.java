package com.supermarket.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.Order;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.enums.OrderStatus;
import com.supermarket.service.OrderService;
import com.supermarket.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：自动取消超过30分钟未支付的订单
 * 作为 Redis Key 过期监听的兜底方案
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private static final int TIMEOUT_MINUTES = 30;

    private final OrderService orderService;
    private final UserCouponService userCouponService;

    /**
     * 每5分钟执行一次，检查并取消超时未支付的订单
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void cancelTimeoutOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.PENDING)
               .lt(Order::getCreateTime, cutoff);
        List<Order> timeoutOrders = orderService.list(wrapper);

        if (timeoutOrders.isEmpty()) {
            return;
        }

        int count = 0;
        for (Order order : timeoutOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            orderService.updateById(order);

            // Unlock coupon if any
            if (order.getUserCouponId() != null) {
                UserCoupon uc = userCouponService.getUserCouponById(order.getUserCouponId());
                if (uc != null && uc.getStatus() == CouponStatus.LOCKED) {
                    uc.setStatus(CouponStatus.AVAILABLE);
                    userCouponService.updateUserCoupon(uc);
                }
            }
            count++;
        }

        log.info("定时任务取消超时订单: {} 笔", count);
    }
}
