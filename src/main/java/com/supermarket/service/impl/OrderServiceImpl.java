package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.enums.OrderStatus;
import com.supermarket.mapper.OrderMapper;
import com.supermarket.service.OrderItemService;
import com.supermarket.service.OrderService;
import com.supermarket.service.ProductService;
import com.supermarket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final UserService userService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    @Override
    public Order getOrderById(Long id) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getId, id);
        return getOne(wrapper);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
               .orderByDesc(Order::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, status)
               .orderByDesc(Order::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Order> getOrdersByProductId(Long productId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getProductId, productId)
               .orderByDesc(Order::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Order> getOrdersByUserCouponId(Long userCouponId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserCouponId, userCouponId)
               .orderByDesc(Order::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Order> getOrdersByStoreId(Long storeId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStoreId, storeId)
               .orderByDesc(Order::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<Order> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public IPage<Order> listPageByStoreId(Long storeId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStoreId, storeId)
               .orderByDesc(Order::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Order addOrder(Long userId, Long productId, Integer quantity, Long userCouponId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return null;
        }

        Product product = productService.getProductById(productId);
        if (product == null) {
            return null;
        }

        BigDecimal unitPrice = product.getPrice();

        // Apply employee discount if user is a hotel employee and product has a discount rate
        if (Boolean.TRUE.equals(user.getIsHotelEmployee()) && product.getEmployeeDiscountRate() != null) {
            unitPrice = unitPrice.multiply(product.getEmployeeDiscountRate()).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));

        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setPriceAtPurchase(unitPrice);
        order.setTotalAmount(totalAmount);
        order.setUserCouponId(userCouponId);
        order.setStatus(OrderStatus.PENDING);

        save(order);
        return order;
    }

    @Override
    @Transactional
    public Order addMultiItemOrder(Long userId, Long storeId, List<Map<String, Object>> items, Long userCouponId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return null;
        }

        // Validate all products upfront
        for (Map<String, Object> item : items) {
            Long productId = ((Number) item.get("productId")).longValue();
            Product product = productService.getProductById(productId);
            if (product == null) {
                return null;
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Create order header first
        Order order = new Order();
        order.setUserId(userId);
        order.setStoreId(storeId);
        order.setUserCouponId(userCouponId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        save(order);

        BigDecimal firstUnitPrice = null;
        Long firstProductId = null;
        int firstQuantity = 0;

        for (Map<String, Object> item : items) {
            Long productId = ((Number) item.get("productId")).longValue();
            int quantity = ((Number) item.get("quantity")).intValue();

            Product product = productService.getProductById(productId);

            BigDecimal unitPrice = product.getPrice();
            if (Boolean.TRUE.equals(user.getIsHotelEmployee()) && product.getEmployeeDiscountRate() != null) {
                unitPrice = unitPrice.multiply(product.getEmployeeDiscountRate()).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
            totalAmount = totalAmount.add(subtotal);

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(productId);
            orderItem.setQuantity(quantity);
            orderItem.setPriceAtPurchase(unitPrice);
            orderItem.setSubtotal(subtotal);
            orderItemService.addOrderItem(orderItem);

            if (firstProductId == null) {
                firstProductId = productId;
                firstUnitPrice = unitPrice;
                firstQuantity = quantity;
            }
        }

        // Update order with calculated total and first product info for backward compat
        order.setProductId(firstProductId);
        order.setQuantity(firstQuantity);
        order.setPriceAtPurchase(firstUnitPrice);
        order.setTotalAmount(totalAmount);
        updateById(order);

        return order;
    }

    @Override
    public boolean updateOrder(Order order) {
        return updateById(order);
    }

    @Override
    public boolean deleteOrder(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchOrders(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<Order> listAll() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);
        return list(wrapper);
    }
}
