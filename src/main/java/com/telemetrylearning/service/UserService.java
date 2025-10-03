package com.telemetrylearning.service;

import com.telemetrylearning.entity.User;
import com.telemetrylearning.repository.UserRepository;
import com.telemetrylearning.telemetry.SimpleMetricsRegistry;
import com.telemetrylearning.telemetry.TracingHelper;

import static com.telemetrylearning.telemetry.TelemetryConstants.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

/**
 * User service for business logic and operations
 * Handles user creation, validation, and management
 */
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final SimpleMetricsRegistry metrics;
    private final TracingHelper tracing;
    
    public UserService(UserRepository userRepository, SimpleMetricsRegistry metrics, TracingHelper tracing) {
        this.userRepository = userRepository;
        this.metrics = metrics;
        this.tracing = tracing;
    }
    
    public User createUser(String username, String email) {
        User result = tracing.traceOperation("user.service.create", () -> {
            logger.info("Creating user: {}", username);
            tracing.addBusinessContext("user", username, "create");
            
            // Validate input with tracing
            tracing.traceVoidOperation("user.validation", () -> {
                if (username == null || username.trim().isEmpty()) {
                    throw new IllegalArgumentException("Username cannot be empty");
                }
                if (email == null || email.trim().isEmpty()) {
                    throw new IllegalArgumentException("Email cannot be empty");
                }
            });
            
            // Create user entity
            User user = new User();
            user.setId("user-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000));
            user.setUsername(username.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setStatus(User.UserStatus.ACTIVE);
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            
            // Save to database with tracing
            tracing.traceVoidOperation("user.repository.save", () -> {
                userRepository.saveUser(user);
            });
            
            // Record metrics
            metrics.incrementCounter(USER_CREATED_TOTAL);
            tracing.recordBusinessEvent("user.created", "user", user.getId());
            
            logger.info("User created successfully: {} (ID: {})", username, user.getId());
            return user;
        });
        
        return result; // ← Perfect breakpoint location!
    }
    
    public User getUserById(String userId) {
        logger.debug("Getting user by ID: {}", userId);
        
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        
        User user = userRepository.findById(userId);
        
        if (user != null) {
            metrics.incrementCounter(USER_FOUND_TOTAL);
        }
        
        return user;
    }
    
    public List<User> getActiveUsers() {
        logger.debug("Getting all active users");
        
        List<User> users = userRepository.findActiveUsers();
        metrics.incrementCounter(USER_FOUND_TOTAL);
        
        logger.debug("Found {} active users", users.size());
        return users;
    }
    
    public User updateUserEmail(String userId, String newEmail) {
        logger.info("Updating email for user: {}", userId);
        
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        // Find existing user
        User existingUser = userRepository.findById(userId);
        if (existingUser == null) {
            throw new RuntimeException("User not found: " + userId);
        }
        
        // Update email
        existingUser.setEmail(newEmail.trim().toLowerCase());
        existingUser.setUpdatedAt(Instant.now());
        
        // Save changes
        userRepository.saveUser(existingUser);
        
        // Record metrics
        metrics.incrementCounter(USER_UPDATED_TOTAL);
        
        logger.info("Email updated for user: {} (ID: {})", existingUser.getUsername(), userId);
        return existingUser;
    }
}
