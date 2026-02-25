package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Coupon;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.User;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.enums.OrderStatus;
import com.supermarket.mapper.OrderMapper;
import com.supermarket.service.CouponService;
import com.supermarket.service.NotificationService;
import com.supermarket.service.OrderItemService;
import com.supermarket.service.OrderService;
import com.supermarket.service.ProductService;
import com.supermarket.service.StoreProductService;
import com.supermarket.service.UserCouponService;
import com.supermarket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final String ORDER_EXPIRE_PREFIX = "order:expire:";
    private static final long ORDER_EXPIRE_MINUTES = 30;
    private static final BigDecimal MIN_ORDER_AMOUNT = BigDecimal.ONE;

    private final UserService userService;
    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final UserCouponService userCouponService;
    private final CouponService couponService;
    private final StoreProductService storeProductService;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

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
    @Transactional
    public Order addOrder(Long userId, Long productId, Integer quantity, Long userCouponId, Long storeId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return null;
        }

        Product product = productService.getProductById(productId);
        if (product == null) {
            return null;
        }

        // Check stock availability
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }

        // Use Redis distributed lock to prevent overselling
        String lockKey = "lock:order:product:" + productId;
        boolean locked = tryLock(lockKey, 5);
        if (!locked) {
            throw new RuntimeException("系统繁忙，请稍后再试");
        }
        try {
            // Re-check stock inside lock
            Product freshProduct = productService.getById(productId);
            if (freshProduct == null || freshProduct.getStock() == null || freshProduct.getStock() < quantity) {
                throw new RuntimeException("商品库存不足");
            }

            // Lock coupon with Redis distributed lock to prevent double-booking
            lockCoupon(userCouponId);

            BigDecimal unitPrice = product.getPrice();

            // Apply employee discount if user is a hotel employee and product has a discount rate
            if (Boolean.TRUE.equals(user.getIsHotelEmployee()) && product.getEmployeeDiscountRate() != null) {
                unitPrice = unitPrice.multiply(product.getEmployeeDiscountRate()).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));

            // Apply coupon discount if provided (employee discount and coupon cannot stack)
            totalAmount = applyCouponDiscount(totalAmount, userCouponId, user);

            Order order = new Order();
            order.setUserId(userId);
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setPriceAtPurchase(unitPrice);
            order.setTotalAmount(totalAmount);
            order.setUserCouponId(userCouponId);
            order.setStoreId(storeId);
            order.setStatus(OrderStatus.PENDING);

            // Deduct product stock
            freshProduct.setStock(freshProduct.getStock() - quantity);
            productService.updateProduct(freshProduct);

            save(order);
            setOrderExpireKey(order.getId());

            // Deduct store stock if storeId is present
            if (storeId != null) {
                storeProductService.deductStoreStock(storeId, productId, quantity);
            }

            // Push notification for new order
            if (notificationService != null) {
                notificationService.notifyOrderStatusChange(userId, order.getId(), OrderStatus.PENDING);
            }

            return order;
        } finally {
            releaseLock(lockKey);
        }
    }

    @Override
    @Transactional
    public Order addMultiItemOrder(Long userId, Long storeId, List<Map<String, Object>> items, Long userCouponId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return null;
        }

        // Collect all product IDs for locking
        List<Long> productIds = new java.util.ArrayList<>();
        for (Map<String, Object> item : items) {
            Long productId = ((Number) item.get("productId")).longValue();
            productIds.add(productId);
        }

        // Acquire distributed locks for all products (sorted to prevent deadlock)
        java.util.Collections.sort(productIds);
        List<String> lockKeys = new java.util.ArrayList<>();
        for (Long pid : productIds) {
            String lockKey = "lock:order:product:" + pid;
            boolean locked = tryLock(lockKey, 5);
            if (!locked) {
                // Release already-acquired locks
                for (String key : lockKeys) releaseLock(key);
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            lockKeys.add(lockKey);
        }

        try {
            // Validate all products and check stock
            for (Map<String, Object> item : items) {
                Long productId = ((Number) item.get("productId")).longValue();
                int quantity = ((Number) item.get("quantity")).intValue();
                Product product = productService.getById(productId);
                if (product == null) {
                    throw new RuntimeException("商品不存在: " + productId);
                }
                if (product.getStock() == null || product.getStock() < quantity) {
                    throw new RuntimeException("商品库存不足: " + product.getName());
                }
            }

            // Pre-calculate totalAmount and item details BEFORE saving order
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal firstUnitPrice = null;
            Long firstProductId = null;
            int firstQuantity = 0;

            // Structure to hold calculated item details
            java.util.List<Object[]> calculatedItems = new java.util.ArrayList<>();

            for (Map<String, Object> item : items) {
                Long productId = ((Number) item.get("productId")).longValue();
                int quantity = ((Number) item.get("quantity")).intValue();

                Product product = productService.getById(productId);

                BigDecimal unitPrice = product.getPrice();
                if (Boolean.TRUE.equals(user.getIsHotelEmployee()) && product.getEmployeeDiscountRate() != null) {
                    unitPrice = unitPrice.multiply(product.getEmployeeDiscountRate()).setScale(2, RoundingMode.HALF_UP);
                }

                BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
                totalAmount = totalAmount.add(subtotal);

                calculatedItems.add(new Object[]{productId, quantity, unitPrice, subtotal});

                if (firstProductId == null) {
                    firstProductId = productId;
                    firstUnitPrice = unitPrice;
                    firstQuantity = quantity;
                }
            }

            // Apply coupon discount if provided (employee discount and coupon cannot stack)
            totalAmount = applyCouponDiscount(totalAmount, userCouponId, user);

            // Lock coupon before saving order to prevent double-booking
            lockCoupon(userCouponId);

            // Create order with correct totalAmount (satisfies CHECK constraint)
            Order order = new Order();
            order.setUserId(userId);
            order.setStoreId(storeId);
            order.setUserCouponId(userCouponId);
            order.setStatus(OrderStatus.PENDING);
            order.setProductId(firstProductId);
            order.setQuantity(firstQuantity);
            order.setPriceAtPurchase(firstUnitPrice);
            order.setTotalAmount(totalAmount);
            save(order);

            // Create order items and deduct stock
            for (Object[] calc : calculatedItems) {
                Long productId = (Long) calc[0];
                int quantity = (Integer) calc[1];

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(productId);
                orderItem.setQuantity(quantity);
                orderItem.setPriceAtPurchase((BigDecimal) calc[2]);
                orderItem.setSubtotal((BigDecimal) calc[3]);
                orderItemService.addOrderItem(orderItem);

                // Deduct product stock atomically
                Product product = productService.getById(productId);
                product.setStock(product.getStock() - quantity);
                productService.updateProduct(product);

                // Deduct store stock for each item
                if (storeId != null) {
                    storeProductService.deductStoreStock(storeId, productId, quantity);
                }
            }

            setOrderExpireKey(order.getId());

            // Push notification for new multi-item order
            if (notificationService != null) {
                notificationService.notifyOrderStatusChange(userId, order.getId(), OrderStatus.PENDING);
            }

            return order;
        } finally {
            for (String key : lockKeys) releaseLock(key);
        }
    }

    @Override
    public boolean updateOrder(Order order) {
        // If status changes to PAID/COMPLETED/CANCELLED, remove the expire key
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.CANCELLED) {
            removeOrderExpireKey(order.getId());

            // Handle coupon status based on order status change
            Order existingOrder = getOrderById(order.getId());
            if (existingOrder != null && existingOrder.getUserCouponId() != null) {
                if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.COMPLETED) {
                    markCouponUsed(existingOrder.getUserCouponId());
                } else if (order.getStatus() == OrderStatus.CANCELLED) {
                    unlockCoupon(existingOrder.getUserCouponId());
                }
            }

            // Push notification for order status change
            if (notificationService != null && existingOrder != null) {
                notificationService.notifyOrderStatusChange(existingOrder.getUserId(), order.getId(), order.getStatus());
            }
        }

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

    /**
     * 应用优惠券折扣（员工折扣和优惠券不可叠加，员工不应用优惠券）
     */
    private BigDecimal applyCouponDiscount(BigDecimal totalAmount, Long userCouponId, User user) {
        if (userCouponId == null) {
            return totalAmount;
        }
        // Employee discount and coupon cannot stack
        if (Boolean.TRUE.equals(user.getIsHotelEmployee())) {
            return totalAmount;
        }
        UserCoupon userCoupon = userCouponService.getUserCouponById(userCouponId);
        if (userCoupon == null || userCoupon.getCouponId() == null
                || userCoupon.getStatus() != CouponStatus.AVAILABLE) {
            return totalAmount;
        }
        Coupon coupon = couponService.getCouponById(userCoupon.getCouponId());
        if (coupon == null || coupon.getDiscount() == null) {
            return totalAmount;
        }
        // Check minimum spend requirement
        if (coupon.getMinAmount() != null && totalAmount.compareTo(coupon.getMinAmount()) < 0) {
            return totalAmount;
        }
        BigDecimal discounted = totalAmount.subtract(coupon.getDiscount());
        return discounted.compareTo(MIN_ORDER_AMOUNT) >= 0 ? discounted : MIN_ORDER_AMOUNT;
    }

    /**
     * 锁定优惠券（下单时 AVAILABLE → LOCKED）
     * 使用 Redis 分布式锁防止并发双重预订
     */
    private void lockCoupon(Long userCouponId) {
        if (userCouponId == null) return;
        String couponLockKey = "lock:coupon:" + userCouponId;
        boolean locked = tryLock(couponLockKey, 3);
        if (!locked) {
            throw new RuntimeException("优惠券正在被使用，请稍后再试");
        }
        try {
            UserCoupon uc = userCouponService.getUserCouponById(userCouponId);
            if (uc != null && uc.getStatus() == CouponStatus.AVAILABLE) {
                uc.setStatus(CouponStatus.LOCKED);
                userCouponService.updateUserCoupon(uc);
            }
        } finally {
            releaseLock(couponLockKey);
        }
    }

    /**
     * 解锁优惠券（取消订单时 LOCKED → AVAILABLE）
     */
    private void unlockCoupon(Long userCouponId) {
        if (userCouponId == null) return;
        UserCoupon uc = userCouponService.getUserCouponById(userCouponId);
        if (uc != null && uc.getStatus() == CouponStatus.LOCKED) {
            uc.setStatus(CouponStatus.AVAILABLE);
            userCouponService.updateUserCoupon(uc);
        }
    }

    /**
     * 标记优惠券已使用（支付成功时 LOCKED → USED）
     */
    private void markCouponUsed(Long userCouponId) {
        if (userCouponId == null) return;
        UserCoupon uc = userCouponService.getUserCouponById(userCouponId);
        if (uc != null && uc.getStatus() == CouponStatus.LOCKED) {
            uc.setStatus(CouponStatus.USED);
            uc.setUseTime(java.time.LocalDateTime.now());
            userCouponService.updateUserCoupon(uc);
        }
    }

    /**
     * 设置订单过期 Redis Key（30分钟后过期 → 自动取消订单）
     */
    private void setOrderExpireKey(Long orderId) {
        if (redisTemplate != null && orderId != null) {
            String key = ORDER_EXPIRE_PREFIX + orderId;
            redisTemplate.opsForValue().set(key, orderId.toString(), ORDER_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 移除订单过期 Redis Key（订单已支付/已完成时调用）
     */
    private void removeOrderExpireKey(Long orderId) {
        if (redisTemplate != null && orderId != null) {
            String key = ORDER_EXPIRE_PREFIX + orderId;
            redisTemplate.delete(key);
        }
    }

    /**
     * Redis 分布式锁 — 获取锁
     * @param lockKey 锁的 key
     * @param expireSeconds 锁的过期时间（秒），防止死锁
     * @return 是否获取成功
     */
    private boolean tryLock(String lockKey, int expireSeconds) {
        if (redisTemplate == null) return true; // Graceful degradation without Redis
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", expireSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Redis 分布式锁 — 释放锁
     */
    private void releaseLock(String lockKey) {
        if (redisTemplate != null) {
            redisTemplate.delete(lockKey);
        }
    }
}
