package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Coupon;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.entity.User;
import com.supermarket.service.CouponService;
import com.supermarket.service.UserCouponService;
import com.supermarket.service.UserService;
import com.supermarket.vo.UserCouponVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "用户优惠券管理", description = "用户拥有的具体优惠券增删改查接口")
@RestController
@RequestMapping("/api/user-coupons")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponService userCouponService;
    private final CouponService couponService;
    private final UserService userService;

    private UserCouponVO toVO(UserCoupon uc, Map<Long, User> userMap, Map<Long, Coupon> couponMap) {
        UserCouponVO vo = new UserCouponVO();
        BeanUtils.copyProperties(uc, vo);
        if (uc.getUserId() != null) {
            User user = userMap.get(uc.getUserId());
            if (user != null) vo.setUserName(user.getUsername());
        }
        if (uc.getCouponId() != null) {
            Coupon coupon = couponMap.get(uc.getCouponId());
            if (coupon != null) vo.setCouponName(coupon.getName());
        }
        return vo;
    }

    private List<UserCouponVO> toVOList(List<UserCoupon> list) {
        Map<Long, User> userMap = userService.listAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Coupon> couponMap = couponService.listAll().stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c, (a, b) -> a));
        return list.stream().map(uc -> toVO(uc, userMap, couponMap)).collect(Collectors.toList());
    }

    @Operation(summary = "根据ID查询用户优惠券")
    @GetMapping("/get/{id}")
    public Result<UserCoupon> getUserCouponById(@Parameter(description = "用户优惠券ID") @PathVariable Long id) {
        UserCoupon userCoupon = userCouponService.getUserCouponById(id);
        return userCoupon != null ? Result.success(userCoupon) : Result.error("用户优惠券不存在");
    }

    @Operation(summary = "查询所有用户优惠券")
    @GetMapping("/list")
    public Result<List<UserCouponVO>> getAllUserCoupons() {
        return Result.success(toVOList(userCouponService.listAll()));
    }

    @Operation(summary = "根据用户ID查询用户优惠券")
    @GetMapping("/getByUserId/{userId}")
    public Result<List<UserCouponVO>> getUserCouponsByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(toVOList(userCouponService.getUserCouponsByUserId(userId)));
    }

    @Operation(summary = "根据优惠券面额ID查询用户优惠券")
    @GetMapping("/getByCouponId/{couponId}")
    public Result<List<UserCouponVO>> getUserCouponsByCouponId(@Parameter(description = "优惠券面额ID") @PathVariable Long couponId) {
        return Result.success(toVOList(userCouponService.getUserCouponsByCouponId(couponId)));
    }

    @Operation(summary = "根据状态查询用户优惠券")
    @GetMapping("/getByStatus/{status}")
    public Result<List<UserCouponVO>> getUserCouponsByStatus(@Parameter(description = "状态：AVAILABLE/USED/EXPIRED") @PathVariable CouponStatus status) {
        return Result.success(toVOList(userCouponService.getUserCouponsByStatus(status)));
    }

    @Operation(summary = "根据用户ID和状态查询用户优惠券")
    @GetMapping("/getByUserIdAndStatus")
    public Result<List<UserCouponVO>> getUserCouponsByUserIdAndStatus(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "状态：AVAILABLE/USED/EXPIRED") @RequestParam CouponStatus status) {
        return Result.success(toVOList(userCouponService.getUserCouponsByUserIdAndStatus(userId, status)));
    }

    @Operation(summary = "分页查询用户优惠券列表")
    @GetMapping("/listPage")
    public Result<IPage<UserCouponVO>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<UserCoupon> page = userCouponService.listPage(pageNum, pageSize);
        Map<Long, User> userMap = userService.listAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Coupon> couponMap = couponService.listAll().stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c, (a, b) -> a));
        IPage<UserCouponVO> voPage = page.convert(uc -> toVO(uc, userMap, couponMap));
        return Result.success(voPage);
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
    @Transactional
    public Result<Void> updateUserCoupon(@RequestBody UserCoupon userCoupon) {
        if (userCoupon.getId() == null) {
            return Result.badRequest("用户优惠券ID不能为空");
        }
        UserCoupon existing = userCouponService.getUserCouponById(userCoupon.getId());
        if (existing == null) {
            return Result.badRequest("用户优惠券不存在");
        }
        // When marking coupon as USED, decrement coupon template remainingCount
        if (userCoupon.getStatus() == CouponStatus.USED && existing.getStatus() != CouponStatus.USED) {
            Coupon coupon = couponService.getCouponById(existing.getCouponId());
            if (coupon != null && coupon.getRemainingCount() != null && coupon.getRemainingCount() > 0) {
                Coupon update = new Coupon();
                update.setId(coupon.getId());
                update.setRemainingCount(coupon.getRemainingCount() - 1);
                couponService.updateCoupon(update);
            }
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

    @Operation(summary = "一键发放优惠券给所有用户")
    @PostMapping("/distributeAll")
    @Transactional
    public Result<Void> distributeToAllUsers(@RequestParam Long couponId) {
        if (couponId == null) {
            return Result.badRequest("优惠券面额ID不能为空");
        }
        Coupon coupon = couponService.getCouponById(couponId);
        if (coupon == null) {
            return Result.badRequest("优惠券不存在");
        }
        List<User> users = userService.listAll();
        for (User user : users) {
            UserCoupon uc = new UserCoupon();
            uc.setUserId(user.getId());
            uc.setCouponId(couponId);
            uc.setStatus(CouponStatus.AVAILABLE);
            userCouponService.addUserCoupon(uc);
        }
        return Result.success();
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
