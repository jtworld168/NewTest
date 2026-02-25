package com.supermarket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.Coupon;

import java.util.List;

public interface CouponService extends IService<Coupon> {

    Coupon getCouponById(Long id);

    Coupon getCouponByName(String name);

    List<Coupon> searchCoupons(String keyword);

    IPage<Coupon> listPage(int pageNum, int pageSize);

    boolean addCoupon(Coupon coupon);

    boolean updateCoupon(Coupon coupon);

    boolean deleteCoupon(Long id);

    boolean deleteBatchCoupons(List<Long> ids);

    List<Coupon> listAll();
}
