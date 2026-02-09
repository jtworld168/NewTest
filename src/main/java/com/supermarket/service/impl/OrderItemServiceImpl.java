package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.OrderItem;
import com.supermarket.mapper.OrderItemMapper;
import com.supermarket.service.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

    @Override
    public OrderItem getOrderItemById(Long id) {
        return getById(id);
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return list(wrapper);
    }

    @Override
    public List<OrderItem> getOrderItemsByProductId(Long productId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getProductId, productId);
        return list(wrapper);
    }

    @Override
    public boolean addOrderItem(OrderItem orderItem) {
        return save(orderItem);
    }

    @Override
    public boolean updateOrderItem(OrderItem orderItem) {
        return updateById(orderItem);
    }

    @Override
    public boolean deleteOrderItem(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchOrderItems(List<Long> ids) {
        return removeByIds(ids);
    }
}
