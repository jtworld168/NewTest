package com.supermarket;

import com.supermarket.entity.User;
import com.supermarket.enums.UserRole;
import com.supermarket.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testAddAndGetUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setPhone("13800000001");
        user.setRole(UserRole.CUSTOMER);

        assertTrue(userService.addUser(user));
        assertNotNull(user.getId());

        User found = userService.getUserById(user.getId());
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals(UserRole.CUSTOMER, found.getRole());
    }

    @Test
    void testGetUserByUsername() {
        User user = new User();
        user.setUsername("admin_test");
        user.setPassword("admin123");
        user.setRole(UserRole.ADMIN);
        userService.addUser(user);

        User found = userService.getUserByUsername("admin_test");
        assertNotNull(found);
        assertEquals(UserRole.ADMIN, found.getRole());
    }

    @Test
    void testGetUsersByRole() {
        User emp1 = new User();
        emp1.setUsername("emp1");
        emp1.setPassword("pass");
        emp1.setRole(UserRole.EMPLOYEE);
        userService.addUser(emp1);

        User emp2 = new User();
        emp2.setUsername("emp2");
        emp2.setPassword("pass");
        emp2.setRole(UserRole.EMPLOYEE);
        userService.addUser(emp2);

        List<User> employees = userService.getUsersByRole(UserRole.EMPLOYEE);
        assertTrue(employees.size() >= 2);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setUsername("update_test");
        user.setPassword("old_pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);

        user.setPhone("13900000000");
        assertTrue(userService.updateUser(user));

        User updated = userService.getUserById(user.getId());
        assertEquals("13900000000", updated.getPhone());
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setUsername("delete_test");
        user.setPassword("pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);

        assertTrue(userService.deleteUser(user.getId()));

        User deleted = userService.getUserById(user.getId());
        assertNull(deleted);
    }

    @Test
    void testLoginSuccess() {
        User user = new User();
        user.setUsername("login_user");
        user.setPassword("login_pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);

        User loggedIn = userService.login("login_user", "login_pass");
        assertNotNull(loggedIn);
        assertEquals("login_user", loggedIn.getUsername());
    }

    @Test
    void testLoginWrongPassword() {
        User user = new User();
        user.setUsername("login_user2");
        user.setPassword("correct_pass");
        user.setRole(UserRole.CUSTOMER);
        userService.addUser(user);

        User loggedIn = userService.login("login_user2", "wrong_pass");
        assertNull(loggedIn);
    }

    @Test
    void testDeleteBatchUsers() {
        User u1 = new User();
        u1.setUsername("batch_del_user1");
        u1.setPassword("pass");
        u1.setRole(UserRole.CUSTOMER);
        userService.addUser(u1);

        User u2 = new User();
        u2.setUsername("batch_del_user2");
        u2.setPassword("pass");
        u2.setRole(UserRole.CUSTOMER);
        userService.addUser(u2);

        assertTrue(userService.deleteBatchUsers(List.of(u1.getId(), u2.getId())));

        assertNull(userService.getUserById(u1.getId()));
        assertNull(userService.getUserById(u2.getId()));
    }
}
