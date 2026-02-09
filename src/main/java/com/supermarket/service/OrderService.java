package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Order;
import com.supermarket.enums.OrderStatus;

import java.util.List;

public interface OrderService extends IService<Order> {

    Order getOrderById(Long id);

    List<Order> getOrdersByUserId(Long userId);

    List<Order> getOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByProductId(Long productId);

    List<Order> getOrdersByUserCouponId(Long userCouponId);

    IPage<Order> listPage(int pageNum, int pageSize);

    boolean addOrder(Long userId, Long productId, Integer quantity, Long userCouponId);

    boolean updateOrder(Order order);

    boolean deleteOrder(Long id);

    boolean deleteBatchOrders(List<Long> ids);

    List<Order> listAll();
}
