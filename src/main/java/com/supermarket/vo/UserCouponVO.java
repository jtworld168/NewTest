package com.supermarket.vo;

import com.supermarket.entity.UserCoupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户优惠券视图对象")
public class UserCouponVO extends UserCoupon {

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "优惠券名称")
    private String couponName;
}
