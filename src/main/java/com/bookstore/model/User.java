package com.bookstore.model;

import java.time.LocalDateTime;

/**
 * This helps use and create a user for the bookstore
 * Users can be either a customer or an admin
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class User {

    public enum Role {
        CUSTOMER, ADMIN
    }

    public enum Status {
        ACTIVE, BLOCKED
    }

    // users need an id, username, passwordHash (getting help understanding hashes
    // and using them for security),
    // email, fullName, role, status, createdAt, lastLogin
    private int id;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private Role role;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public User() {
    }

    public User(int id, String username, String passwordHash, String email, String fullName, Role role, Status status,
            LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

}
