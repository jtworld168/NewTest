package com.supermarket.controller;

import com.supermarket.annotation.RateLimit;
import com.supermarket.common.Result;
import com.supermarket.entity.Order;
import com.supermarket.entity.Product;
import com.supermarket.entity.Store;
import com.supermarket.entity.User;
import com.supermarket.enums.OrderStatus;
import com.supermarket.service.OrderService;
import com.supermarket.service.ProductService;
import com.supermarket.service.StoreService;
import com.supermarket.service.UserService;
import com.supermarket.vo.OrderVO;
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

@Tag(name = "订单管理", description = "订单增删改查接口（含员工折扣计算）")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final StoreService storeService;
    private final ProductService productService;

    private OrderVO toVO(Order order, Map<Long, User> userMap, Map<Long, Store> storeMap, Map<Long, Product> productMap) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        if (order.getUserId() != null) {
            User user = userMap.get(order.getUserId());
            if (user != null) vo.setUserName(user.getUsername());
        }
        if (order.getStoreId() != null) {
            Store store = storeMap.get(order.getStoreId());
            if (store != null) vo.setStoreName(store.getName());
        }
        if (order.getProductId() != null) {
            Product product = productMap.get(order.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductBarcode(product.getBarcode());
            }
        }
        return vo;
    }

    private List<OrderVO> toVOList(List<Order> orders) {
        Map<Long, User> userMap = userService.listAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Store> storeMap = storeService.listAll().stream()
                .collect(Collectors.toMap(Store::getId, s -> s, (a, b) -> a));
        Map<Long, Product> productMap = productService.listAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        return orders.stream().map(o -> toVO(o, userMap, storeMap, productMap)).collect(Collectors.toList());
    }

    @Operation(summary = "根据ID查询订单")
    @GetMapping("/get/{id}")
    public Result<Order> getOrderById(@Parameter(description = "订单ID") @PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return order != null ? Result.success(order) : Result.error("订单不存在");
    }

    @Operation(summary = "查询所有订单")
    @GetMapping("/list")
    public Result<List<OrderVO>> getAllOrders() {
        return Result.success(toVOList(orderService.listAll()));
    }

    @Operation(summary = "根据用户ID查询订单")
    @GetMapping("/getByUserId/{userId}")
    public Result<List<OrderVO>> getOrdersByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(toVOList(orderService.getOrdersByUserId(userId)));
    }

    @Operation(summary = "根据状态查询订单")
    @GetMapping("/getByStatus/{status}")
    public Result<List<OrderVO>> getOrdersByStatus(@Parameter(description = "订单状态：PENDING/PAID/COMPLETED/CANCELLED") @PathVariable OrderStatus status) {
        return Result.success(toVOList(orderService.getOrdersByStatus(status)));
    }

    @Operation(summary = "根据商品ID查询订单")
    @GetMapping("/getByProductId/{productId}")
    public Result<List<OrderVO>> getOrdersByProductId(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(toVOList(orderService.getOrdersByProductId(productId)));
    }

    @Operation(summary = "根据用户优惠券ID查询订单")
    @GetMapping("/getByUserCouponId/{userCouponId}")
    public Result<List<OrderVO>> getOrdersByUserCouponId(@Parameter(description = "用户优惠券ID") @PathVariable Long userCouponId) {
        return Result.success(toVOList(orderService.getOrdersByUserCouponId(userCouponId)));
    }

    @Operation(summary = "根据店铺ID查询订单")
    @GetMapping("/getByStoreId/{storeId}")
    public Result<List<OrderVO>> getOrdersByStoreId(@Parameter(description = "店铺ID") @PathVariable Long storeId) {
        return Result.success(toVOList(orderService.getOrdersByStoreId(storeId)));
    }

    @Operation(summary = "分页查询订单列表")
    @GetMapping("/listPage")
    public Result<IPage<OrderVO>> listPage(
            @Parameter(description = "店铺ID（可选，为空查全部）") @RequestParam(required = false) Long storeId,
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Order> page;
        if (storeId != null) {
            page = orderService.listPageByStoreId(storeId, pageNum, pageSize);
        } else {
            page = orderService.listPage(pageNum, pageSize);
        }
        Map<Long, User> userMap = userService.listAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Store> storeMap = storeService.listAll().stream()
                .collect(Collectors.toMap(Store::getId, s -> s, (a, b) -> a));
        Map<Long, Product> productMap = productService.listAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        IPage<OrderVO> voPage = page.convert(o -> toVO(o, userMap, storeMap, productMap));
        return Result.success(voPage);
    }

    @Operation(summary = "添加订单（自动计算员工折扣价和总金额）")
    @PostMapping("/add")
    @RateLimit(key = "order:add", maxRequests = 5, windowSeconds = 1, message = "下单过于频繁，请稍后再试")
    public Result<Order> addOrder(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "购买数量") @RequestParam Integer quantity,
            @Parameter(description = "用户优惠券ID（可选）") @RequestParam(required = false) Long userCouponId,
            @Parameter(description = "店铺ID（可选）") @RequestParam(required = false) Long storeId) {
        if (userId == null) {
            return Result.badRequest("用户ID不能为空");
        }
        if (productId == null) {
            return Result.badRequest("商品ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            return Result.badRequest("购买数量必须大于0");
        }
        try {
            Order order = orderService.addOrder(userId, productId, quantity, userCouponId, storeId);
            return order != null ? Result.success(order) : Result.error("添加订单失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "添加多商品订单（一个订单包含多个商品）")
    @PostMapping("/addMultiItem")
    @RateLimit(key = "order:addMulti", maxRequests = 5, windowSeconds = 1, message = "下单过于频繁，请稍后再试")
    public Result<Order> addMultiItemOrder(@RequestBody Map<String, Object> request) {
        Long userId = request.get("userId") != null ? ((Number) request.get("userId")).longValue() : null;
        Long storeId = request.get("storeId") != null ? ((Number) request.get("storeId")).longValue() : null;
        Long userCouponId = request.get("userCouponId") != null ? ((Number) request.get("userCouponId")).longValue() : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

        if (userId == null) {
            return Result.badRequest("用户ID不能为空");
        }
        if (items == null || items.isEmpty()) {
            return Result.badRequest("商品列表不能为空");
        }

        try {
            Order order = orderService.addMultiItemOrder(userId, storeId, items, userCouponId);
            return order != null ? Result.success(order) : Result.error("添加订单失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
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
