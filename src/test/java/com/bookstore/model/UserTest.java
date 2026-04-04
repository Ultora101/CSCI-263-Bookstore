package com.bookstore.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    @BeforeEach
    void setUp() {
        createdAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        lastLogin = LocalDateTime.of(2026, 3, 15, 9, 30);

        user = new User(1, "Ultora", "hashpassword123", "test@example.com", "Ultora User", User.Role.CUSTOMER,
                User.Status.ACTIVE, createdAt, lastLogin);
    }

    // Testing the Constructor
    @Test
    void fullConstructor_shouldSetAllCorrectly() {
        assertEquals(1, user.getId());
        assertEquals("Ultora", user.getUsername());
        assertEquals("hashpassword123", user.getPasswordHash());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Ultora User", user.getFullName());
        assertEquals(User.Role.CUSTOMER, user.getRole());
        assertEquals(User.Status.ACTIVE, user.getStatus());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(lastLogin, user.getLastLogin());
    }

    // Null checker of constructor
    @Test
    void noArgsToConstructor_shouldCreateUserWithNullFields() {
        User emptyUser = new User();
        assertEquals(0, emptyUser.getId());
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getRole());
        assertNull(emptyUser.getStatus());
    }

    // Testing Setters
    @Test
    void setId_shouldUpdateId() {
        user.setId(99);
        assertEquals(99, user.getId());
    }

    @Test
    void setUsername_shouldUpdateUsername() {
        user.setUsername("new_username");
        assertEquals("new_username", user.getUsername());
    }

    @Test
    void setPasswordHash_shouldUpdatePasswordHash() {
        user.setPasswordHash("newPasswordHash321");
        assertEquals("newPasswordHash321", user.getPasswordHash());
    }

    @Test
    void setEmail_shouldUpdateEmail() {
        user.setEmail("newemail@test.com");
        assertEquals("newemail@test.com", user.getEmail());
    }

    @Test
    void setFullName_shouldUpdateFullName() {
        user.setFullName("Full Name");
        assertEquals("Full Name", user.getFullName());
    }

    @Test
    void setRole_shouldUpdateRole() {
        user.setRole(User.Role.ADMIN);
        assertEquals(User.Role.ADMIN, user.getRole());
    }

    @Test
    void setStatus_shouldUpdateStatus() {
        user.setStatus(User.Status.BLOCKED);
        assertEquals(User.Status.BLOCKED, user.getStatus());
    }

    @Test
    void setLastLogin_shouldUpdateLastLogin() {
        LocalDateTime newLogin = LocalDateTime.of(2025, 4, 1, 10, 0);
        user.setLastLogin(newLogin);
        assertEquals(newLogin, user.getLastLogin());
    }

    // Testing isAdmin
    @Test
    void isAdmin_shouldReturnFalse_whenRoleIsCustomer() {
        user.setRole(User.Role.CUSTOMER);
        assertFalse(user.isAdmin());
    }

    @Test
    void isAdmin_shouldReturnTrue_whenRoleIsAdmin() {
        user.setRole(User.Role.ADMIN);
        assertTrue(user.isAdmin());
    }

    @Test
    void isAdmin_shouldReturnFalse_whenRoleIsNull() {
        user.setRole(null);
        assertFalse(user.isAdmin());
    }

    // Testing isActive
    @Test
    void isActive_shouldReturnTrue_whenStatusIsActive() {
        user.setStatus(User.Status.ACTIVE);
        assertTrue(user.isActive());
    }

    @Test
    void isActive_shouldReturnFalse_whenStatusIsBlocked() {
        user.setStatus(User.Status.BLOCKED);
        assertFalse(user.isActive());
    }

    @Test
    void isActive_shouldReturnFalse_whenStatusIsNull() {
        user.setStatus(null);
        assertFalse(user.isActive());
    }

    // Tesing toString
    @Test
    void toString_shoudlContainIdAndUsername() {
        String result = user.toString();
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Ultora"));
    }

    @Test
    void toString_shouldContainRoleAndStatus() {
        String result = user.toString();
        assertTrue(result.contains("CUSTOMER"));
        assertTrue(result.contains("ACTIVE"));
    }

    // Testing Enums
    @Test
    void role_enumShouldHaveExactlyTwoValues() {
        assertEquals(2, User.Role.values().length);
    }

    @Test
    void status_enumsShouldHaveExactlyTwoValues() {
        assertEquals(2, User.Status.values().length);
    }
}
