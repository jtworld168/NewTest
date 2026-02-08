package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.mapper.OrderItemMapper;
import com.supermarket.service.OrderItemService;
import com.supermarket.service.OrderService;
import com.supermarket.service.ProductService;
import com.supermarket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;

    @Override
    public OrderItem getOrderItemById(Long id) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getId, id);
        return getOne(wrapper);
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return list(wrapper);
    }

    @Override
    public boolean addOrderItem(Long orderId, Long productId, Integer quantity) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return false;
        }

        Product product = productService.getProductById(productId);
        if (product == null) {
            return false;
        }

        User user = userService.getUserById(order.getUserId());
        if (user == null) {
            return false;
        }

        BigDecimal unitPrice = product.getPrice();

        // Apply employee discount if user is a hotel employee and product has a discount rate
        if (Boolean.TRUE.equals(user.getIsHotelEmployee()) && product.getEmployeeDiscountRate() != null) {
            unitPrice = unitPrice.multiply(product.getEmployeeDiscountRate()).setScale(2, RoundingMode.HALF_UP);
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(productId);
        orderItem.setQuantity(quantity);
        orderItem.setPriceAtPurchase(unitPrice);

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
    public List<OrderItem> getOrderItemsByProductId(Long productId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getProductId, productId);
        return list(wrapper);
    }
}
