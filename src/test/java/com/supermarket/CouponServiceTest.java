package com.supermarket;

import com.supermarket.entity.Coupon;
import com.supermarket.enums.CouponStatus;
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
        coupon.setStartTime(LocalDateTime.now());
        coupon.setEndTime(LocalDateTime.now().plusDays(30));
        coupon.setStatus(CouponStatus.AVAILABLE);

        assertTrue(couponService.addCoupon(coupon));
        assertNotNull(coupon.getId());

        Coupon found = couponService.getCouponById(coupon.getId());
        assertNotNull(found);
        assertEquals("满100减10", found.getName());
        assertEquals(0, new BigDecimal("10.00").compareTo(found.getDiscount()));
        assertEquals(CouponStatus.AVAILABLE, found.getStatus());
    }

    @Test
    void testGetCouponByName() {
        Coupon coupon = new Coupon();
        coupon.setName("满200减30");
        coupon.setDiscount(new BigDecimal("30.00"));
        coupon.setMinAmount(new BigDecimal("200.00"));
        coupon.setStatus(CouponStatus.AVAILABLE);
        couponService.addCoupon(coupon);

        Coupon found = couponService.getCouponByName("满200减30");
        assertNotNull(found);
        assertEquals(0, new BigDecimal("30.00").compareTo(found.getDiscount()));
    }

    @Test
    void testGetCouponsByStatus() {
        Coupon c1 = new Coupon();
        c1.setName("新人券1");
        c1.setDiscount(new BigDecimal("5.00"));
        c1.setMinAmount(new BigDecimal("50.00"));
        c1.setStatus(CouponStatus.USED);
        couponService.addCoupon(c1);

        List<Coupon> usedCoupons = couponService.getCouponsByStatus(CouponStatus.USED);
        assertTrue(usedCoupons.size() >= 1);
    }

    @Test
    void testUpdateCoupon() {
        Coupon coupon = new Coupon();
        coupon.setName("折扣券");
        coupon.setDiscount(new BigDecimal("15.00"));
        coupon.setMinAmount(new BigDecimal("150.00"));
        coupon.setStatus(CouponStatus.AVAILABLE);
        couponService.addCoupon(coupon);

        coupon.setStatus(CouponStatus.EXPIRED);
        assertTrue(couponService.updateCoupon(coupon));

        Coupon updated = couponService.getCouponById(coupon.getId());
        assertEquals(CouponStatus.EXPIRED, updated.getStatus());
    }

    @Test
    void testDeleteCoupon() {
        Coupon coupon = new Coupon();
        coupon.setName("临时券");
        coupon.setDiscount(new BigDecimal("3.00"));
        coupon.setMinAmount(new BigDecimal("30.00"));
        coupon.setStatus(CouponStatus.AVAILABLE);
        couponService.addCoupon(coupon);

        assertTrue(couponService.deleteCoupon(coupon.getId()));

        Coupon deleted = couponService.getCouponById(coupon.getId());
        assertNull(deleted);
    }
}
