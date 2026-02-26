package com.supermarket.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.supermarket.annotation.RateLimit;
import com.supermarket.common.Result;
import com.supermarket.dto.LoginDTO;
import com.supermarket.dto.LoginResponseDTO;
import com.supermarket.dto.UserCreateDTO;
import com.supermarket.dto.UserDTO;
import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;
import com.supermarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户登录退出注册接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @RateLimit(key = "auth:login", maxRequests = 10, windowSeconds = 60, message = "登录尝试过于频繁，请1分钟后再试")
    public Result<LoginResponseDTO> login(@RequestBody LoginDTO param, HttpServletResponse response) {
        if (param.getUsername() == null || param.getUsername().isBlank()) {
            return Result.error("用户名不能为空");
        }
        if (param.getPassword() == null || param.getPassword().isBlank()) {
            return Result.error("密码不能为空");
        }
        User user = userService.login(param.getUsername(), param.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        response.setHeader("satoken", token);
        LoginResponseDTO result = new LoginResponseDTO();
        result.setUser(toUserDTO(user));
        result.setToken(token);
        return Result.success(result);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserCreateDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            return Result.badRequest("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            return Result.badRequest("密码不能为空");
        }
        if (userService.getUserByUsername(dto.getUsername()) != null) {
            return Result.badRequest("用户名已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        if (user.getRole() == null) {
            user.setRole(UserRole.CUSTOMER);
        }
        return userService.addUser(user) ? Result.success() : Result.error("注册失败");
    }

    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
}
