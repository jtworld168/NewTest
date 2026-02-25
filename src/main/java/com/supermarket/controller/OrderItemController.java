package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.service.OrderItemService;
import com.supermarket.service.ProductService;
import com.supermarket.vo.OrderItemVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "订单商品明细管理", description = "订单商品明细增删改查接口")
@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;
    private final ProductService productService;

    private OrderItemVO toVO(OrderItem item, Map<Long, Product> productMap) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtils.copyProperties(item, vo);
        if (item.getProductId() != null) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductBarcode(product.getBarcode());
            }
        }
        return vo;
    }

    private List<OrderItemVO> toVOList(List<OrderItem> items) {
        Map<Long, Product> productMap = productService.listAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        return items.stream().map(item -> toVO(item, productMap)).collect(Collectors.toList());
    }

    @Operation(summary = "根据ID查询订单商品明细")
    @GetMapping("/get/{id}")
    public Result<OrderItem> getOrderItemById(@Parameter(description = "明细ID") @PathVariable Long id) {
        OrderItem item = orderItemService.getOrderItemById(id);
        return item != null ? Result.success(item) : Result.error("订单商品明细不存在");
    }

    @Operation(summary = "查询所有订单商品明细")
    @GetMapping("/list")
    public Result<List<OrderItemVO>> getAllOrderItems() {
        return Result.success(toVOList(orderItemService.listAll()));
    }

    @Operation(summary = "根据订单ID查询商品明细")
    @GetMapping("/getByOrderId/{orderId}")
    public Result<List<OrderItemVO>> getOrderItemsByOrderId(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        return Result.success(toVOList(orderItemService.getOrderItemsByOrderId(orderId)));
    }

    @Operation(summary = "根据商品ID查询订单明细")
    @GetMapping("/getByProductId/{productId}")
    public Result<List<OrderItemVO>> getOrderItemsByProductId(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(toVOList(orderItemService.getOrderItemsByProductId(productId)));
    }

    @Operation(summary = "分页查询订单商品明细列表")
    @GetMapping("/listPage")
    public Result<IPage<OrderItemVO>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<OrderItem> page = orderItemService.listPage(pageNum, pageSize);
        Map<Long, Product> productMap = productService.listAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        IPage<OrderItemVO> voPage = page.convert(item -> toVO(item, productMap));
        return Result.success(voPage);
    }

    @Operation(summary = "添加订单商品明细")
    @PostMapping("/add")
    public Result<Void> addOrderItem(@RequestBody OrderItem orderItem) {
        if (orderItem.getOrderId() == null) {
            return Result.badRequest("订单ID不能为空");
        }
        if (orderItem.getProductId() == null) {
            return Result.badRequest("商品ID不能为空");
        }
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            return Result.badRequest("购买数量必须大于0");
        }
        return orderItemService.addOrderItem(orderItem) ? Result.success() : Result.error("添加订单商品明细失败");
    }

    @Operation(summary = "更新订单商品明细")
    @PutMapping("/update")
    public Result<Void> updateOrderItem(@RequestBody OrderItem orderItem) {
        if (orderItem.getId() == null) {
            return Result.badRequest("明细ID不能为空");
        }
        if (orderItemService.getOrderItemById(orderItem.getId()) == null) {
            return Result.badRequest("订单商品明细不存在");
        }
        if (orderItem.getQuantity() != null && orderItem.getQuantity() <= 0) {
            return Result.badRequest("购买数量必须大于0");
        }
        return orderItemService.updateOrderItem(orderItem) ? Result.success() : Result.error("更新订单商品明细失败");
    }

    @Operation(summary = "删除订单商品明细")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteOrderItem(@Parameter(description = "明细ID") @PathVariable Long id) {
        if (orderItemService.getOrderItemById(id) == null) {
            return Result.badRequest("订单商品明细不存在");
        }
        return orderItemService.deleteOrderItem(id) ? Result.success() : Result.error("删除订单商品明细失败");
    }

    @Operation(summary = "批量删除订单商品明细")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchOrderItems(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        List<Long> validIds = ids.stream().filter(id -> id != null).toList();
        if (validIds.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return orderItemService.deleteBatchOrderItems(validIds) ? Result.success() : Result.error("批量删除订单商品明细失败");
    }
}
