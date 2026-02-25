package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supermarket.enums.PaymentMethod;
import com.supermarket.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
@Schema(description = "订单支付实体")
public class Payment {

    @Schema(description = "支付ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付方式：WECHAT-微信支付，ALIPAY-支付宝，CASH-现金，CARD-银行卡")
    private PaymentMethod paymentMethod;

    @Schema(description = "支付状态：PENDING-待支付，SUCCESS-支付成功，FAILED-支付失败，REFUNDED-已退款")
    private PaymentStatus paymentStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "交易流水号")
    private String transactionNo;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    @TableLogic
    private Integer deleted;
}
