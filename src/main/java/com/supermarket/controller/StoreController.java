package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Store;
import com.supermarket.service.StoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "店铺管理", description = "店铺增删改查接口")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "根据ID查询店铺")
    @GetMapping("/get/{id}")
    public Result<Store> getStoreById(@Parameter(description = "店铺ID") @PathVariable Long id) {
        Store store = storeService.getStoreById(id);
        return store != null ? Result.success(store) : Result.error("店铺不存在");
    }

    @Operation(summary = "查询所有店铺")
    @GetMapping("/list")
    public Result<List<Store>> getAllStores() {
        return Result.success(storeService.listAll());
    }

    @Operation(summary = "根据名称查询店铺")
    @GetMapping("/getByName/{name}")
    public Result<Store> getStoreByName(@Parameter(description = "店铺名称") @PathVariable String name) {
        Store store = storeService.getStoreByName(name);
        return store != null ? Result.success(store) : Result.error("店铺不存在");
    }

    @Operation(summary = "搜索店铺")
    @GetMapping("/search")
    public Result<List<Store>> searchStores(@Parameter(description = "关键词") @RequestParam String keyword) {
        return Result.success(storeService.searchStores(keyword));
    }

    @Operation(summary = "分页查询店铺列表")
    @GetMapping("/listPage")
    public Result<IPage<Store>> listPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(storeService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加店铺")
    @PostMapping("/add")
    public Result<Void> addStore(@RequestBody Store store) {
        if (store.getName() == null || store.getName().isBlank()) {
            return Result.badRequest("店铺名称不能为空");
        }
        return storeService.addStore(store) ? Result.success() : Result.error("添加店铺失败");
    }

    @Operation(summary = "更新店铺")
    @PutMapping("/update")
    public Result<Void> updateStore(@RequestBody Store store) {
        if (store.getId() == null) {
            return Result.badRequest("店铺ID不能为空");
        }
        if (storeService.getStoreById(store.getId()) == null) {
            return Result.badRequest("店铺不存在");
        }
        return storeService.updateStore(store) ? Result.success() : Result.error("更新店铺失败");
    }

    @Operation(summary = "删除店铺")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteStore(@Parameter(description = "店铺ID") @PathVariable Long id) {
        if (storeService.getStoreById(id) == null) {
            return Result.badRequest("店铺不存在");
        }
        return storeService.deleteStore(id) ? Result.success() : Result.error("删除店铺失败");
    }

    @Operation(summary = "批量删除店铺")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchStores(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return storeService.deleteBatchStores(ids) ? Result.success() : Result.error("批量删除失败");
    }
}
