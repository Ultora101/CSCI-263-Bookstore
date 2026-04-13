package com.bookstore.service;

import com.bookstore.dao.UserDAO;
import com.bookstore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDAO mockUserDAO;

    private UserService userService;
    private User customerUser;
    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserService();

        Field field = UserService.class.getDeclaredField("userDAO");
        field.setAccessible(true);
        field.set(userService, mockUserDAO);

        customerUser = new User();
        customerUser.setId(1);
        customerUser.setUsername("john_doe");
        customerUser.setEmail("john@example.com");
        customerUser.setFullName("John Doe");
        customerUser.setRole(User.Role.CUSTOMER);
        customerUser.setStatus(User.Status.ACTIVE);

        adminUser = new User();
        adminUser.setId(2);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@bookstore.com");
        adminUser.setFullName("System Administrator");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setStatus(User.Status.ACTIVE);
    }

    // getAllUsers
    @Test
    void getAllUsers_shouldReturnAllUsersFromDAO() {
        when(mockUserDAO.findAll()).thenReturn(List.of(customerUser, adminUser));
        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
        verify(mockUserDAO).findAll();
    }

    // getUserByID
    @Test
    void getUserByID_shouldReturnUser_whenFound() {
        when(mockUserDAO.findById(1)).thenReturn(Optional.of(customerUser));
        User result = userService.getUserByID(1);
        assertEquals("john_doe", result.getUsername());
    }

    @Test
    void getUserByID_shouldThrow_whenNotFound() {
        when(mockUserDAO.findById(999)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByID(999));
    }

    // blockUser
    @Test
    void blockUser_shouldSetStatusToBlocked_forCustomer() {
        when(mockUserDAO.findById(1)).thenReturn(Optional.of(customerUser));
        doNothing().when(mockUserDAO).update(any(User.class));
        userService.blockUser(1);
        assertEquals(User.Status.BLOCKED, customerUser.getStatus());
        verify(mockUserDAO).update(customerUser);
    }

    @Test
    void blockUser_shouldThrow_whenUserIsAdmin() {
        when(mockUserDAO.findById(2)).thenReturn(Optional.of(adminUser));
        assertThrows(IllegalStateException.class, () -> userService.blockUser(2));
        verify(mockUserDAO, never()).update(any());
    }

    @Test
    void blockUser_shouldThrow_whenUserNotFound() {
        when(mockUserDAO.findById(999)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.blockUser(999));
    }

    // unblockUser
    @Test
    void unblockUser_shouldSetStatusToActive() {
        customerUser.setStatus(User.Status.BLOCKED);
        when(mockUserDAO.findById(1)).thenReturn(Optional.of(customerUser));
        doNothing().when(mockUserDAO).update(any(User.class));
        userService.unblockUser(1);
        assertEquals(User.Status.ACTIVE, customerUser.getStatus());
        verify(mockUserDAO).update(customerUser);
    }

    @Test
    void unblockUser_shouldThrow_whenUserNotFound() {
        when(mockUserDAO.findById(999)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.unblockUser(999));
    }

    // promoteToAdmin
    @Test
    void promoteToAdmin_shouldSetRoleToAdmin() {
        when(mockUserDAO.findById(1)).thenReturn(Optional.of(customerUser));
        doNothing().when(mockUserDAO).update(any(User.class));
        userService.promoteToAdmin(1);
        assertEquals(User.Role.ADMIN, customerUser.getRole());
        verify(mockUserDAO).update(customerUser);
    }

    @Test
    void promoteToAdmin_shouldThrow_whenUserNotFound() {
        when(mockUserDAO.findById(999)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.promoteToAdmin(999));
    }
}