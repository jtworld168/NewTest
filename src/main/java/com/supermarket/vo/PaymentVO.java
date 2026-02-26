package com.supermarket.vo;

import com.supermarket.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "支付视图对象")
public class PaymentVO extends Payment {

    @Schema(description = "订单用户名")
    private String userName;

    @Schema(description = "订单总金额描述")
    private String orderInfo;
}
