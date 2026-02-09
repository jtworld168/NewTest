package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Coupon;
import com.supermarket.service.CouponService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "优惠券面额管理", description = "优惠券模板/面额增删改查接口")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "根据ID查询优惠券面额")
    @GetMapping("/get/{id}")
    public Result<Coupon> getCouponById(@Parameter(description = "优惠券面额ID") @PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return coupon != null ? Result.success(coupon) : Result.error("优惠券面额不存在");
    }

    @Operation(summary = "查询所有优惠券面额")
    @GetMapping("/list")
    public Result<List<Coupon>> getAllCoupons() {
        return Result.success(couponService.list());
    }

    @Operation(summary = "根据名称查询优惠券面额")
    @GetMapping("/getByName/{name}")
    public Result<Coupon> getCouponByName(@Parameter(description = "优惠券名称") @PathVariable String name) {
        Coupon coupon = couponService.getCouponByName(name);
        return coupon != null ? Result.success(coupon) : Result.error("优惠券面额不存在");
    }

    @Operation(summary = "模糊搜索优惠券（按名称）")
    @GetMapping("/search")
    public Result<List<Coupon>> searchCoupons(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return Result.success(couponService.searchCoupons(keyword));
    }

    @Operation(summary = "分页查询优惠券面额列表")
    @GetMapping("/listPage")
    public Result<IPage<Coupon>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(couponService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加优惠券面额")
    @PostMapping("/add")
    public Result<Void> addCoupon(@RequestBody Coupon coupon) {
        if (coupon.getName() == null || coupon.getName().isBlank()) {
            return Result.error("优惠券名称不能为空");
        }
        if (coupon.getDiscount() == null) {
            return Result.error("折扣金额不能为空");
        }
        if (coupon.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("折扣金额不能为负数");
        }
        if (coupon.getMinAmount() == null) {
            return Result.error("最低使用金额不能为空");
        }
        if (coupon.getMinAmount().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("最低使用金额不能为负数");
        }
        if (coupon.getTotalCount() == null || coupon.getTotalCount() <= 0) {
            return Result.error("发放总数量必须大于0");
        }
        if (coupon.getRemainingCount() == null) {
            coupon.setRemainingCount(coupon.getTotalCount());
        }
        return couponService.addCoupon(coupon) ? Result.success() : Result.error("添加优惠券面额失败");
    }

    @Operation(summary = "更新优惠券面额")
    @PutMapping("/update")
    public Result<Void> updateCoupon(@RequestBody Coupon coupon) {
        if (coupon.getId() == null) {
            return Result.error("优惠券面额ID不能为空");
        }
        if (couponService.getCouponById(coupon.getId()) == null) {
            return Result.error("优惠券面额不存在");
        }
        return couponService.updateCoupon(coupon) ? Result.success() : Result.error("更新优惠券面额失败");
    }

    @Operation(summary = "删除优惠券面额")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCoupon(@Parameter(description = "优惠券面额ID") @PathVariable Long id) {
        if (couponService.getCouponById(id) == null) {
            return Result.error("优惠券面额不存在");
        }
        return couponService.deleteCoupon(id) ? Result.success() : Result.error("删除优惠券面额失败");
    }

    @Operation(summary = "批量删除优惠券面额")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchCoupons(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("ID列表不能为空");
        }
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return Result.error("ID列表不能为空");
        }
        return couponService.deleteBatchCoupons(ids) ? Result.success() : Result.error("批量删除优惠券面额失败");
    }
}
