package com.telemetrylearning.service;

import com.telemetrylearning.entity.Order;
import com.telemetrylearning.repository.OrderRepository;
import com.telemetrylearning.telemetry.SimpleMetricsRegistry;
import com.telemetrylearning.telemetry.TracingHelper;

import static com.telemetrylearning.telemetry.TelemetryConstants.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Order service for business logic and operations
 * Handles order creation, processing, and management
 */
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final SimpleMetricsRegistry metrics;
    private final TracingHelper tracing;
    
    public OrderService(OrderRepository orderRepository, SimpleMetricsRegistry metrics, TracingHelper tracing) {
        this.orderRepository = orderRepository;
        this.metrics = metrics;
        this.tracing = tracing;
    }
    
    public Order createOrder(String customerId, BigDecimal amount, String currency) {
        logger.info("Creating order for customer: {} (Amount: {} {})", customerId, amount, currency);
        
        // Validate input
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }
        
        // Create order entity
        Order order = new Order();
        order.setId("order-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000));
        order.setCustomerId(customerId.trim());
        order.setAmount(amount);
        order.setCurrency(currency.trim().toUpperCase());
        order.setStatus(Order.OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        
        // Save to database
        orderRepository.saveOrder(order);
        
        // Record metrics
        metrics.incrementCounter(ORDER_CREATED_TOTAL);
        metrics.recordHistogram(ORDER_VALUE_DISTRIBUTION, amount.doubleValue());
        
        // Check for high-value orders
        if (order.isHighValue()) {
            metrics.incrementCounter(BUSINESS_HIGH_VALUE_ORDERS_TOTAL);
            logger.warn("High-value order created: {} for ${}", order.getId(), amount);
        }
        
        logger.info("Order created successfully: {} (ID: {})", customerId, order.getId());
        return order;
    }
    
    public void processOrder(String orderId) {
        logger.info("Processing order: {}", orderId);
        
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        
        try {
            // Simulate order processing time
            Thread.sleep(100);
            
            // Update order status to processing
            orderRepository.updateOrderStatus(orderId, Order.OrderStatus.PROCESSING);
            metrics.incrementCounter(ORDER_PROCESSED_TOTAL);
            
            // Simulate more processing
            Thread.sleep(50);
            
            // Complete the order
            orderRepository.updateOrderStatus(orderId, Order.OrderStatus.COMPLETED);
            metrics.incrementCounter(ORDER_COMPLETED_TOTAL);
            
            logger.info("Order processed successfully: {}", orderId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Order processing interrupted: {}", orderId);
            metrics.recordError("order", e);
            throw new RuntimeException("Order processing interrupted", e);
        } catch (Exception e) {
            logger.error("Failed to process order: {}", orderId, e);
            orderRepository.updateOrderStatus(orderId, Order.OrderStatus.FAILED);
            metrics.recordError("order", e);
            throw e;
        }
    }
    
    public Order getOrderById(String orderId) {
        logger.debug("Getting order by ID: {}", orderId);
        
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        
        Order order = orderRepository.findById(orderId);
        
        if (order != null) {
            metrics.incrementCounter(ORDER_FOUND_TOTAL);
        }
        
        return order;
    }
    
    public List<Order> getOrdersByCustomer(String customerId) {
        logger.debug("Getting orders for customer: {}", customerId);
        
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        
        List<Order> orders = orderRepository.findOrdersByCustomer(customerId);
        metrics.incrementCounter(ORDER_FOUND_TOTAL);
        
        logger.debug("Found {} orders for customer: {}", orders.size(), customerId);
        return orders;
    }
    
    public void cancelOrder(String orderId) {
        logger.info("Cancelling order: {}", orderId);
        
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        
        // Check if order exists and can be cancelled
        Order existingOrder = orderRepository.findById(orderId);
        if (existingOrder == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        if (existingOrder.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed order: " + orderId);
        }
        
        // Cancel the order
        orderRepository.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
        metrics.incrementCounter(ORDER_CANCELLED_TOTAL);
        
        logger.info("Order cancelled successfully: {}", orderId);
    }
}
