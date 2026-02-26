package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.ProductCreateDTO;
import com.supermarket.dto.ProductUpdateDTO;
import com.supermarket.entity.Category;
import com.supermarket.entity.Product;
import com.supermarket.service.CategoryService;
import com.supermarket.service.ProductService;
import com.supermarket.vo.ProductVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "商品管理", description = "商品增删改查接口")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    private ProductVO toVO(Product product, Map<Long, Category> categoryMap) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        if (product.getCategoryId() != null) {
            Category category = categoryMap.get(product.getCategoryId());
            if (category != null) vo.setCategoryName(category.getName());
        }
        return vo;
    }

    private List<ProductVO> toVOList(List<Product> products) {
        Map<Long, Category> categoryMap = categoryService.listAll().stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));
        return products.stream().map(p -> toVO(p, categoryMap)).collect(Collectors.toList());
    }

    @Operation(summary = "根据ID查询商品")
    @GetMapping("/get/{id}")
    public Result<Product> getProductById(@Parameter(description = "商品ID") @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return product != null ? Result.success(product) : Result.error("商品不存在");
    }

    @Operation(summary = "查询所有商品")
    @GetMapping("/list")
    public Result<List<ProductVO>> getAllProducts() {
        return Result.success(toVOList(productService.listAll()));
    }

    @Operation(summary = "根据名称搜索商品（模糊匹配名称）")
    @GetMapping("/search")
    public Result<List<ProductVO>> searchProducts(@Parameter(description = "商品名称关键词") @RequestParam String name) {
        return Result.success(toVOList(productService.searchProductsByName(name)));
    }

    @Operation(summary = "综合搜索商品（模糊匹配名称和描述）")
    @GetMapping("/searchAll")
    public Result<List<ProductVO>> searchAllProducts(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return Result.success(toVOList(productService.searchProducts(keyword)));
    }

    @Operation(summary = "分页查询商品列表")
    @GetMapping("/listPage")
    public Result<IPage<ProductVO>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Product> page = productService.listPage(pageNum, pageSize);
        Map<Long, Category> categoryMap = categoryService.listAll().stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));
        IPage<ProductVO> voPage = page.convert(p -> toVO(p, categoryMap));
        return Result.success(voPage);
    }

    @Operation(summary = "根据名称精确查询商品")
    @GetMapping("/getByName/{name}")
    public Result<Product> getProductByName(@Parameter(description = "商品名称") @PathVariable String name) {
        Product product = productService.getProductByName(name);
        return product != null ? Result.success(product) : Result.error("商品不存在");
    }

    @Operation(summary = "根据分类ID查询商品")
    @GetMapping("/getByCategoryId/{categoryId}")
    public Result<List<ProductVO>> getProductsByCategoryId(@Parameter(description = "分类ID") @PathVariable Long categoryId) {
        return Result.success(toVOList(productService.getProductsByCategoryId(categoryId)));
    }

    @Operation(summary = "添加商品")
    @PostMapping("/add")
    public Result<Void> addProduct(@RequestBody ProductCreateDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            return Result.badRequest("商品名称不能为空");
        }
        if (dto.getPrice() == null) {
            return Result.badRequest("商品价格不能为空");
        }
        if (dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.badRequest("商品价格必须大于0");
        }
        if (dto.getStock() == null) {
            return Result.badRequest("库存数量不能为空");
        }
        if (dto.getStock() < 0) {
            return Result.badRequest("库存数量不能为负数");
        }
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        return productService.addProduct(product) ? Result.success() : Result.error("添加商品失败");
    }

    @Operation(summary = "更新商品")
    @PutMapping("/update")
    public Result<Void> updateProduct(@RequestBody ProductUpdateDTO dto) {
        if (dto.getId() == null) {
            return Result.badRequest("商品ID不能为空");
        }
        if (productService.getProductById(dto.getId()) == null) {
            return Result.badRequest("商品不存在");
        }
        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.badRequest("商品价格必须大于0");
        }
        if (dto.getStock() != null && dto.getStock() < 0) {
            return Result.badRequest("库存数量不能为负数");
        }
        if (dto.getName() != null && dto.getName().isBlank()) {
            return Result.badRequest("商品名称不能为空字符串");
        }
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        return productService.updateProduct(product) ? Result.success() : Result.error("更新商品失败");
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteProduct(@Parameter(description = "商品ID") @PathVariable Long id) {
        if (productService.getProductById(id) == null) {
            return Result.badRequest("商品不存在");
        }
        return productService.deleteProduct(id) ? Result.success() : Result.error("删除商品失败");
    }

    @Operation(summary = "查询上架商品")
    @GetMapping("/onShelf")
    public Result<List<ProductVO>> getOnShelfProducts() {
        return Result.success(toVOList(productService.getOnShelfProducts()));
    }

    @Operation(summary = "查询库存预警商品")
    @GetMapping("/lowStock")
    public Result<List<ProductVO>> getLowStockProducts() {
        return Result.success(toVOList(productService.getLowStockProducts()));
    }

    @Operation(summary = "批量删除商品")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchProducts(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return productService.deleteBatchProducts(ids) ? Result.success() : Result.error("批量删除商品失败");
    }
}
