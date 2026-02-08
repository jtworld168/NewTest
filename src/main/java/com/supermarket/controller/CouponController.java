package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Coupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "优惠券管理", description = "优惠券增删改查接口")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "根据ID查询优惠券")
    @GetMapping("/get/{id}")
    public Result<Coupon> getCouponById(@Parameter(description = "优惠券ID") @PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return coupon != null ? Result.success(coupon) : Result.error("优惠券不存在");
    }

    @Operation(summary = "查询所有优惠券")
    @GetMapping("/list")
    public Result<List<Coupon>> getAllCoupons() {
        return Result.success(couponService.list());
    }

    @Operation(summary = "根据状态查询优惠券")
    @GetMapping("/getByStatus/{status}")
    public Result<List<Coupon>> getCouponsByStatus(@Parameter(description = "优惠券状态：AVAILABLE/USED/EXPIRED") @PathVariable CouponStatus status) {
        return Result.success(couponService.getCouponsByStatus(status));
    }

    @Operation(summary = "根据名称查询优惠券")
    @GetMapping("/getByName/{name}")
    public Result<Coupon> getCouponByName(@Parameter(description = "优惠券名称") @PathVariable String name) {
        Coupon coupon = couponService.getCouponByName(name);
        return coupon != null ? Result.success(coupon) : Result.error("优惠券不存在");
    }

    @Operation(summary = "添加优惠券")
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
        if (coupon.getStatus() == null) {
            coupon.setStatus(CouponStatus.AVAILABLE);
        }
        return couponService.addCoupon(coupon) ? Result.success() : Result.error("添加优惠券失败");
    }

    @Operation(summary = "更新优惠券")
    @PutMapping("/update")
    public Result<Void> updateCoupon(@RequestBody Coupon coupon) {
        if (coupon.getId() == null) {
            return Result.error("优惠券ID不能为空");
        }
        if (couponService.getCouponById(coupon.getId()) == null) {
            return Result.error("优惠券不存在");
        }
        return couponService.updateCoupon(coupon) ? Result.success() : Result.error("更新优惠券失败");
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCoupon(@Parameter(description = "优惠券ID") @PathVariable Long id) {
        if (couponService.getCouponById(id) == null) {
            return Result.error("优惠券不存在");
        }
        return couponService.deleteCoupon(id) ? Result.success() : Result.error("删除优惠券失败");
    }
}
