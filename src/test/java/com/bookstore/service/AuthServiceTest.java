package com.bookstore.service;

import com.bookstore.dao.UserDAO;
import com.bookstore.exception.AuthenticationException;
import com.bookstore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserDAO mockUserDAO;

    private AuthService authService;

    private User activeCustomer;
    private User blockedCustomer;
    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        authService = new AuthService();

        // Inject mock DAO via reflection
        Field field = AuthService.class.getDeclaredField("userDAO");
        field.setAccessible(true);
        field.set(authService, mockUserDAO);

        // BCrypt hash for "password123"
        String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                .hashToString(12, "password123".toCharArray());

        activeCustomer = new User();
        activeCustomer.setId(1);
        activeCustomer.setUsername("john_doe");
        activeCustomer.setPasswordHash(hash);
        activeCustomer.setEmail("john@example.com");
        activeCustomer.setFullName("John Doe");
        activeCustomer.setRole(User.Role.CUSTOMER);
        activeCustomer.setStatus(User.Status.ACTIVE);

        blockedCustomer = new User();
        blockedCustomer.setId(2);
        blockedCustomer.setUsername("blocked_user");
        blockedCustomer.setPasswordHash(hash);
        blockedCustomer.setEmail("blocked@example.com");
        blockedCustomer.setFullName("Blocked User");
        blockedCustomer.setRole(User.Role.CUSTOMER);
        blockedCustomer.setStatus(User.Status.BLOCKED);

        adminUser = new User();
        adminUser.setId(3);
        adminUser.setUsername("admin");
        adminUser.setPasswordHash(hash);
        adminUser.setEmail("admin@bookstore.com");
        adminUser.setFullName("System Administrator");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setStatus(User.Status.ACTIVE);
    }

    // login
    @Test
    void login_shouldReturnUser_whenCredentialsAreValid() {
        when(mockUserDAO.findByUsername("john_doe")).thenReturn(Optional.of(activeCustomer));
        User result = authService.login("john_doe", "password123");
        assertEquals("john_doe", result.getUsername());
        assertEquals(User.Role.CUSTOMER, result.getRole());
    }

    @Test
    void login_shouldSetCurrentUser_onSuccess() {
        when(mockUserDAO.findByUsername("john_doe")).thenReturn(Optional.of(activeCustomer));
        authService.login("john_doe", "password123");
        assertTrue(authService.isLoggedIn());
        assertEquals("john_doe", authService.getCurrentUser().getUsername());
    }

    @Test
    void login_shouldThrow_whenUsernameIsEmpty() {
        assertThrows(AuthenticationException.class, () -> authService.login("", "password123"));
    }

    @Test
    void login_shouldThrow_whenPasswordIsEmpty() {
        assertThrows(AuthenticationException.class, () -> authService.login("john_doe", ""));
    }

    @Test
    void login_shouldThrow_whenUsernameIsNull() {
        assertThrows(AuthenticationException.class, () -> authService.login(null, "password123"));
    }

    @Test
    void login_shouldThrow_whenPasswordIsNull() {
        assertThrows(AuthenticationException.class, () -> authService.login("john_doe", null));
    }

    @Test
    void login_shouldThrow_whenUsernameDoesNotExist() {
        when(mockUserDAO.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> authService.login("nobody", "password123"));
    }

    @Test
    void login_shouldThrow_whenPasswordIsWrong() {
        when(mockUserDAO.findByUsername("john_doe")).thenReturn(Optional.of(activeCustomer));
        assertThrows(AuthenticationException.class, () -> authService.login("john_doe", "wrongpassword"));
    }

    @Test
    void login_shouldThrow_whenAccountIsBlocked() {
        when(mockUserDAO.findByUsername("blocked_user")).thenReturn(Optional.of(blockedCustomer));
        assertThrows(AuthenticationException.class, () -> authService.login("blocked_user", "password123"));
    }

    @Test
    void login_shouldWorkForAdminUser() {
        when(mockUserDAO.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        User result = authService.login("admin", "password123");
        assertEquals(User.Role.ADMIN, result.getRole());
        assertTrue(authService.isAdmin());
    }

    // register
    @Test
    void register_shouldCreateAndReturnUser_whenInputIsValid() {
        when(mockUserDAO.usernameExists("new_user")).thenReturn(false);
        when(mockUserDAO.emailExists("new@example.com")).thenReturn(false);

        User created = new User();
        created.setId(10);
        created.setUsername("new_user");
        when(mockUserDAO.create(any(User.class))).thenReturn(created);

        User result = authService.register("new_user", "securePass", "new@example.com", "New User");
        assertEquals("new_user", result.getUsername());
        verify(mockUserDAO).create(any(User.class));
    }

    @Test
    void register_shouldThrow_whenUsernameTooShort() {
        assertThrows(AuthenticationException.class,
                () -> authService.register("ab", "securePass", "x@example.com", "Full Name"));
    }

    @Test
    void register_shouldThrow_whenUsernameTooLong() {
        String longName = "a".repeat(31);
        assertThrows(AuthenticationException.class,
                () -> authService.register(longName, "securePass", "x@example.com", "Full Name"));
    }

    @Test
    void register_shouldThrow_whenUsernameHasInvalidChars() {
        assertThrows(AuthenticationException.class,
                () -> authService.register("bad name!", "securePass", "x@example.com", "Full Name"));
    }

    @Test
    void register_shouldThrow_whenPasswordTooShort() {
        assertThrows(AuthenticationException.class,
                () -> authService.register("valid_user", "abc", "x@example.com", "Full Name"));
    }

    @Test
    void register_shouldThrow_whenEmailIsInvalid() {
        assertThrows(AuthenticationException.class,
                () -> authService.register("valid_user", "securePass", "notanemail", "Full Name"));
    }

    @Test
    void register_shouldThrow_whenFullNameIsBlank() {
        assertThrows(AuthenticationException.class,
                () -> authService.register("valid_user", "securePass", "x@example.com", "  "));
    }

    @Test
    void register_shouldThrow_whenUsernameAlreadyTaken() {
        when(mockUserDAO.usernameExists("john_doe")).thenReturn(true);
        assertThrows(AuthenticationException.class,
                () -> authService.register("john_doe", "securePass", "x@example.com", "Full Name"));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyRegistered() {
        when(mockUserDAO.usernameExists("new_user")).thenReturn(false);
        when(mockUserDAO.emailExists("john@example.com")).thenReturn(true);
        assertThrows(AuthenticationException.class,
                () -> authService.register("new_user", "securePass", "john@example.com", "Full Name"));
    }

    // resetPassword
    @Test
    void resetPassword_shouldCallUpdatePassword_whenPasswordIsValid() {
        doNothing().when(mockUserDAO).updatePassword(anyInt(), anyString());
        assertDoesNotThrow(() -> authService.resetPassword(1, "newSecurePass"));
        verify(mockUserDAO).updatePassword(eq(1), anyString());
    }

    @Test
    void resetPassword_shouldThrow_whenPasswordIsTooShort() {
        assertThrows(AuthenticationException.class, () -> authService.resetPassword(1, "abc"));
    }

    @Test
    void resetPassword_shouldThrow_whenPasswordIsNull() {
        assertThrows(AuthenticationException.class, () -> authService.resetPassword(1, null));
    }

    // logout
    @Test
    void logout_shouldClearCurrentUser() {
        when(mockUserDAO.findByUsername("john_doe")).thenReturn(Optional.of(activeCustomer));
        authService.login("john_doe", "password123");
        authService.logout();
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }

    // isAdmin
    @Test
    void isAdmin_shouldReturnFalse_whenNotLoggedIn() {
        assertFalse(authService.isAdmin());
    }

    @Test
    void isAdmin_shouldReturnFalse_whenLoggedInAsCustomer() {
        when(mockUserDAO.findByUsername("john_doe")).thenReturn(Optional.of(activeCustomer));
        authService.login("john_doe", "password123");
        assertFalse(authService.isAdmin());
    }

    @Test
    void isAdmin_shouldReturnTrue_whenLoggedInAsAdmin() {
        when(mockUserDAO.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        authService.login("admin", "password123");
        assertTrue(authService.isAdmin());
    }
}