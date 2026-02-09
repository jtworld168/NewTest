package com.supermarket.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PaymentStatus {

    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败"),
    REFUNDED(3, "已退款");

    @EnumValue
    private final int code;

    @JsonValue
    private final String description;

    PaymentStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
