package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;
import com.supermarket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户增删改查接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<User> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }

    @Operation(summary = "查询所有用户")
    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.list());
    }

    @Operation(summary = "根据角色查询用户")
    @GetMapping("/role/{role}")
    public Result<List<User>> getUsersByRole(@Parameter(description = "用户角色：ADMIN/EMPLOYEE/CUSTOMER") @PathVariable UserRole role) {
        return Result.success(userService.getUsersByRole(role));
    }

    @Operation(summary = "添加用户")
    @PostMapping
    public Result<Void> addUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.error("密码不能为空");
        }
        if (user.getRole() == null) {
            return Result.error("角色不能为空");
        }
        return userService.addUser(user) ? Result.success() : Result.error("添加用户失败");
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public Result<Void> updateUser(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        return userService.updateUser(user) ? Result.success() : Result.error("更新用户失败");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.deleteUser(id) ? Result.success() : Result.error("删除用户失败");
    }
}
