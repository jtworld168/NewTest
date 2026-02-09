package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.mapper.UserCouponMapper;
import com.supermarket.service.UserCouponService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    @Override
    public UserCoupon getUserCouponById(Long id) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getId, id);
        return getOne(wrapper);
    }

    @Override
    public List<UserCoupon> getUserCouponsByUserId(Long userId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        return list(wrapper);
    }

    @Override
    public List<UserCoupon> getUserCouponsByCouponId(Long couponId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, couponId);
        return list(wrapper);
    }

    @Override
    public List<UserCoupon> getUserCouponsByStatus(CouponStatus status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getStatus, status);
        return list(wrapper);
    }

    @Override
    public List<UserCoupon> getUserCouponsByUserIdAndStatus(Long userId, CouponStatus status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
               .eq(UserCoupon::getStatus, status);
        return list(wrapper);
    }

    @Override
    public boolean addUserCoupon(UserCoupon userCoupon) {
        return save(userCoupon);
    }

    @Override
    public boolean updateUserCoupon(UserCoupon userCoupon) {
        return updateById(userCoupon);
    }

    @Override
    public boolean deleteUserCoupon(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchUserCoupons(List<Long> ids) {
        return removeByIds(ids);
    }
}
