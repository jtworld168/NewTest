package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        wrapper.eq(UserCoupon::getUserId, userId)
               .orderByDesc(UserCoupon::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<UserCoupon> getUserCouponsByCouponId(Long couponId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, couponId)
               .orderByDesc(UserCoupon::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<UserCoupon> getUserCouponsByStatus(CouponStatus status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getStatus, status)
               .orderByDesc(UserCoupon::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<UserCoupon> getUserCouponsByUserIdAndStatus(Long userId, CouponStatus status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
               .eq(UserCoupon::getStatus, status)
               .orderByDesc(UserCoupon::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<UserCoupon> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserCoupon::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
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
