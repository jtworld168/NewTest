package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.enums.CouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coupon")
@Schema(description = "用户优惠券实体（用户拥有的具体优惠券）")
public class UserCoupon {

    @Schema(description = "用户优惠券ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "优惠券面额ID")
    private Long couponId;

    @Schema(description = "状态：AVAILABLE-可用，USED-已使用，EXPIRED-已过期")
    private CouponStatus status;

    @Schema(description = "使用时间")
    private LocalDateTime useTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    @TableLogic
    private Integer deleted;
}
