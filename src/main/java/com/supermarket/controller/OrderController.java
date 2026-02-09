package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Order;
import com.supermarket.enums.OrderStatus;
import com.supermarket.service.OrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理", description = "订单增删改查接口（含员工折扣计算）")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "根据ID查询订单")
    @GetMapping("/get/{id}")
    public Result<Order> getOrderById(@Parameter(description = "订单ID") @PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return order != null ? Result.success(order) : Result.error("订单不存在");
    }

    @Operation(summary = "查询所有订单")
    @GetMapping("/list")
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.listAll());
    }

    @Operation(summary = "根据用户ID查询订单")
    @GetMapping("/getByUserId/{userId}")
    public Result<List<Order>> getOrdersByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(orderService.getOrdersByUserId(userId));
    }

    @Operation(summary = "根据状态查询订单")
    @GetMapping("/getByStatus/{status}")
    public Result<List<Order>> getOrdersByStatus(@Parameter(description = "订单状态：PENDING/PAID/COMPLETED/CANCELLED") @PathVariable OrderStatus status) {
        return Result.success(orderService.getOrdersByStatus(status));
    }

    @Operation(summary = "根据商品ID查询订单")
    @GetMapping("/getByProductId/{productId}")
    public Result<List<Order>> getOrdersByProductId(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(orderService.getOrdersByProductId(productId));
    }

    @Operation(summary = "根据用户优惠券ID查询订单")
    @GetMapping("/getByUserCouponId/{userCouponId}")
    public Result<List<Order>> getOrdersByUserCouponId(@Parameter(description = "用户优惠券ID") @PathVariable Long userCouponId) {
        return Result.success(orderService.getOrdersByUserCouponId(userCouponId));
    }

    @Operation(summary = "分页查询订单列表")
    @GetMapping("/listPage")
    public Result<IPage<Order>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加订单（自动计算员工折扣价和总金额）")
    @PostMapping("/add")
    public Result<Void> addOrder(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "购买数量") @RequestParam Integer quantity,
            @Parameter(description = "用户优惠券ID（可选）") @RequestParam(required = false) Long userCouponId) {
        if (userId == null) {
            return Result.badRequest("用户ID不能为空");
        }
        if (productId == null) {
            return Result.badRequest("商品ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            return Result.badRequest("购买数量必须大于0");
        }
        return orderService.addOrder(userId, productId, quantity, userCouponId)
                ? Result.success() : Result.error("添加订单失败");
    }

    @Operation(summary = "更新订单")
    @PutMapping("/update")
    public Result<Void> updateOrder(@RequestBody Order order) {
        if (order.getId() == null) {
            return Result.badRequest("订单ID不能为空");
        }
        if (orderService.getOrderById(order.getId()) == null) {
            return Result.badRequest("订单不存在");
        }
        if (order.getQuantity() != null && order.getQuantity() <= 0) {
            return Result.badRequest("购买数量必须大于0");
        }
        return orderService.updateOrder(order) ? Result.success() : Result.error("更新订单失败");
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteOrder(@Parameter(description = "订单ID") @PathVariable Long id) {
        if (orderService.getOrderById(id) == null) {
            return Result.badRequest("订单不存在");
        }
        return orderService.deleteOrder(id) ? Result.success() : Result.error("删除订单失败");
    }

    @Operation(summary = "批量删除订单")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchOrders(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return orderService.deleteBatchOrders(ids) ? Result.success() : Result.error("批量删除订单失败");
    }
}
