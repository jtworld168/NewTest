package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Product;
import com.supermarket.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "商品管理", description = "商品增删改查接口")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "根据ID查询商品")
    @GetMapping("/get/{id}")
    public Result<Product> getProductById(@Parameter(description = "商品ID") @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return product != null ? Result.success(product) : Result.error("商品不存在");
    }

    @Operation(summary = "查询所有商品")
    @GetMapping("/list")
    public Result<List<Product>> getAllProducts() {
        return Result.success(productService.list());
    }

    @Operation(summary = "根据名称搜索商品")
    @GetMapping("/search")
    public Result<List<Product>> searchProducts(@Parameter(description = "商品名称关键词") @RequestParam String name) {
        return Result.success(productService.searchProductsByName(name));
    }

    @Operation(summary = "根据名称精确查询商品")
    @GetMapping("/getByName/{name}")
    public Result<Product> getProductByName(@Parameter(description = "商品名称") @PathVariable String name) {
        Product product = productService.getProductByName(name);
        return product != null ? Result.success(product) : Result.error("商品不存在");
    }

    @Operation(summary = "添加商品")
    @PostMapping("/add")
    public Result<Void> addProduct(@RequestBody Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            return Result.error("商品名称不能为空");
        }
        if (product.getPrice() == null) {
            return Result.error("商品价格不能为空");
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("商品价格不能为负数");
        }
        if (product.getStock() == null) {
            return Result.error("库存数量不能为空");
        }
        if (product.getStock() < 0) {
            return Result.error("库存数量不能为负数");
        }
        return productService.addProduct(product) ? Result.success() : Result.error("添加商品失败");
    }

    @Operation(summary = "更新商品")
    @PutMapping("/update")
    public Result<Void> updateProduct(@RequestBody Product product) {
        if (product.getId() == null) {
            return Result.error("商品ID不能为空");
        }
        if (productService.getProductById(product.getId()) == null) {
            return Result.error("商品不存在");
        }
        return productService.updateProduct(product) ? Result.success() : Result.error("更新商品失败");
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteProduct(@Parameter(description = "商品ID") @PathVariable Long id) {
        if (productService.getProductById(id) == null) {
            return Result.error("商品不存在");
        }
        return productService.deleteProduct(id) ? Result.success() : Result.error("删除商品失败");
    }
}
