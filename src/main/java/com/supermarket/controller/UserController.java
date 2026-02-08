package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;
import com.supermarket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }

    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.list());
    }

    @GetMapping("/role/{role}")
    public Result<List<User>> getUsersByRole(@PathVariable UserRole role) {
        return Result.success(userService.getUsersByRole(role));
    }

    @PostMapping
    public Result<Void> addUser(@RequestBody User user) {
        return userService.addUser(user) ? Result.success() : Result.error("添加用户失败");
    }

    @PutMapping
    public Result<Void> updateUser(@RequestBody User user) {
        return userService.updateUser(user) ? Result.success() : Result.error("更新用户失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id) ? Result.success() : Result.error("删除用户失败");
    }
}
