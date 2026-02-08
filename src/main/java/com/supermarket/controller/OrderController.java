package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Order;
import com.supermarket.enums.OrderStatus;
import com.supermarket.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "订单管理", description = "订单增删改查接口")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "根据ID查询订单")
    @GetMapping("/{id}")
    public Result<Order> getOrderById(@Parameter(description = "订单ID") @PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return order != null ? Result.success(order) : Result.error("订单不存在");
    }

    @Operation(summary = "查询所有订单")
    @GetMapping
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.list());
    }

    @Operation(summary = "根据用户ID查询订单")
    @GetMapping("/user/{userId}")
    public Result<List<Order>> getOrdersByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(orderService.getOrdersByUserId(userId));
    }

    @Operation(summary = "根据状态查询订单")
    @GetMapping("/status/{status}")
    public Result<List<Order>> getOrdersByStatus(@Parameter(description = "订单状态：PENDING/PAID/COMPLETED/CANCELLED") @PathVariable OrderStatus status) {
        return Result.success(orderService.getOrdersByStatus(status));
    }

    @Operation(summary = "根据优惠券ID查询订单")
    @GetMapping("/coupon/{couponId}")
    public Result<List<Order>> getOrdersByCouponId(@Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        return Result.success(orderService.getOrdersByCouponId(couponId));
    }

    @Operation(summary = "添加订单")
    @PostMapping
    public Result<Void> addOrder(@RequestBody Order order) {
        if (order.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        if (order.getTotalAmount() == null) {
            return Result.error("订单金额不能为空");
        }
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("订单金额不能为负数");
        }
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        return orderService.addOrder(order) ? Result.success() : Result.error("添加订单失败");
    }

    @Operation(summary = "更新订单")
    @PutMapping
    public Result<Void> updateOrder(@RequestBody Order order) {
        if (order.getId() == null) {
            return Result.error("订单ID不能为空");
        }
        if (orderService.getOrderById(order.getId()) == null) {
            return Result.error("订单不存在");
        }
        return orderService.updateOrder(order) ? Result.success() : Result.error("更新订单失败");
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@Parameter(description = "订单ID") @PathVariable Long id) {
        if (orderService.getOrderById(id) == null) {
            return Result.error("订单不存在");
        }
        return orderService.deleteOrder(id) ? Result.success() : Result.error("删除订单失败");
    }
}
