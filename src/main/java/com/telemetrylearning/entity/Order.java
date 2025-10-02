package com.telemetrylearning.entity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Simple Order entity - Plain Old Java Object (POJO)
 * Contains only data and basic getters/setters
 */
public class Order {
    
    public enum OrderStatus {
        CREATED, PROCESSING, COMPLETED, CANCELLED, FAILED
    }
    
    private String id;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Default constructor
    public Order() {
    }
    
    // Constructor for creating new orders
    public Order(String id, String customerId, BigDecimal amount, String currency, OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods (minimal)
    public boolean isHighValue() {
        return amount != null && amount.compareTo(new BigDecimal("1000")) > 0;
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}