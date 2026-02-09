package com.supermarket;

import com.supermarket.entity.Coupon;
import com.supermarket.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Test
    void testAddAndGetCoupon() {
        Coupon coupon = new Coupon();
        coupon.setName("满100减10");
        coupon.setDiscount(new BigDecimal("10.00"));
        coupon.setMinAmount(new BigDecimal("100.00"));
        coupon.setTotalCount(100);
        coupon.setRemainingCount(100);
        coupon.setStartTime(LocalDateTime.now());
        coupon.setEndTime(LocalDateTime.now().plusDays(30));

        assertTrue(couponService.addCoupon(coupon));
        assertNotNull(coupon.getId());

        Coupon found = couponService.getCouponById(coupon.getId());
        assertNotNull(found);
        assertEquals("满100减10", found.getName());
        assertEquals(0, new BigDecimal("10.00").compareTo(found.getDiscount()));
        assertEquals(100, found.getTotalCount());
        assertEquals(100, found.getRemainingCount());
    }

    @Test
    void testGetCouponByName() {
        Coupon coupon = new Coupon();
        coupon.setName("满200减30");
        coupon.setDiscount(new BigDecimal("30.00"));
        coupon.setMinAmount(new BigDecimal("200.00"));
        coupon.setTotalCount(50);
        coupon.setRemainingCount(50);
        couponService.addCoupon(coupon);

        Coupon found = couponService.getCouponByName("满200减30");
        assertNotNull(found);
        assertEquals(0, new BigDecimal("30.00").compareTo(found.getDiscount()));
    }

    @Test
    void testUpdateCoupon() {
        Coupon coupon = new Coupon();
        coupon.setName("折扣券");
        coupon.setDiscount(new BigDecimal("15.00"));
        coupon.setMinAmount(new BigDecimal("150.00"));
        coupon.setTotalCount(200);
        coupon.setRemainingCount(200);
        couponService.addCoupon(coupon);

        coupon.setRemainingCount(150);
        assertTrue(couponService.updateCoupon(coupon));

        Coupon updated = couponService.getCouponById(coupon.getId());
        assertEquals(150, updated.getRemainingCount());
    }

    @Test
    void testDeleteCoupon() {
        Coupon coupon = new Coupon();
        coupon.setName("临时券");
        coupon.setDiscount(new BigDecimal("3.00"));
        coupon.setMinAmount(new BigDecimal("30.00"));
        coupon.setTotalCount(10);
        coupon.setRemainingCount(10);
        couponService.addCoupon(coupon);

        assertTrue(couponService.deleteCoupon(coupon.getId()));

        Coupon deleted = couponService.getCouponById(coupon.getId());
        assertNull(deleted);
    }

    @Test
    void testDeleteBatchCoupons() {
        Coupon c1 = new Coupon();
        c1.setName("批量删除券1");
        c1.setDiscount(new BigDecimal("5.00"));
        c1.setMinAmount(new BigDecimal("50.00"));
        c1.setTotalCount(10);
        c1.setRemainingCount(10);
        couponService.addCoupon(c1);

        Coupon c2 = new Coupon();
        c2.setName("批量删除券2");
        c2.setDiscount(new BigDecimal("10.00"));
        c2.setMinAmount(new BigDecimal("100.00"));
        c2.setTotalCount(20);
        c2.setRemainingCount(20);
        couponService.addCoupon(c2);

        assertTrue(couponService.deleteBatchCoupons(List.of(c1.getId(), c2.getId())));

        assertNull(couponService.getCouponById(c1.getId()));
        assertNull(couponService.getCouponById(c2.getId()));
    }
}
