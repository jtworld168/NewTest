package com.supermarket.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.supermarket.common.LoginParam;
import com.supermarket.common.Result;
import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;
import com.supermarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证管理", description = "用户登录退出注册接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Object> login(@RequestBody LoginParam param, HttpServletResponse response) {
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
        user.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);
        return Result.success(result);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return Result.badRequest("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.badRequest("密码不能为空");
        }
        if (userService.getUserByUsername(user.getUsername()) != null) {
            return Result.badRequest("用户名已存在");
        }
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
