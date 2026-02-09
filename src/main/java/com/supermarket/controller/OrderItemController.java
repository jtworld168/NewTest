package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.OrderItem;
import com.supermarket.service.OrderItemService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单商品明细管理", description = "订单商品明细增删改查接口")
@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Operation(summary = "根据ID查询订单商品明细")
    @GetMapping("/get/{id}")
    public Result<OrderItem> getOrderItemById(@Parameter(description = "明细ID") @PathVariable Long id) {
        OrderItem item = orderItemService.getOrderItemById(id);
        return item != null ? Result.success(item) : Result.error("订单商品明细不存在");
    }

    @Operation(summary = "查询所有订单商品明细")
    @GetMapping("/list")
    public Result<List<OrderItem>> getAllOrderItems() {
        return Result.success(orderItemService.list());
    }

    @Operation(summary = "根据订单ID查询商品明细")
    @GetMapping("/getByOrderId/{orderId}")
    public Result<List<OrderItem>> getOrderItemsByOrderId(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return Result.success(orderItemService.getOrderItemsByOrderId(orderId));
    }

    @Operation(summary = "根据商品ID查询订单明细")
    @GetMapping("/getByProductId/{productId}")
    public Result<List<OrderItem>> getOrderItemsByProductId(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(orderItemService.getOrderItemsByProductId(productId));
    }

    @Operation(summary = "分页查询订单商品明细列表")
    @GetMapping("/listPage")
    public Result<IPage<OrderItem>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderItemService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加订单商品明细")
    @PostMapping("/add")
    public Result<Void> addOrderItem(@RequestBody OrderItem orderItem) {
        if (orderItem.getOrderId() == null) {
            return Result.error("订单ID不能为空");
        }
        if (orderItem.getProductId() == null) {
            return Result.error("商品ID不能为空");
        }
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            return Result.error("购买数量必须大于0");
        }
        return orderItemService.addOrderItem(orderItem) ? Result.success() : Result.error("添加订单商品明细失败");
    }

    @Operation(summary = "更新订单商品明细")
    @PutMapping("/update")
    public Result<Void> updateOrderItem(@RequestBody OrderItem orderItem) {
        if (orderItem.getId() == null) {
            return Result.error("明细ID不能为空");
        }
        if (orderItemService.getOrderItemById(orderItem.getId()) == null) {
            return Result.error("订单商品明细不存在");
        }
        return orderItemService.updateOrderItem(orderItem) ? Result.success() : Result.error("更新订单商品明细失败");
    }

    @Operation(summary = "删除订单商品明细")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteOrderItem(@Parameter(description = "明细ID") @PathVariable Long id) {
        if (orderItemService.getOrderItemById(id) == null) {
            return Result.error("订单商品明细不存在");
        }
        return orderItemService.deleteOrderItem(id) ? Result.success() : Result.error("删除订单商品明细失败");
    }

    @Operation(summary = "批量删除订单商品明细")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchOrderItems(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("ID列表不能为空");
        }
        List<Long> validIds = ids.stream().filter(id -> id != null).toList();
        if (validIds.isEmpty()) {
            return Result.error("ID列表不能为空");
        }
        return orderItemService.deleteBatchOrderItems(validIds) ? Result.success() : Result.error("批量删除订单商品明细失败");
    }
}
