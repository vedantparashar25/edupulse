package com.vityarthi.edupulse.service;

import com.vityarthi.edupulse.common.AppException;
import com.vityarthi.edupulse.common.Role;
import com.vityarthi.edupulse.model.User;
import com.vityarthi.edupulse.pattern.factory.UserFactory;
import com.vityarthi.edupulse.repository.Repository;
import com.vityarthi.edupulse.util.AsyncAuditLogger;
import com.vityarthi.edupulse.util.SecurityUtil;

import java.util.Map;
import java.util.Optional;

/**
 * Authentication and Session Management Service.
 */
public class AuthService {
    private final Repository<User, String> userRepository;
    private final AsyncAuditLogger auditLogger = AsyncAuditLogger.getInstance();
    private User currentUser = null;

    public AuthService(Repository<User, String> userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String email, String plainPassword) throws AppException.AuthenticationException {
        Optional<User> opt = userRepository.findBy(u -> u.getEmail().equalsIgnoreCase(email)).stream().findFirst();
        if (opt.isEmpty()) {
            auditLogger.log(email, "AUTH_LOGIN_FAILED", "FAIL", "User not found");
            throw new AppException.AuthenticationException("Invalid credentials: User not registered.");
        }

        User user = opt.get();
        if (!user.isActive()) {
            auditLogger.log(user.getId(), "AUTH_LOGIN_FAILED", "BLOCKED", "Account inactive");
            throw new AppException.AuthenticationException("Account is deactivated. Contact Administrator.");
        }

        boolean valid = SecurityUtil.verifyPassword(plainPassword, user.getSalt(), user.getPasswordHash());
        if (!valid) {
            auditLogger.log(user.getId(), "AUTH_LOGIN_FAILED", "FAIL", "Incorrect password attempt");
            throw new AppException.AuthenticationException("Invalid credentials: Password mismatch.");
        }

        this.currentUser = user;
        auditLogger.log(user.getId(), "AUTH_LOGIN_SUCCESS", "SUCCESS", "Logged in as " + user.getRole());
        return user;
    }

    public void logout() {
        if (currentUser != null) {
            auditLogger.log(currentUser.getId(), "AUTH_LOGOUT", "SUCCESS", "User session terminated");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User registerUser(Role role, String name, String email, String password,
                             Map<String, String> attributes) throws AppException.ValidationException, AppException.DuplicateResourceException {
        if (email == null || !email.contains("@")) {
            throw new AppException.ValidationException("Invalid email format provided: " + email);
        }

        boolean exists = userRepository.findBy(u -> u.getEmail().equalsIgnoreCase(email)).size() > 0;
        if (exists) {
            throw new AppException.DuplicateResourceException("A user with email " + email + " already exists.");
        }

        String newId = "U" + String.format("%03d", userRepository.count() + 1);
        User user = UserFactory.createUser(role, newId, name, email, password, attributes);
        userRepository.save(user);

        auditLogger.log(newId, "USER_REGISTERED", "SUCCESS", "Role: " + role + " | Email: " + email);
        return user;
    }
}
