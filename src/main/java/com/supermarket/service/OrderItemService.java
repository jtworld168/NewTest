package com.supermarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.OrderItem;

import java.util.List;

public interface OrderItemService extends IService<OrderItem> {

    OrderItem getOrderItemById(Long id);

    List<OrderItem> getOrderItemsByOrderId(Long orderId);

    boolean addOrderItem(Long orderId, Long productId, Integer quantity);

    boolean updateOrderItem(OrderItem orderItem);

    boolean deleteOrderItem(Long id);
}
