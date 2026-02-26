package com.supermarket.dto;

import com.supermarket.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户创建DTO")
public class UserCreateDTO {
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "角色：ADMIN/EMPLOYEE/CUSTOMER")
    private UserRole role;
    @Schema(description = "是否酒店员工")
    private Boolean isHotelEmployee;
    @Schema(description = "用户头像URL")
    private String avatar;
}
