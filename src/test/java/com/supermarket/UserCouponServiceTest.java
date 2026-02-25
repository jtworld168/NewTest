package com.supermarket;

import com.supermarket.entity.Coupon;
import com.supermarket.entity.User;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.enums.UserRole;
import com.supermarket.service.CouponService;
import com.supermarket.service.UserCouponService;
import com.supermarket.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserCouponServiceTest {

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private UserService userService;

    @Autowired
    private CouponService couponService;

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);
        return user;
    }

    private Coupon createTestCoupon(String name, BigDecimal discount) {
        Coupon coupon = new Coupon();
        coupon.setName(name);
        coupon.setDiscount(discount);
        coupon.setMinAmount(new BigDecimal("50.00"));
        coupon.setTotalCount(100);
        coupon.setRemainingCount(100);
        couponService.addCoupon(coupon);
        return coupon;
    }

    @Test
    void testAddAndGetUserCoupon() {
        User user = createTestUser("uc_test_user1");
        Coupon coupon = createTestCoupon("测试面额券1", new BigDecimal("10.00"));

        UserCoupon uc = new UserCoupon();
        uc.setUserId(user.getId());
        uc.setCouponId(coupon.getId());
        uc.setStatus(CouponStatus.AVAILABLE);

        assertTrue(userCouponService.addUserCoupon(uc));
        assertNotNull(uc.getId());

        UserCoupon found = userCouponService.getUserCouponById(uc.getId());
        assertNotNull(found);
        assertEquals(user.getId(), found.getUserId());
        assertEquals(coupon.getId(), found.getCouponId());
        assertEquals(CouponStatus.AVAILABLE, found.getStatus());
    }

    @Test
    void testGetUserCouponsByUserId() {
        User user = createTestUser("uc_test_user2");
        Coupon coupon = createTestCoupon("测试面额券2", new BigDecimal("20.00"));

        UserCoupon uc1 = new UserCoupon();
        uc1.setUserId(user.getId());
        uc1.setCouponId(coupon.getId());
        uc1.setStatus(CouponStatus.AVAILABLE);
        userCouponService.addUserCoupon(uc1);

        UserCoupon uc2 = new UserCoupon();
        uc2.setUserId(user.getId());
        uc2.setCouponId(coupon.getId());
        uc2.setStatus(CouponStatus.USED);
        userCouponService.addUserCoupon(uc2);

        List<UserCoupon> results = userCouponService.getUserCouponsByUserId(user.getId());
        assertTrue(results.size() >= 2);
    }

    @Test
    void testGetUserCouponsByUserIdAndStatus() {
        User user = createTestUser("uc_test_user3");
        Coupon coupon = createTestCoupon("测试面额券3", new BigDecimal("5.00"));

        UserCoupon uc = new UserCoupon();
        uc.setUserId(user.getId());
        uc.setCouponId(coupon.getId());
        uc.setStatus(CouponStatus.AVAILABLE);
        userCouponService.addUserCoupon(uc);

        List<UserCoupon> available = userCouponService.getUserCouponsByUserIdAndStatus(user.getId(), CouponStatus.AVAILABLE);
        assertTrue(available.size() >= 1);

        List<UserCoupon> used = userCouponService.getUserCouponsByUserIdAndStatus(user.getId(), CouponStatus.USED);
        assertEquals(0, used.size());
    }

    @Test
    void testUpdateUserCoupon() {
        User user = createTestUser("uc_test_user4");
        Coupon coupon = createTestCoupon("测试面额券4", new BigDecimal("15.00"));

        UserCoupon uc = new UserCoupon();
        uc.setUserId(user.getId());
        uc.setCouponId(coupon.getId());
        uc.setStatus(CouponStatus.AVAILABLE);
        userCouponService.addUserCoupon(uc);

        uc.setStatus(CouponStatus.USED);
        assertTrue(userCouponService.updateUserCoupon(uc));

        UserCoupon updated = userCouponService.getUserCouponById(uc.getId());
        assertEquals(CouponStatus.USED, updated.getStatus());
    }

    @Test
    void testDeleteUserCoupon() {
        User user = createTestUser("uc_test_user5");
        Coupon coupon = createTestCoupon("测试面额券5", new BigDecimal("8.00"));

        UserCoupon uc = new UserCoupon();
        uc.setUserId(user.getId());
        uc.setCouponId(coupon.getId());
        uc.setStatus(CouponStatus.AVAILABLE);
        userCouponService.addUserCoupon(uc);

        assertTrue(userCouponService.deleteUserCoupon(uc.getId()));
        assertNull(userCouponService.getUserCouponById(uc.getId()));
    }

    @Test
    void testDeleteBatchUserCoupons() {
        User user = createTestUser("uc_test_user6");
        Coupon coupon = createTestCoupon("测试面额券6", new BigDecimal("12.00"));

        UserCoupon uc1 = new UserCoupon();
        uc1.setUserId(user.getId());
        uc1.setCouponId(coupon.getId());
        uc1.setStatus(CouponStatus.AVAILABLE);
        userCouponService.addUserCoupon(uc1);

        UserCoupon uc2 = new UserCoupon();
        uc2.setUserId(user.getId());
        uc2.setCouponId(coupon.getId());
        uc2.setStatus(CouponStatus.AVAILABLE);
        userCouponService.addUserCoupon(uc2);

        assertTrue(userCouponService.deleteBatchUserCoupons(List.of(uc1.getId(), uc2.getId())));
        assertNull(userCouponService.getUserCouponById(uc1.getId()));
        assertNull(userCouponService.getUserCouponById(uc2.getId()));
    }
}
