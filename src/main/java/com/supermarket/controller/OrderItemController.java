package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.OrderItem;
import com.supermarket.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单项管理", description = "订单项增删改查接口（含员工折扣计算）")
@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Operation(summary = "根据ID查询订单项")
    @GetMapping("/get/{id}")
    public Result<OrderItem> getOrderItemById(@Parameter(description = "订单项ID") @PathVariable Long id) {
        OrderItem item = orderItemService.getOrderItemById(id);
        return item != null ? Result.success(item) : Result.error("订单项不存在");
    }

    @Operation(summary = "查询所有订单项")
    @GetMapping("/list")
    public Result<List<OrderItem>> getAllOrderItems() {
        return Result.success(orderItemService.list());
    }

    @Operation(summary = "根据订单ID查询订单项列表")
    @GetMapping("/getByOrderId/{orderId}")
    public Result<List<OrderItem>> getOrderItemsByOrderId(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return Result.success(orderItemService.getOrderItemsByOrderId(orderId));
    }

    @Operation(summary = "根据商品ID查询订单项列表")
    @GetMapping("/getByProductId/{productId}")
    public Result<List<OrderItem>> getOrderItemsByProductId(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(orderItemService.getOrderItemsByProductId(productId));
    }

    @Operation(summary = "添加订单项（自动计算员工折扣价）")
    @PostMapping("/add")
    public Result<Void> addOrderItem(
            @Parameter(description = "订单ID") @RequestParam Long orderId,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "购买数量") @RequestParam Integer quantity) {
        if (orderId == null) {
            return Result.error("订单ID不能为空");
        }
        if (productId == null) {
            return Result.error("商品ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            return Result.error("购买数量必须大于0");
        }
        return orderItemService.addOrderItem(orderId, productId, quantity)
                ? Result.success() : Result.error("添加订单项失败");
    }

    @Operation(summary = "更新订单项")
    @PutMapping("/update")
    public Result<Void> updateOrderItem(@RequestBody OrderItem orderItem) {
        if (orderItem.getId() == null) {
            return Result.error("订单项ID不能为空");
        }
        if (orderItemService.getOrderItemById(orderItem.getId()) == null) {
            return Result.error("订单项不存在");
        }
        return orderItemService.updateOrderItem(orderItem) ? Result.success() : Result.error("更新订单项失败");
    }

    @Operation(summary = "删除订单项")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteOrderItem(@Parameter(description = "订单项ID") @PathVariable Long id) {
        if (orderItemService.getOrderItemById(id) == null) {
            return Result.error("订单项不存在");
        }
        return orderItemService.deleteOrderItem(id) ? Result.success() : Result.error("删除订单项失败");
    }
}
