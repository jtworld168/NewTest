package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Coupon;
import com.supermarket.entity.Product;
import com.supermarket.entity.Store;
import com.supermarket.entity.StoreProduct;
import com.supermarket.service.CouponService;
import com.supermarket.service.ProductService;
import com.supermarket.service.StoreProductService;
import com.supermarket.service.StoreService;
import com.supermarket.vo.StoreProductVO;
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

@Tag(name = "店铺商品管理", description = "店铺商品增删改查接口")
@RestController
@RequestMapping("/api/store-products")
@RequiredArgsConstructor
public class StoreProductController {

    private final StoreProductService storeProductService;
    private final StoreService storeService;
    private final ProductService productService;
    private final CouponService couponService;

    private StoreProductVO toVO(StoreProduct sp, Map<Long, Store> storeMap, Map<Long, Product> productMap, Map<Long, Coupon> couponMap) {
        StoreProductVO vo = new StoreProductVO();
        BeanUtils.copyProperties(sp, vo);
        if (sp.getStoreId() != null) {
            Store store = storeMap.get(sp.getStoreId());
            if (store != null) vo.setStoreName(store.getName());
        }
        if (sp.getProductId() != null) {
            Product product = productMap.get(sp.getProductId());
            if (product != null) vo.setProductName(product.getName());
        }
        if (sp.getCouponId() != null) {
            Coupon coupon = couponMap.get(sp.getCouponId());
            if (coupon != null) vo.setCouponName(coupon.getName());
        }
        return vo;
    }

    private List<StoreProductVO> toVOList(List<StoreProduct> list) {
        Map<Long, Store> storeMap = storeService.listAll().stream()
                .collect(Collectors.toMap(Store::getId, s -> s, (a, b) -> a));
        Map<Long, Product> productMap = productService.listAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        Map<Long, Coupon> couponMap = couponService.listAll().stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c, (a, b) -> a));
        return list.stream().map(sp -> toVO(sp, storeMap, productMap, couponMap)).collect(Collectors.toList());
    }

    @Operation(summary = "根据ID查询店铺商品")
    @GetMapping("/get/{id}")
    public Result<StoreProduct> getById(@Parameter(description = "店铺商品ID") @PathVariable Long id) {
        StoreProduct sp = storeProductService.getStoreProductById(id);
        return sp != null ? Result.success(sp) : Result.error("店铺商品不存在");
    }

    @Operation(summary = "查询所有店铺商品")
    @GetMapping("/list")
    public Result<List<StoreProductVO>> getAll() {
        return Result.success(toVOList(storeProductService.listAll()));
    }

    @Operation(summary = "根据店铺ID查询商品")
    @GetMapping("/getByStoreId/{storeId}")
    public Result<List<StoreProductVO>> getByStoreId(@Parameter(description = "店铺ID") @PathVariable Long storeId) {
        return Result.success(toVOList(storeProductService.getByStoreId(storeId)));
    }

    @Operation(summary = "根据商品ID查询店铺商品")
    @GetMapping("/getByProductId/{productId}")
    public Result<List<StoreProductVO>> getByProductId(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return Result.success(toVOList(storeProductService.getByProductId(productId)));
    }

    @Operation(summary = "分页查询店铺商品")
    @GetMapping("/listPage")
    public Result<IPage<StoreProductVO>> listPage(
            @Parameter(description = "店铺ID（可选）") @RequestParam(required = false) Long storeId,
            @Parameter(description = "商品名称搜索（可选）") @RequestParam(required = false) String productName,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<StoreProduct> page;
        if (productName != null && !productName.isBlank()) {
            page = storeProductService.searchByProductName(productName.trim(), storeId, pageNum, pageSize);
        } else if (storeId != null) {
            page = storeProductService.listPageByStoreId(storeId, pageNum, pageSize);
        } else {
            page = storeProductService.listPage(pageNum, pageSize);
        }
        Map<Long, Store> storeMap = storeService.listAll().stream()
                .collect(Collectors.toMap(Store::getId, s -> s, (a, b) -> a));
        Map<Long, Product> productMap = productService.listAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        Map<Long, Coupon> couponMap = couponService.listAll().stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c, (a, b) -> a));
        IPage<StoreProductVO> voPage = page.convert(sp -> toVO(sp, storeMap, productMap, couponMap));
        return Result.success(voPage);
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

    @Operation(summary = "获取商品总库存（各店铺库存之和）")
    @GetMapping("/totalStock/{productId}")
    public Result<Integer> getTotalStock(@Parameter(description = "商品ID") @PathVariable Long productId) {
        List<StoreProduct> storeProducts = storeProductService.getByProductId(productId);
        int totalStock = storeProducts.stream()
                .mapToInt(sp -> sp.getStoreStock() != null ? sp.getStoreStock() : 0)
                .sum();
        return Result.success(totalStock);
    }

    @Operation(summary = "获取店铺库存预警商品（库存低于安全库存）")
    @GetMapping("/lowStock/{storeId}")
    public Result<List<StoreProductVO>> getLowStock(
            @Parameter(description = "店铺ID") @PathVariable Long storeId) {
        return Result.success(toVOList(storeProductService.getLowStockByStoreIdUsingSafetyStock(storeId)));
    }

    @Operation(summary = "按商品名称新增店铺商品（若商品不存在则自动创建）")
    @PostMapping("/addWithName")
    public Result<StoreProduct> addStoreProductWithName(@RequestBody Map<String, Object> body) {
        String productName = (String) body.get("productName");
        if (productName == null || productName.isBlank()) {
            return Result.badRequest("商品名称不能为空");
        }
        Object storeIdObj = body.get("storeId");
        if (storeIdObj == null) {
            return Result.badRequest("店铺ID不能为空");
        }
        StoreProduct sp = new StoreProduct();
        sp.setStoreId(Long.valueOf(storeIdObj.toString()));
        if (body.get("storePrice") != null) {
            sp.setStorePrice(new java.math.BigDecimal(body.get("storePrice").toString()));
        }
        if (body.get("storeStock") != null) {
            sp.setStoreStock(Integer.valueOf(body.get("storeStock").toString()));
        }
        if (body.get("safetyStock") != null) {
            sp.setSafetyStock(Integer.valueOf(body.get("safetyStock").toString()));
        }
        if (body.get("status") != null) {
            sp.setStatus(Integer.valueOf(body.get("status").toString()));
        }
        StoreProduct result = storeProductService.addStoreProductWithName(productName.trim(), sp);
        return Result.success(result);
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

    @Operation(summary = "一键设置优惠券（批量）")
    @PutMapping("/batchSetCoupon")
    public Result<Void> batchSetCoupon(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> idNumbers = (List<Number>) body.get("ids");
        Object couponIdObj = body.get("couponId");
        if (idNumbers == null || idNumbers.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        if (couponIdObj == null) {
            return Result.badRequest("优惠券ID不能为空");
        }
        List<Long> ids = idNumbers.stream().map(Number::longValue).toList();
        Long couponId = Long.valueOf(couponIdObj.toString());
        return storeProductService.batchSetCoupon(ids, couponId) ? Result.success() : Result.error("设置优惠券失败");
    }
}
