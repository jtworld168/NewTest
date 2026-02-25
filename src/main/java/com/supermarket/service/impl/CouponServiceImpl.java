package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.entity.Coupon;
import com.supermarket.mapper.CouponMapper;
import com.supermarket.service.CouponService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Override
    public Coupon getCouponById(Long id) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getId, id);
        return getOne(wrapper);
    }

    @Override
    public Coupon getCouponByName(String name) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getName, name);
        return getOne(wrapper);
    }

    @Override
    public List<Coupon> searchCoupons(String keyword) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Coupon::getName, keyword)
               .orderByDesc(Coupon::getCreateTime);
        return list(wrapper);
    }

    @Override
    public IPage<Coupon> listPage(int pageNum, int pageSize) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Coupon::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean addCoupon(Coupon coupon) {
        return save(coupon);
    }

    @Override
    public boolean updateCoupon(Coupon coupon) {
        return updateById(coupon);
    }

    @Override
    public boolean deleteCoupon(Long id) {
        return removeById(id);
    }

    @Override
    public boolean deleteBatchCoupons(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<Coupon> listAll() {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Coupon::getCreateTime);
        return list(wrapper);
    }
}
