package com.supermarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应DTO")
public class LoginResponseDTO {
    @Schema(description = "用户信息")
    private UserDTO user;
    @Schema(description = "认证令牌")
    private String token;
}
