package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Order;
import com.supermarket.enums.OrderStatus;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    Order getOrderById(Long id);

    List<Order> getOrdersByUserId(Long userId);

    List<Order> getOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByProductId(Long productId);

    List<Order> getOrdersByUserCouponId(Long userCouponId);

    List<Order> getOrdersByStoreId(Long storeId);

    IPage<Order> listPage(int pageNum, int pageSize);

    IPage<Order> listPageByStoreId(Long storeId, int pageNum, int pageSize);

    Order addOrder(Long userId, Long productId, Integer quantity, Long userCouponId);

    Order addMultiItemOrder(Long userId, Long storeId, List<Map<String, Object>> items, Long userCouponId);

    boolean updateOrder(Order order);

    boolean deleteOrder(Long id);

    boolean deleteBatchOrders(List<Long> ids);

    List<Order> listAll();
}
