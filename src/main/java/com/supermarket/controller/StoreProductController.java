package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.StoreProduct;
import com.supermarket.service.StoreProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "店铺商品管理", description = "店铺商品增删改查接口")
@RestController
@RequestMapping("/api/store-products")
@RequiredArgsConstructor
public class StoreProductController {

    private final StoreProductService storeProductService;

    @Operation(summary = "根据ID查询店铺商品")
    @GetMapping("/get/{id}")
    public Result<StoreProduct> getById(@Parameter(description = "店铺商品ID") @PathVariable Long id) {
        StoreProduct sp = storeProductService.getStoreProductById(id);
        return sp != null ? Result.success(sp) : Result.error("店铺商品不存在");
    }

    @Operation(summary = "查询所有店铺商品")
    @GetMapping("/list")
    public Result<List<StoreProduct>> getAll() {
        return Result.success(storeProductService.listAll());
    }

    @Operation(summary = "根据店铺ID查询商品")
    @GetMapping("/getByStoreId/{storeId}")
    public Result<List<StoreProduct>> getByStoreId(@Parameter(description = "店铺ID") @PathVariable Long storeId) {
        return Result.success(storeProductService.getByStoreId(storeId));
    }

    @Operation(summary = "根据商品ID查询店铺商品")
    @GetMapping("/getByProductId/{productId}")
    public Result<List<StoreProduct>> getByProductId(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(storeProductService.getByProductId(productId));
    }

    @Operation(summary = "分页查询店铺商品")
    @GetMapping("/listPage")
    public Result<IPage<StoreProduct>> listPage(
            @Parameter(description = "店铺ID（可选）") @RequestParam(required = false) Long storeId,
            @Parameter(description = "商品名称搜索（可选）") @RequestParam(required = false) String productName,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        if (productName != null && !productName.isBlank()) {
            return Result.success(storeProductService.searchByProductName(productName.trim(), storeId, pageNum, pageSize));
        }
        if (storeId != null) {
            return Result.success(storeProductService.listPageByStoreId(storeId, pageNum, pageSize));
        }
        return Result.success(storeProductService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加店铺商品")
    @PostMapping("/add")
    public Result<Void> addStoreProduct(@RequestBody StoreProduct storeProduct) {
        if (storeProduct.getStoreId() == null) {
            return Result.badRequest("店铺ID不能为空");
        }
        if (storeProduct.getProductId() == null) {
            return Result.badRequest("商品ID不能为空");
        }
        return storeProductService.addStoreProduct(storeProduct) ? Result.success() : Result.error("添加失败");
    }

    @Operation(summary = "更新店铺商品")
    @PutMapping("/update")
    public Result<Void> updateStoreProduct(@RequestBody StoreProduct storeProduct) {
        if (storeProduct.getId() == null) {
            return Result.badRequest("ID不能为空");
        }
        if (storeProductService.getStoreProductById(storeProduct.getId()) == null) {
            return Result.badRequest("店铺商品不存在");
        }
        return storeProductService.updateStoreProduct(storeProduct) ? Result.success() : Result.error("更新失败");
    }

    @Operation(summary = "删除店铺商品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteStoreProduct(@Parameter(description = "ID") @PathVariable Long id) {
        if (storeProductService.getStoreProductById(id) == null) {
            return Result.badRequest("店铺商品不存在");
        }
        return storeProductService.deleteStoreProduct(id) ? Result.success() : Result.error("删除失败");
    }

    @Operation(summary = "批量删除店铺商品")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchStoreProducts(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return storeProductService.deleteBatchStoreProducts(ids) ? Result.success() : Result.error("批量删除失败");
    }
}
