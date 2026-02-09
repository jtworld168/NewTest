package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
@Schema(description = "订单实体")
public class Order {

    @Schema(description = "订单ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "下单时单价（已计算员工折扣）")
    private BigDecimal priceAtPurchase;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "用户优惠券ID（关联用户优惠券表）")
    private Long userCouponId;

    @Schema(description = "订单状态：PENDING-待支付，PAID-已支付，COMPLETED-已完成，CANCELLED-已取消")
    private OrderStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    @TableLogic
    private Integer deleted;
}
