package com.supermarket.dto;

import com.supermarket.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户数据传输对象")
public class UserDTO {
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "角色")
    private UserRole role;
    @Schema(description = "是否酒店员工")
    private Boolean isHotelEmployee;
    @Schema(description = "用户头像URL")
    private String avatar;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
