package com.supermarket.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.supermarket.common.LoginParam;
import com.supermarket.common.Result;
import com.supermarket.entity.User;
import com.supermarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户登录退出接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginParam param) {
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
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
}
