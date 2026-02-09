package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.service.UserCouponService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户优惠券管理", description = "用户拥有的具体优惠券增删改查接口")
@RestController
@RequestMapping("/api/user-coupons")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponService userCouponService;

    @Operation(summary = "根据ID查询用户优惠券")
    @GetMapping("/get/{id}")
    public Result<UserCoupon> getUserCouponById(@Parameter(description = "用户优惠券ID") @PathVariable Long id) {
        UserCoupon userCoupon = userCouponService.getUserCouponById(id);
        return userCoupon != null ? Result.success(userCoupon) : Result.error("用户优惠券不存在");
    }

    @Operation(summary = "查询所有用户优惠券")
    @GetMapping("/list")
    public Result<List<UserCoupon>> getAllUserCoupons() {
        return Result.success(userCouponService.listAll());
    }

    @Operation(summary = "根据用户ID查询用户优惠券")
    @GetMapping("/getByUserId/{userId}")
    public Result<List<UserCoupon>> getUserCouponsByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(userCouponService.getUserCouponsByUserId(userId));
    }

    @Operation(summary = "根据优惠券面额ID查询用户优惠券")
    @GetMapping("/getByCouponId/{couponId}")
    public Result<List<UserCoupon>> getUserCouponsByCouponId(@Parameter(description = "优惠券面额ID") @PathVariable Long couponId) {
        return Result.success(userCouponService.getUserCouponsByCouponId(couponId));
    }

    @Operation(summary = "根据状态查询用户优惠券")
    @GetMapping("/getByStatus/{status}")
    public Result<List<UserCoupon>> getUserCouponsByStatus(@Parameter(description = "状态：AVAILABLE/USED/EXPIRED") @PathVariable CouponStatus status) {
        return Result.success(userCouponService.getUserCouponsByStatus(status));
    }

    @Operation(summary = "根据用户ID和状态查询用户优惠券")
    @GetMapping("/getByUserIdAndStatus")
    public Result<List<UserCoupon>> getUserCouponsByUserIdAndStatus(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "状态：AVAILABLE/USED/EXPIRED") @RequestParam CouponStatus status) {
        return Result.success(userCouponService.getUserCouponsByUserIdAndStatus(userId, status));
    }

    @Operation(summary = "分页查询用户优惠券列表")
    @GetMapping("/listPage")
    public Result<IPage<UserCoupon>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userCouponService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加用户优惠券（发放优惠券给用户）")
    @PostMapping("/add")
    public Result<Void> addUserCoupon(@RequestBody UserCoupon userCoupon) {
        if (userCoupon.getUserId() == null) {
            return Result.badRequest("用户ID不能为空");
        }
        if (userCoupon.getCouponId() == null) {
            return Result.badRequest("优惠券面额ID不能为空");
        }
        if (userCoupon.getStatus() == null) {
            userCoupon.setStatus(CouponStatus.AVAILABLE);
        }
        return userCouponService.addUserCoupon(userCoupon) ? Result.success() : Result.error("添加用户优惠券失败");
    }

    @Operation(summary = "更新用户优惠券")
    @PutMapping("/update")
    public Result<Void> updateUserCoupon(@RequestBody UserCoupon userCoupon) {
        if (userCoupon.getId() == null) {
            return Result.badRequest("用户优惠券ID不能为空");
        }
        if (userCouponService.getUserCouponById(userCoupon.getId()) == null) {
            return Result.badRequest("用户优惠券不存在");
        }
        return userCouponService.updateUserCoupon(userCoupon) ? Result.success() : Result.error("更新用户优惠券失败");
    }

    @Operation(summary = "删除用户优惠券")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteUserCoupon(@Parameter(description = "用户优惠券ID") @PathVariable Long id) {
        if (userCouponService.getUserCouponById(id) == null) {
            return Result.badRequest("用户优惠券不存在");
        }
        return userCouponService.deleteUserCoupon(id) ? Result.success() : Result.error("删除用户优惠券失败");
    }

    @Operation(summary = "批量删除用户优惠券")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchUserCoupons(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return userCouponService.deleteBatchUserCoupons(ids) ? Result.success() : Result.error("批量删除用户优惠券失败");
    }
}
