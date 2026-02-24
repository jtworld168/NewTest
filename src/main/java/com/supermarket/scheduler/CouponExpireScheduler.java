package com.supermarket.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.Coupon;
import com.supermarket.entity.UserCoupon;
import com.supermarket.enums.CouponStatus;
import com.supermarket.service.CouponService;
import com.supermarket.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 定时任务：自动将过期优惠券标记为已过期
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpireScheduler {

    private final CouponService couponService;
    private final UserCouponService userCouponService;

    /**
     * 每小时执行一次，检查并标记过期的用户优惠券
     */
    @Scheduled(cron = "0 0 * * * *")
    public void expireUserCoupons() {
        LocalDateTime now = LocalDateTime.now();

        // Find all coupon templates that have expired
        List<Coupon> allCoupons = couponService.list();
        Set<Long> expiredCouponIds = allCoupons.stream()
                .filter(c -> c.getEndTime() != null && c.getEndTime().isBefore(now))
                .map(Coupon::getId)
                .collect(Collectors.toSet());

        if (expiredCouponIds.isEmpty()) {
            return;
        }

        // Find all AVAILABLE user coupons for expired coupon templates
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getStatus, CouponStatus.AVAILABLE);
        wrapper.in(UserCoupon::getCouponId, expiredCouponIds);
        List<UserCoupon> toExpire = userCouponService.list(wrapper);

        int count = 0;
        for (UserCoupon uc : toExpire) {
            uc.setStatus(CouponStatus.EXPIRED);
            userCouponService.updateById(uc);
            count++;
        }

        if (count > 0) {
            log.info("已将 {} 张过期优惠券标记为已过期", count);
        }
    }
}
