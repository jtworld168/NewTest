package com.supermarket.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CouponStatus {

    AVAILABLE(0, "可用"),
    USED(1, "已使用"),
    EXPIRED(2, "已过期");

    @EnumValue
    private final int code;

    @JsonValue
    private final String description;

    CouponStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
