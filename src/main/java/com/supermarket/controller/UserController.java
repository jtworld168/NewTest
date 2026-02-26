package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.UserCreateDTO;
import com.supermarket.dto.UserDTO;
import com.supermarket.dto.UserUpdateDTO;
import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;
import com.supermarket.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "用户管理", description = "用户增删改查接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/get/{id}")
    public Result<UserDTO> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? Result.success(toDTO(user)) : Result.error("用户不存在");
    }

    @Operation(summary = "查询所有用户")
    @GetMapping("/list")
    public Result<List<UserDTO>> getAllUsers() {
        return Result.success(userService.listAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @Operation(summary = "根据角色查询用户")
    @GetMapping("/getByRole/{role}")
    public Result<List<UserDTO>> getUsersByRole(@Parameter(description = "用户角色：ADMIN/EMPLOYEE/CUSTOMER") @PathVariable UserRole role) {
        return Result.success(userService.getUsersByRole(role).stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @Operation(summary = "根据用户名查询用户")
    @GetMapping("/getByUsername/{username}")
    public Result<UserDTO> getUserByUsername(@Parameter(description = "用户名") @PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return user != null ? Result.success(toDTO(user)) : Result.error("用户不存在");
    }

    @Operation(summary = "模糊搜索用户（按用户名或手机号）")
    @GetMapping("/search")
    public Result<List<UserDTO>> searchUsers(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return Result.success(userService.searchUsers(keyword).stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/listPage")
    public Result<IPage<UserDTO>> listPage(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（默认10）") @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<User> page = userService.listPage(pageNum, pageSize);
        IPage<UserDTO> dtoPage = page.convert(this::toDTO);
        return Result.success(dtoPage);
    }

    @Operation(summary = "添加用户")
    @PostMapping("/add")
    public Result<Void> addUser(@RequestBody UserCreateDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            return Result.badRequest("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            return Result.badRequest("密码不能为空");
        }
        if (dto.getRole() == null) {
            return Result.badRequest("角色不能为空");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        return userService.addUser(user) ? Result.success() : Result.error("添加用户失败");
    }

    @Operation(summary = "更新用户")
    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody UserUpdateDTO dto) {
        if (dto.getId() == null) {
            return Result.badRequest("用户ID不能为空");
        }
        if (userService.getUserById(dto.getId()) == null) {
            return Result.badRequest("用户不存在");
        }
        if (dto.getUsername() != null && dto.getUsername().isBlank()) {
            return Result.badRequest("用户名不能为空字符串");
        }
        if (dto.getPassword() != null && dto.getPassword().isBlank()) {
            return Result.badRequest("密码不能为空字符串");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
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
