package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;
import com.supermarket.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
    @GetMapping("/get/{id}")
    public Result<User> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }

    @Operation(summary = "查询所有用户")
    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.listAll());
    }

    @Operation(summary = "根据角色查询用户")
    @GetMapping("/getByRole/{role}")
    public Result<List<User>> getUsersByRole(@Parameter(description = "用户角色：ADMIN/EMPLOYEE/CUSTOMER") @PathVariable UserRole role) {
        return Result.success(userService.getUsersByRole(role));
    }

    @Operation(summary = "根据用户名查询用户")
    @GetMapping("/getByUsername/{username}")
    public Result<User> getUserByUsername(@Parameter(description = "用户名") @PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }

    @Operation(summary = "模糊搜索用户（按用户名或手机号）")
    @GetMapping("/search")
    public Result<List<User>> searchUsers(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return Result.success(userService.searchUsers(keyword));
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/listPage")
    public Result<IPage<User>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "添加用户")
    @PostMapping("/add")
    public Result<Void> addUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return Result.badRequest("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.badRequest("密码不能为空");
        }
        if (user.getRole() == null) {
            return Result.badRequest("角色不能为空");
        }
        return userService.addUser(user) ? Result.success() : Result.error("添加用户失败");
    }

    @Operation(summary = "更新用户")
    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.badRequest("用户ID不能为空");
        }
        if (userService.getUserById(user.getId()) == null) {
            return Result.badRequest("用户不存在");
        }
        if (user.getUsername() != null && user.getUsername().isBlank()) {
            return Result.badRequest("用户名不能为空字符串");
        }
        if (user.getPassword() != null && user.getPassword().isBlank()) {
            return Result.badRequest("密码不能为空字符串");
        }
        return userService.updateUser(user) ? Result.success() : Result.error("更新用户失败");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        if (userService.getUserById(id) == null) {
            return Result.badRequest("用户不存在");
        }
        return userService.deleteUser(id) ? Result.success() : Result.error("删除用户失败");
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/deleteBatch")
    public Result<Void> deleteBatchUsers(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        ids.removeIf(id -> id == null);
        if (ids.isEmpty()) {
            return Result.badRequest("ID列表不能为空");
        }
        return userService.deleteBatchUsers(ids) ? Result.success() : Result.error("批量删除用户失败");
    }
}
