package com.supermarket.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserRole {

    ADMIN(0, "管理员"),
    EMPLOYEE(1, "员工"),
    CUSTOMER(2, "顾客");

    @EnumValue
    private final int code;

    private final String description;

    UserRole(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
