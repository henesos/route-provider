package com.aviation.routeprovider.domain.model.entity;

import com.aviation.routeprovider.domain.exception.DomainException;
import com.aviation.routeprovider.domain.model.valueobject.UserRole;

import java.util.Objects;

public class User {
    
    private Long id;
    private String username;
    private String passwordHash;
    private UserRole role;

    private User(Long id, String username, String passwordHash, UserRole role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static User create(String username, String passwordHash, UserRole role) {
        validate(username, passwordHash, role);
        return new User(null, username.trim(), passwordHash, role);
    }

    public static User reconstruct(Long id, String username, 
                                    String passwordHash, UserRole role) {
        if (id == null) {
            throw new DomainException("ID is required for reconstruction");
        }
        validate(username, passwordHash, role);
        return new User(id, username, passwordHash, role);
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }
    

    public boolean canManageLocations() {
        return role.canManageLocations();
    }

    public boolean canManageTransportations() {
        return role.canManageTransportations();
    }

    public boolean canSearchRoutes() {
        return role.canSearchRoutes();
    }
    
    // Getters
    
    public Long getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    // Validation
    
    private static void validate(String username, String passwordHash, UserRole role) {
        if (username == null || username.isBlank()) {
            throw new DomainException("Username is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("Password hash is required");
        }
        if (role == null) {
            throw new DomainException("User role is required");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new DomainException(
                "Username must be between 3 and 50 characters");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        if (id != null && user.id != null) {
            return Objects.equals(id, user.id);
        }
        return Objects.equals(username, user.username);
    }
    
    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(username);
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s}",
            id, username, role);
    }
}
