package com.telemetrylearning.telemetry;

/**
 * OpenTelemetry metrics constants for the Telemetry Learning system.
 * 
 * This class defines all counter metrics with their descriptions, following
 * industry standards for metric naming and documentation. All metrics are
 * pre-defined to ensure consistency across the application.
 */
public final class TelemetryConstants {

    private TelemetryConstants() {
        // Utility class
    }

    /**
     * Standard unit designation for counter metrics.
     */
    public static final String COUNTER_UNIT = "1";

    /**
     * Counter for unknown/unmapped metrics - fallback for missing counters.
     */
    public static final String UNKNOWN_METRIC_COUNT = "unknown.metric.count";

    /**
     * Pre-defined counters with descriptions.
     * Format: {counter_name, description}
     */
    public static final String[][] COUNTERS_WITH_DESCRIPTIONS = {
        // User operations
        {"user.created.total", "Total number of users created"},
        {"user.found.total", "Total number of user lookup operations"},
        {"user.updated.total", "Total number of user update operations"},
        {"user.deactivated.total", "Total number of users deactivated"},
        {"user.errors.total", "Total number of user operation errors"},
        
        // Order operations
        {"order.created.total", "Total number of orders created"},
        {"order.processed.total", "Total number of orders processed"},
        {"order.completed.total", "Total number of orders completed"},
        {"order.cancelled.total", "Total number of orders cancelled"},
        {"order.errors.total", "Total number of order operation errors"},
        
        // Business metrics
        {"business.high_value_orders.total", "Total number of high-value orders"},
        {"business.transactions.total", "Total number of business transactions"},
        
        // System operations  
        {"database.operations.total", "Total number of database operations"},
        {"database.errors.total", "Total number of database errors"},
        {"health.checks.total", "Total number of health check operations"},
        {"application.startups.total", "Total number of application startups"},
        
        // Telemetry system
        {"telemetry.spans.created.total", "Total number of spans created"},
        {"telemetry.metrics.recorded.total", "Total number of metrics recorded"},
        
        // Unknown metrics fallback
        {UNKNOWN_METRIC_COUNT, "Total number of unknown metric increment attempts"}
    };

    /**
     * Histogram metrics with descriptions.
     * Format: {histogram_name, description, unit}
     */
    public static final String[][] HISTOGRAMS_WITH_DESCRIPTIONS = {
        {"user.operation.duration", "Duration of user operations", "ms"},
        {"order.operation.duration", "Duration of order operations", "ms"},
        {"order.value.distribution", "Distribution of order values", "USD"},
        {"database.operation.duration", "Duration of database operations", "ms"},
        {"business.transaction.amount", "Distribution of transaction amounts", "USD"}
    };
}
