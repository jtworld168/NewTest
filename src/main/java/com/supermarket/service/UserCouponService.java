package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;

import java.util.List;

public interface UserCouponService extends IService<UserCoupon> {

    UserCoupon getUserCouponById(Long id);

    List<UserCoupon> getUserCouponsByUserId(Long userId);

    List<UserCoupon> getUserCouponsByCouponId(Long couponId);

    List<UserCoupon> getUserCouponsByStatus(CouponStatus status);

    List<UserCoupon> getUserCouponsByUserIdAndStatus(Long userId, CouponStatus status);

    IPage<UserCoupon> listPage(int pageNum, int pageSize);

    boolean addUserCoupon(UserCoupon userCoupon);

    boolean updateUserCoupon(UserCoupon userCoupon);

    boolean deleteUserCoupon(Long id);

    boolean deleteBatchUserCoupons(List<Long> ids);
}
