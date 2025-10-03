package com.telemetrylearning.repository;

import com.telemetrylearning.entity.Order;
import com.telemetrylearning.telemetry.SimpleMetricsRegistry;

import static com.telemetrylearning.telemetry.TelemetryConstants.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Order repository for database operations
 * Handles all order-related data access using plain JDBC
 */
public class OrderRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);
    
    private final Connection connection;
    private final SimpleMetricsRegistry metrics;
    
    public OrderRepository(Connection connection, SimpleMetricsRegistry metrics) {
        this.connection = connection;
        this.metrics = metrics;
    }
    
    public void saveOrder(Order order) {
        String sql = "INSERT INTO orders (id, customer_id, amount, currency, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "amount = VALUES(amount), currency = VALUES(currency), status = VALUES(status), updated_at = VALUES(updated_at)";
        
        long startTime = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, order.getId());
            stmt.setString(2, order.getCustomerId());
            stmt.setBigDecimal(3, order.getAmount());
            stmt.setString(4, order.getCurrency());
            stmt.setString(5, order.getStatus().name());
            stmt.setTimestamp(6, Timestamp.from(order.getCreatedAt()));
            stmt.setTimestamp(7, Timestamp.from(order.getUpdatedAt()));
            
            int rowsAffected = stmt.executeUpdate();
            
            metrics.recordDuration(DATABASE_OPERATION_DURATION, 
                System.currentTimeMillis() - startTime, "saveOrder");
            metrics.incrementCounter(DATABASE_OPERATIONS_TOTAL);
            
            logger.debug("Saved order: {} (rows affected: {})", order.getId(), rowsAffected);
            
        } catch (SQLException e) {
            metrics.recordError("database", e);
            throw new RuntimeException("Failed to save order: " + order.getId(), e);
        }
    }
    
    public List<Order> findOrdersByCustomer(String customerId) {
        String sql = "SELECT id, customer_id, amount, currency, status, created_at, updated_at " +
                    "FROM orders WHERE customer_id = ? ORDER BY created_at DESC";
        
        List<Order> orders = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getString("id"));
                    order.setCustomerId(rs.getString("customer_id"));
                    order.setAmount(rs.getBigDecimal("amount"));
                    order.setCurrency(rs.getString("currency"));
                    order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                    order.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    order.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    orders.add(order);
                }
            }
            
            metrics.recordDuration(DATABASE_OPERATION_DURATION, 
                System.currentTimeMillis() - startTime, "findOrdersByCustomer");
            metrics.incrementCounter(DATABASE_OPERATIONS_TOTAL);
            
            logger.debug("Found {} orders for customer: {}", orders.size(), customerId);
            
        } catch (SQLException e) {
            metrics.recordError("database", e);
            throw new RuntimeException("Failed to find orders for customer: " + customerId, e);
        }
        
        return orders;
    }
    
    public void updateOrderStatus(String orderId, Order.OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";
        
        long startTime = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setTimestamp(2, Timestamp.from(Instant.now()));
            stmt.setString(3, orderId);
            
            int rowsAffected = stmt.executeUpdate();
            
            metrics.recordDuration(DATABASE_OPERATION_DURATION, 
                System.currentTimeMillis() - startTime, "updateOrderStatus");
            metrics.incrementCounter(DATABASE_OPERATIONS_TOTAL);
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Order not found: " + orderId);
            }
            
            logger.debug("Updated order status: {} -> {}", orderId, newStatus);
            
        } catch (SQLException e) {
            metrics.recordError("database", e);
            throw new RuntimeException("Failed to update order status: " + orderId, e);
        }
    }
    
    public Order findById(String orderId) {
        String sql = "SELECT id, customer_id, amount, currency, status, created_at, updated_at " +
                    "FROM orders WHERE id = ?";
        
        long startTime = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getString("id"));
                    order.setCustomerId(rs.getString("customer_id"));
                    order.setAmount(rs.getBigDecimal("amount"));
                    order.setCurrency(rs.getString("currency"));
                    order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                    order.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    order.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    
                    metrics.recordDuration(DATABASE_OPERATION_DURATION, 
                        System.currentTimeMillis() - startTime, "findById");
                    metrics.incrementCounter(DATABASE_OPERATIONS_TOTAL);
                    
                    return order;
                }
            }
            
        } catch (SQLException e) {
            metrics.recordError("database", e);
            throw new RuntimeException("Failed to find order: " + orderId, e);
        }
        
        return null;
    }
}
