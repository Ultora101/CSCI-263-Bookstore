package com.bookstore.service;

import com.bookstore.dao.UserDAO;
import com.bookstore.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service for admin user managemnt operations
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public User getUserByID(int id) {
        return userDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public void blockUser(int userId) {
        User user = getUserByID(userId);
        if (user.isAdmin())
            throw new IllegalStateException("Cannot block an admin account");
        user.setStatus(User.Status.BLOCKED);
        userDAO.update(user);
        logger.info("User {} blocked", userId);
    }

    public void unblockUser(int userId) {
        User user = getUserByID(userId);
        user.setStatus(User.Status.ACTIVE);
        userDAO.update(user);
        logger.info("User {} unblocked", userId);
    }

    public void promoteToAdmin(int userId) {
        User user = getUserByID(userId);
        user.setRole(User.Role.ADMIN);
        userDAO.update(user);
        logger.info("User {} promoted to ADMIN", userId);
    }
}
