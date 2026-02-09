package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.CartItem;
import com.supermarket.service.CartItemService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车管理", description = "购物车增删改查接口")
@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @Operation(summary = "根据ID查询购物车商品")
    @GetMapping("/get/{id}")
    public Result<CartItem> getCartItemById(@Parameter(description = "购物车商品ID") @PathVariable Long id) {
        CartItem item = cartItemService.getCartItemById(id);
        return item != null ? Result.success(item) : Result.error("购物车商品不存在");
    }

    @Operation(summary = "查询所有购物车商品")
    @GetMapping("/list")
    public Result<List<CartItem>> getAllCartItems() {
        return Result.success(cartItemService.listAll());
    }

    @Operation(summary = "根据用户ID查询购物车")
    @GetMapping("/getByUserId/{userId}")
    public Result<List<CartItem>> getCartItemsByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(cartItemService.getCartItemsByUserId(userId));
    }

    @Operation(summary = "根据商品ID查询购物车记录")
    @GetMapping("/getByProductId/{productId}")
    public Result<List<CartItem>> getCartItemsByProductId(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(cartItemService.getCartItemsByProductId(productId));
    }

    @Operation(summary = "分页查询购物车商品列表")
    @GetMapping("/listPage")
    public Result<IPage<CartItem>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(cartItemService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public Result<Void> addCartItem(@RequestBody CartItem cartItem) {
        if (cartItem.getUserId() == null) {
            return Result.badRequest("用户ID不能为空");
        }
        if (cartItem.getProductId() == null) {
            return Result.badRequest("商品ID不能为空");
        }
        if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
            return Result.badRequest("数量必须大于0");
        }
        return cartItemService.addCartItem(cartItem) ? Result.success() : Result.error("添加购物车失败");
    }

    @Operation(summary = "更新购物车商品")
    @PutMapping("/update")
    public Result<Void> updateCartItem(@RequestBody CartItem cartItem) {
        if (cartItem.getId() == null) {
            return Result.badRequest("购物车商品ID不能为空");
        }
        if (cartItemService.getCartItemById(cartItem.getId()) == null) {
            return Result.badRequest("购物车商品不存在");
        }
        if (cartItem.getQuantity() != null && cartItem.getQuantity() <= 0) {
            return Result.badRequest("数量必须大于0");
        }
        return cartItemService.updateCartItem(cartItem) ? Result.success() : Result.error("更新购物车失败");
    }

    @Operation(summary = "删除购物车商品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCartItem(@Parameter(description = "购物车商品ID") @PathVariable Long id) {
        if (cartItemService.getCartItemById(id) == null) {
            return Result.badRequest("购物车商品不存在");
        }
        return cartItemService.deleteCartItem(id) ? Result.success() : Result.error("删除购物车商品失败");
    }

    @Operation(summary = "批量删除购物车商品")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchCartItems(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        List<Long> validIds = ids.stream().filter(id -> id != null).toList();
        if (validIds.isEmpty()) {
            return Result.badRequest("ID列表中没有有效ID");
        }
        return cartItemService.deleteBatchCartItems(validIds) ? Result.success() : Result.error("批量删除购物车商品失败");
    }

    @Operation(summary = "清空用户购物车")
    @DeleteMapping("/clear/{userId}")
    public Result<Void> clearCart(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return cartItemService.clearCart(userId) ? Result.success() : Result.error("清空购物车失败");
    }
}
