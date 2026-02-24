package com.supermarket.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CouponStatus {

    AVAILABLE(0, "可用"),
    USED(1, "已使用"),
    EXPIRED(2, "已过期"),
    LOCKED(3, "已锁定");

    @EnumValue
    private final int code;

    private final String description;

    CouponStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
