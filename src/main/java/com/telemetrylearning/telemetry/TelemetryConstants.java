package com.telemetrylearning.telemetry;

/**
 * Constants for Telemetry metrics used by the telemetry-poc system.
 *
 * This class follows the style used in com.linkedin.cfi.telemetry.ContentFetcherMetricsConstants:
 * - All metric names are defined as public static final String in UPPER_SNAKE_CASE.
 * - Counter / histogram definition arrays are package-private static final String[][],
 *   each entry containing {METRIC_NAME_CONSTANT, "description"}.
 *
 * Keep this class as a simple constants holder — no instantiation allowed.
 */
public final class TelemetryConstants {

    /**
     * Private constructor to prevent instantiation.
     */
    private TelemetryConstants() {
    }

    /* ---------------------------------------------------------------------
     * Unit / fallback
     * ------------------------------------------------------------------ */

    public static final String COUNTER_UNIT = "1";
    public static final String UNKNOWN_METRIC_COUNT = "UNKNOWN_METRIC_COUNT";

    /* ---------------------------------------------------------------------
     * User operation counters
     * ------------------------------------------------------------------ */

    public static final String USER_CREATED_TOTAL = "USER_CREATED_TOTAL";
    public static final String USER_FOUND_TOTAL = "USER_FOUND_TOTAL";
    public static final String USER_UPDATED_TOTAL = "USER_UPDATED_TOTAL";
    public static final String USER_DEACTIVATED_TOTAL = "USER_DEACTIVATED_TOTAL";
    public static final String USER_ERRORS_TOTAL = "USER_ERRORS_TOTAL";

    /* ---------------------------------------------------------------------
     * Order operation counters
     * ------------------------------------------------------------------ */

    public static final String ORDER_CREATED_TOTAL = "ORDER_CREATED_TOTAL";
    public static final String ORDER_PROCESSED_TOTAL = "ORDER_PROCESSED_TOTAL";
    public static final String ORDER_COMPLETED_TOTAL = "ORDER_COMPLETED_TOTAL";
    public static final String ORDER_CANCELLED_TOTAL = "ORDER_CANCELLED_TOTAL";
    public static final String ORDER_ERRORS_TOTAL = "ORDER_ERRORS_TOTAL";

    /* ---------------------------------------------------------------------
     * Business counters
     * ------------------------------------------------------------------ */

    public static final String BUSINESS_HIGH_VALUE_ORDERS_TOTAL = "BUSINESS_HIGH_VALUE_ORDERS_TOTAL";
    public static final String BUSINESS_TRANSACTIONS_TOTAL = "BUSINESS_TRANSACTIONS_TOTAL";

    /* ---------------------------------------------------------------------
     * System counters
     * ------------------------------------------------------------------ */

    public static final String DATABASE_OPERATIONS_TOTAL = "DATABASE_OPERATIONS_TOTAL";
    public static final String DATABASE_ERRORS_TOTAL = "DATABASE_ERRORS_TOTAL";
    public static final String HEALTH_CHECKS_TOTAL = "HEALTH_CHECKS_TOTAL";
    public static final String APPLICATION_STARTUPS_TOTAL = "APPLICATION_STARTUPS_TOTAL";

    /* ---------------------------------------------------------------------
     * Telemetry system counters
     * ------------------------------------------------------------------ */

    public static final String TELEMETRY_SPANS_CREATED_TOTAL = "TELEMETRY_SPANS_CREATED_TOTAL";
    public static final String TELEMETRY_METRICS_RECORDED_TOTAL = "TELEMETRY_METRICS_RECORDED_TOTAL";

    /* ---------------------------------------------------------------------
     * Counters with descriptions (package-private)
     * Format: { {METRIC_CONSTANT, "description"}, ... }
     * ------------------------------------------------------------------ */
    static final String[][] COUNTERS_WITH_DESCRIPTIONS = {
        // User operations
        {USER_CREATED_TOTAL, "Total number of users created"},
        {USER_FOUND_TOTAL, "Total number of user lookup operations"},
        {USER_UPDATED_TOTAL, "Total number of user update operations"},
        {USER_DEACTIVATED_TOTAL, "Total number of users deactivated"},
        {USER_ERRORS_TOTAL, "Total number of user operation errors"},

        // Order operations
        {ORDER_CREATED_TOTAL, "Total number of orders created"},
        {ORDER_PROCESSED_TOTAL, "Total number of orders processed"},
        {ORDER_COMPLETED_TOTAL, "Total number of orders completed"},
        {ORDER_CANCELLED_TOTAL, "Total number of orders cancelled"},
        {ORDER_ERRORS_TOTAL, "Total number of order operation errors"},

        // Business metrics
        {BUSINESS_HIGH_VALUE_ORDERS_TOTAL, "Total number of high-value orders"},
        {BUSINESS_TRANSACTIONS_TOTAL, "Total number of business transactions"},

        // System operations
        {DATABASE_OPERATIONS_TOTAL, "Total number of database operations"},
        {DATABASE_ERRORS_TOTAL, "Total number of database errors"},
        {HEALTH_CHECKS_TOTAL, "Total number of health check operations"},
        {APPLICATION_STARTUPS_TOTAL, "Total number of application startups"},

        // Telemetry system
        {TELEMETRY_SPANS_CREATED_TOTAL, "Total number of spans created"},
        {TELEMETRY_METRICS_RECORDED_TOTAL, "Total number of metrics recorded"},

        // Unknown/fallback
        {UNKNOWN_METRIC_COUNT, "Total number of unknown metric increment attempts"},
    };

    /* ---------------------------------------------------------------------
     * Histogram metric names (defined as constants) and descriptions array
     * ------------------------------------------------------------------ */

    public static final String USER_OPERATION_DURATION = "USER_OPERATION_DURATION";
    public static final String ORDER_OPERATION_DURATION = "ORDER_OPERATION_DURATION";
    public static final String ORDER_VALUE_DISTRIBUTION = "ORDER_VALUE_DISTRIBUTION";
    public static final String DATABASE_OPERATION_DURATION = "DATABASE_OPERATION_DURATION";
    public static final String BUSINESS_TRANSACTION_AMOUNT = "BUSINESS_TRANSACTION_AMOUNT";

    /**
     * Histogram definitions with descriptions and units.
     * Format: { {HISTOGRAM_NAME, "description", "unit"}, ... }
     */
    static final String[][] HISTOGRAMS_WITH_DESCRIPTIONS = {
        {USER_OPERATION_DURATION, "Duration of user operations", "ms"},
        {ORDER_OPERATION_DURATION, "Duration of order operations", "ms"},
        {ORDER_VALUE_DISTRIBUTION, "Distribution of order values", "USD"},
        {DATABASE_OPERATION_DURATION, "Duration of database operations", "ms"},
        {BUSINESS_TRANSACTION_AMOUNT, "Distribution of transaction amounts", "USD"},
    };
}