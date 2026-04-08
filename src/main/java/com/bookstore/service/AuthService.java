package com.bookstore.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.bookstore.dao.UserDAO;
import com.bookstore.exception.AuthenticationException;
import com.bookstore.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service used for user authentication and registration
 * uses BCrypt for password hashing
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates a user with their username and password
     * 
     * @param username the username
     * @param password the plain text password
     * @return the authenticated User
     * @throws AuthenticationException if credentials are invalid or account is
     *                                 blocked
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Username and password cannot be empty");
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            logger.warn("Login atempt with unknown username: {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOpt.get();

        if (user.getStatus() == User.Status.BLOCKED) {
            logger.warn("Blocked user attempted login: {}", username);
            throw new AuthenticationException("Your account has been blocked. Please contact support.");
        }

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());
        if (!result.verified) {
            logger.warn("Failed login attempt for user: {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        userDAO.updateLastLogin(user.getId());
        this.currentUser = user;
        logger.info("User logged in: {} ({})", username, user.getRole());
        return user;
    }

    /**
     * Registers a new customer account
     * 
     * @param username the desired username
     * @param password the plain text password
     * @param email    the user's email
     * @param fullName the user's full name
     * @return the newly created user
     * @throws AuthenticationException if registration validation fails
     */
    public User register(String username, String password, String email, String fullName) {
        validateRegistrationInput(username, password, email, fullName);

        if (userDAO.usernameExists(username)) {
            throw new AuthenticationException("Username '" + username + "' is already taken");
        }

        if (userDAO.emailExists(email)) {
            throw new AuthenticationException("Email '" + email + "' is already registered");
        }

        String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(hash);
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRole(User.Role.CUSTOMER);
        newUser.setStatus(User.Status.ACTIVE);

        User created = userDAO.create(newUser);
        logger.info("New user registered: {}", username);
        return created;
    }

    /**
     * Resets a user's password (admin function)
     */
    public void resetPassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters");
        }
        String hash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
        userDAO.updatePassword(userId, hash);
        logger.info("Password reset for user id: {}", userId);
    }

    /**
     * logs user out
     */
    public void logout() {
        logger.info("User logged out: {}", currentUser != null ? currentUser.getUsername() : "unknown");
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    private void validateRegistrationInput(String username, String password, String email, String fullName) {
        if (username == null || username.length() < 3 || username.length() > 30) {
            throw new AuthenticationException("Username must be between 3 and 30 characters");
        }
        if (!username.matches("[a-zA-Z0-9_]+")) {
            throw new AuthenticationException("Username can only contain letters, numbers, and underscores");
        }
        if (password == null || password.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters");
        }
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new AuthenticationException("Please enter a valid email address");
        }
        if (fullName == null || fullName.isBlank() || fullName.length() < 2) {
            throw new AuthenticationException("Please enter your full name");
        }
    }

}
