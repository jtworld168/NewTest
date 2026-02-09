package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
@Schema(description = "优惠券面额实体（优惠券模板）")
public class Coupon {

    @Schema(description = "优惠券ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "折扣金额")
    private BigDecimal discount;

    @Schema(description = "最低使用金额")
    private BigDecimal minAmount;

    @Schema(description = "发放总数量")
    private Integer totalCount;

    @Schema(description = "剩余可领取数量")
    private Integer remainingCount;

    @Schema(description = "生效时间")
    private LocalDateTime startTime;

    @Schema(description = "过期时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    @TableLogic
    private Integer deleted;
}
