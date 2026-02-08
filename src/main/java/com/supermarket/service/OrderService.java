package com.supermarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Order;
import com.supermarket.enums.OrderStatus;

import java.util.List;

public interface OrderService extends IService<Order> {

    Order getOrderById(Long id);

    List<Order> getOrdersByUserId(Long userId);

    List<Order> getOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByProductId(Long productId);

    List<Order> getOrdersByCouponId(Long couponId);

    boolean addOrder(Long userId, Long productId, Integer quantity, Long couponId);

    boolean updateOrder(Order order);

    boolean deleteOrder(Long id);
}
