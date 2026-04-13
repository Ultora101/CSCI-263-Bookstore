package com.bookstore.dao;

import com.bookstore.exception.DatabaseException;
import com.bookstore.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Acess Object ofr User entities
 * Handles all crud operations for users in the database
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private final DatabaseManager dbManager;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * finds a user by their username
     * 
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new DatabaseException("Failed to find user", e);
        }
        return Optional.empty();
    }

    /**
     * Finds a user by ID
     * 
     * @param id the users's id
     * @return Optional containing the user if found
     */
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding by id: {}", id, e);
            throw new DatabaseException("Failed to find user", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all users from the database
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Statement stmt = dbManager.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error Fetching all users", e);
            throw new DatabaseException("Failed to retrieve users", e);
        }
        return users;
    }

    /**
     * Creates a new user in the database
     */
    public User create(User user) {
        String sql = "INSERT INTO users (username, password_hash, email, full_name, role, status) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getStatus().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
            logger.info("Created user: {}", user.getUsername());
            return user;
        } catch (SQLException e) {
            logger.error("Error creating user: {}", user.getUsername(), e);
            throw new DatabaseException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an exsidting user's details
     */
    public void update(User user) {
        String sql = "UPDATE users SET email=?, full_name=?, role=?, status=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getStatus().name());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
            logger.info("Updated user: {}", user.getId());
        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getId(), e);
            throw new DatabaseException("Failed to update user", e);
        }
    }

    /**
     * updates user's password hash
     */
    public void updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
            logger.info("Password updated for user id: {}", userId);
        } catch (SQLException e) {
            logger.error("Error updating password for user: {}", userId, e);
            throw new DatabaseException("Failed to update password", e);
        }
    }

    /**
     * Updates the last login timestamp for a user.
     */
    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Could not update last login for user: {}", userId, e);
        }
    }

    /**
     * Deletes a blocked user
     */
    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            logger.info("Deleted user id: {}", userId);
        } catch (SQLException e) {
            logger.error("Error deleting user: {}", userId, e);
            throw new DatabaseException("Failed to delete user", e);
        }
    }

    /**
     * Checks if a username already exists.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check username existence", e);
        }
    }

    /**
     * Checks if an email already exists.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check email existence", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setStatus(User.Status.valueOf(rs.getString("status")));
        String createdAt = rs.getString("created_at");
        if (createdAt != null)
            user.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        String lastLogin = rs.getString("last_login");
        if (lastLogin != null)
            user.setLastLogin(LocalDateTime.parse(lastLogin.replace(" ", "T")));

        return user;
    }
}
