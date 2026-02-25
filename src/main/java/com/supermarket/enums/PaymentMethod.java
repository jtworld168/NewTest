package com.supermarket.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PaymentMethod {

    WECHAT(0, "微信支付"),
    ALIPAY(1, "支付宝"),
    CASH(2, "现金"),
    CARD(3, "银行卡");

    @EnumValue
    private final int code;

    private final String description;

    PaymentMethod(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
