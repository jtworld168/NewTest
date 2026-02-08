package com.supermarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;

import java.util.List;

public interface UserService extends IService<User> {

    User getUserById(Long id);

    List<User> getUsersByRole(UserRole role);

    User getUserByUsername(String username);

    boolean addUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(Long id);

    boolean deleteBatchUsers(List<Long> ids);

    User login(String username, String password);
}
