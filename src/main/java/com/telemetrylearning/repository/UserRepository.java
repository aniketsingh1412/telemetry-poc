package com.telemetrylearning.repository;

import com.telemetrylearning.entity.User;
import com.telemetrylearning.telemetry.SimpleMetricsRegistry;

import static com.telemetrylearning.telemetry.TelemetryConstants.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * User repository for database operations
 * Handles all user-related data access using plain JDBC
 */
public class UserRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    
    private final Connection connection;
    private final SimpleMetricsRegistry metrics;
    
    public UserRepository(Connection connection, SimpleMetricsRegistry metrics) {
        this.connection = connection;
        this.metrics = metrics;
    }
    
    public void saveUser(User user) {
        String sql = "INSERT INTO users (id, username, email, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "email = VALUES(email), status = VALUES(status), updated_at = VALUES(updated_at)";
        
        long startTime = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getStatus().name());
            stmt.setTimestamp(5, Timestamp.from(user.getCreatedAt()));
            stmt.setTimestamp(6, Timestamp.from(user.getUpdatedAt()));
            
            int rowsAffected = stmt.executeUpdate();
            
            metrics.recordDuration(DATABASE_OPERATION_DURATION, 
                System.currentTimeMillis() - startTime, "saveUser");
            metrics.incrementCounter(DATABASE_OPERATIONS_TOTAL);
            
            logger.debug("Saved user: {} (rows affected: {})", user.getId(), rowsAffected);
            
        } catch (SQLException e) {
            metrics.recordError("database", e);
            throw new RuntimeException("Failed to save user: " + user.getId(), e);
        }
    }
    
    public List<User> findActiveUsers() {
        String sql = "SELECT id, username, email, status, created_at, updated_at " +
                    "FROM users WHERE status = 'ACTIVE' ORDER BY created_at DESC";
        
        List<User> users = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setStatus(User.UserStatus.valueOf(rs.getString("status")));
                user.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                user.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                users.add(user);
            }
            
            metrics.recordDuration(DATABASE_OPERATION_DURATION, 
                System.currentTimeMillis() - startTime, "findActiveUsers");
            metrics.incrementCounter(DATABASE_OPERATIONS_TOTAL);
            
            logger.debug("Found {} active users", users.size());
            
        } catch (SQLException e) {
            metrics.recordError("database", e);
            throw new RuntimeException("Failed to find active users", e);
        }
        
        return users;
    }
    
    public User findById(String userId) {
        String sql = "SELECT id, username, email, status, created_at, updated_at " +
                    "FROM users WHERE id = ?";
        
        long startTime = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setStatus(User.UserStatus.valueOf(rs.getString("status")));
                    user.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    user.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    
                    metrics.recordDuration(DATABASE_OPERATION_DURATION, 
                        System.currentTimeMillis() - startTime, "findById");
                    metrics.incrementCounter(DATABASE_OPERATIONS_TOTAL);
                    
                    return user;
                }
            }
            
        } catch (SQLException e) {
            metrics.recordError("database", e);
            throw new RuntimeException("Failed to find user: " + userId, e);
        }
        
        return null;
    }
}
