package com.supermarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.OrderItem;

import java.util.List;

public interface OrderItemService extends IService<OrderItem> {

    OrderItem getOrderItemById(Long id);

    List<OrderItem> getOrderItemsByOrderId(Long orderId);

    List<OrderItem> getOrderItemsByProductId(Long productId);

    boolean addOrderItem(OrderItem orderItem);

    boolean updateOrderItem(OrderItem orderItem);

    boolean deleteOrderItem(Long id);

    boolean deleteBatchOrderItems(List<Long> ids);
}
