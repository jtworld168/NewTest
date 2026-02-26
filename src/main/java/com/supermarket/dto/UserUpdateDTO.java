package com.supermarket.dto;

import com.supermarket.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户更新DTO")
public class UserUpdateDTO {
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码（可选，不传则不修改）")
    private String password;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "角色")
    private UserRole role;
    @Schema(description = "是否酒店员工")
    private Boolean isHotelEmployee;
    @Schema(description = "用户头像URL")
    private String avatar;
}
