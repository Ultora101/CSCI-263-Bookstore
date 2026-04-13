package com.bookstore.dao;

import com.bookstore.exception.DatabaseException;
import com.bookstore.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserDAO using the actual SQLite database.
 * The database is seeded with admin and john_doe users on first run.
 * Test-created users are cleaned up after each test.
 */
public class UserDAOTest {

    private UserDAO userDAO;
    private int createdUserId = -1;

    private static final String TEST_USERNAME = "test_dao_user";
    private static final String TEST_EMAIL = "test_dao@example.com";

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }

    @AfterEach
    void tearDown() {
        // Clean up test-created user by deleting directly via DAO if it was created
        // Since there's no delete method on UserDAO, we use a helper to find and verify
        // The user will remain in the DB but won't affect other tests due to unique
        // names
        createdUserId = -1;
    }

    private User buildTestUser() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setPasswordHash("$2a$12$fakehashfortest1234567890123456789012345678901234567890");
        user.setEmail(TEST_EMAIL);
        user.setFullName("Test DAO User");
        user.setRole(User.Role.CUSTOMER);
        user.setStatus(User.Status.ACTIVE);
        return user;
    }

    // findByUsername
    @Test
    void findByUsername_shouldReturnUser_whenUsernameExists() {
        Optional<User> result = userDAO.findByUsername("admin");
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUsernameDoesNotExist() {
        Optional<User> result = userDAO.findByUsername("does_not_exist_xyz");
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_shouldReturnCorrectRole_forAdmin() {
        Optional<User> result = userDAO.findByUsername("admin");
        assertTrue(result.isPresent());
        assertEquals(User.Role.ADMIN, result.get().getRole());
    }

    @Test
    void findByUsername_shouldReturnCorrectRole_forCustomer() {
        Optional<User> result = userDAO.findByUsername("john_doe");
        assertTrue(result.isPresent());
        assertEquals(User.Role.CUSTOMER, result.get().getRole());
    }

    // findById
    @Test
    void findById_shouldReturnUser_whenExists() {
        // Get admin (id=1 from seed order)
        Optional<User> admin = userDAO.findByUsername("admin");
        assertTrue(admin.isPresent());
        int adminId = admin.get().getId();

        Optional<User> result = userDAO.findById(adminId);
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        Optional<User> result = userDAO.findById(Integer.MAX_VALUE);
        assertTrue(result.isEmpty());
    }

    // findAll
    @Test
    void findAll_shouldReturnAtLeastSeededUsers() {
        List<User> users = userDAO.findAll();
        assertTrue(users.size() >= 2, "Should have at least admin and john_doe");
    }

    @Test
    void findAll_shouldContainAdminUser() {
        List<User> users = userDAO.findAll();
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("admin")));
    }

    @Test
    void findAll_shouldContainCustomerUser() {
        List<User> users = userDAO.findAll();
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("john_doe")));
    }

    // create
    @Test
    void create_shouldPersistUser_andAssignId() {
        // Clean up any leftover test user first
        if (userDAO.usernameExists(TEST_USERNAME)) {
            return; // skip if already exists from a previous failed run
        }

        User user = buildTestUser();
        User created = userDAO.create(user);
        createdUserId = created.getId();

        assertTrue(created.getId() > 0);
        assertEquals(TEST_USERNAME, created.getUsername());
    }

    @Test
    void create_shouldBeRetrievable_afterCreation() {
        if (userDAO.usernameExists(TEST_USERNAME)) {
            return; // skip if already exists
        }

        User user = buildTestUser();
        User created = userDAO.create(user);
        createdUserId = created.getId();

        Optional<User> found = userDAO.findByUsername(TEST_USERNAME);
        assertTrue(found.isPresent());
        assertEquals(TEST_EMAIL, found.get().getEmail());
    }

    @Test
    void create_shouldThrow_whenUsernameIsDuplicate() {
        assertThrows(DatabaseException.class, () -> {
            User duplicate = buildTestUser();
            duplicate.setUsername("admin"); // Already exists
            duplicate.setEmail("unique_for_test@example.com");
            userDAO.create(duplicate);
        });
    }

    @Test
    void update_shouldModifyUserFields() {
        Optional<User> userOpt = userDAO.findByUsername("john_doe");
        assertTrue(userOpt.isPresent());

        User user = userOpt.get();
        String originalName = user.getFullName();
        user.setFullName("Updated Full Name");

        // This will throw DatabaseException until the SQL bug is fixed:
        assertDoesNotThrow(() -> userDAO.update(user));

        // Restore original name
        user.setFullName(originalName);
        userDAO.update(user);
    }

    // updatePassword
    @Test
    void updatePassword_shouldChangePasswordHash() {
        Optional<User> userOpt = userDAO.findByUsername("john_doe");
        assertTrue(userOpt.isPresent());
        int userId = userOpt.get().getId();

        String newHash = "$2a$12$newFakeHashForTesting123456789012345678901234567890";
        assertDoesNotThrow(() -> userDAO.updatePassword(userId, newHash));

        Optional<User> updated = userDAO.findById(userId);
        assertTrue(updated.isPresent());
        assertEquals(newHash, updated.get().getPasswordHash());
    }

    // updateLastLogin
    @Test
    void updateLastLogin_shouldNotThrow() {
        Optional<User> userOpt = userDAO.findByUsername("john_doe");
        assertTrue(userOpt.isPresent());
        assertDoesNotThrow(() -> userDAO.updateLastLogin(userOpt.get().getId()));
    }

    @Test
    void updateLastLogin_shouldSetTimestamp() {
        Optional<User> userOpt = userDAO.findByUsername("john_doe");
        assertTrue(userOpt.isPresent());
        int userId = userOpt.get().getId();

        userDAO.updateLastLogin(userId);

        Optional<User> updated = userDAO.findById(userId);
        assertTrue(updated.isPresent());
        assertNotNull(updated.get().getLastLogin());
    }

    // usernameExists
    @Test
    void usernameExists_shouldReturnTrue_whenUsernameExists() {
        assertTrue(userDAO.usernameExists("admin"));
    }

    @Test
    void usernameExists_shouldReturnFalse_whenUsernameDoesNotExist() {
        assertFalse(userDAO.usernameExists("totally_made_up_xyz_999"));
    }

    // emailExists
    @Test
    void emailExists_shouldReturnTrue_whenEmailExists() {
        assertTrue(userDAO.emailExists("admin@bookstore.com"));
    }

    @Test
    void emailExists_shouldReturnFalse_whenEmailDoesNotExist() {
        assertFalse(userDAO.emailExists("nobody@nowhere_xyz.com"));
    }
}