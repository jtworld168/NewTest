package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Category;
import com.supermarket.service.CategoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理", description = "商品分类增删改查接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "根据ID查询分类")
    @GetMapping("/get/{id}")
    public Result<Category> getCategoryById(@Parameter(description = "分类ID") @PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return category != null ? Result.success(category) : Result.error("分类不存在");
    }

    @Operation(summary = "查询所有分类")
    @GetMapping("/list")
    public Result<List<Category>> getAllCategories() {
        return Result.success(categoryService.list());
    }

    @Operation(summary = "根据名称查询分类")
    @GetMapping("/getByName/{name}")
    public Result<Category> getCategoryByName(@Parameter(description = "分类名称") @PathVariable String name) {
        Category category = categoryService.getCategoryByName(name);
        return category != null ? Result.success(category) : Result.error("分类不存在");
    }

    @Operation(summary = "模糊搜索分类（按名称或描述）")
    @GetMapping("/search")
    public Result<List<Category>> searchCategories(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return Result.success(categoryService.searchCategories(keyword));
    }

    @Operation(summary = "分页查询分类列表")
    @GetMapping("/listPage")
    public Result<IPage<Category>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(categoryService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加分类")
    @PostMapping("/add")
    public Result<Void> addCategory(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return Result.error("分类名称不能为空");
        }
        return categoryService.addCategory(category) ? Result.success() : Result.error("添加分类失败");
    }

    @Operation(summary = "更新分类")
    @PutMapping("/update")
    public Result<Void> updateCategory(@RequestBody Category category) {
        if (category.getId() == null) {
            return Result.error("分类ID不能为空");
        }
        if (categoryService.getCategoryById(category.getId()) == null) {
            return Result.error("分类不存在");
        }
        return categoryService.updateCategory(category) ? Result.success() : Result.error("更新分类失败");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCategory(@Parameter(description = "分类ID") @PathVariable Long id) {
        if (categoryService.getCategoryById(id) == null) {
            return Result.error("分类不存在");
        }
        return categoryService.deleteCategory(id) ? Result.success() : Result.error("删除分类失败");
    }

    @Operation(summary = "批量删除分类")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchCategories(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("ID列表不能为空");
        }
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return Result.error("ID列表不能为空");
        }
        return categoryService.deleteBatchCategories(ids) ? Result.success() : Result.error("批量删除分类失败");
    }
}
