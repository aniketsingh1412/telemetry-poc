package com.telemetrylearning.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.telemetrylearning.telemetry.TelemetryConstants.*;

/**
 * OpenTelemetry-based metrics implementation for the Telemetry Learning system.
 *
 * This class provides simple, industry-standard metrics collection using OpenTelemetry
 * for monitoring and observability. It follows LinkedIn's proven patterns for
 * enterprise-grade telemetry with minimal complexity and maximum reliability.
 *
 * Key features:
 * - Pre-initialized counters and histograms for performance
 * - Thread-safe operations using ConcurrentHashMap
 * - Automatic fallback to unknown metric counter
 * - Simple API with just increment() and record() methods
 */
public class SimpleMetricsRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMetricsRegistry.class);

    /**
     * OpenTelemetry meter instance for creating and managing metrics.
     * Named with service identifier for clear attribution in monitoring systems.
     */
    private final Meter meter;

    /**
     * Thread-safe map storing counter name to LongCounter instance mappings.
     * All counters are pre-initialized during construction to avoid runtime overhead.
     */
    private final Map<String, LongCounter> counterMap = new ConcurrentHashMap<>();

    /**
     * Thread-safe map storing histogram name to DoubleHistogram instance mappings.
     * All histograms are pre-initialized during construction for consistent performance.
     */
    private final Map<String, DoubleHistogram> histogramMap = new ConcurrentHashMap<>();

    /**
     * Constructs the metrics registry and initializes all predefined metrics.
     *
     * This constructor automatically registers all counters and histograms defined in
     * TelemetryConstants, creating OpenTelemetry metric instances with appropriate
     * descriptions and units. All metrics are immediately available after construction.
     *
     * @param openTelemetry the OpenTelemetry instance
     * @param serviceName the name of the service for metric attribution
     */
    public SimpleMetricsRegistry(OpenTelemetry openTelemetry, String serviceName) {
        this.meter = openTelemetry.getMeter(serviceName + "-metrics");
        
        initializeCounters();
        initializeHistograms();
        
        logger.info("📊 Simple metrics registry initialized for service: {} with {} counters and {} histograms", 
            serviceName, counterMap.size(), histogramMap.size());
    }

    /**
     * Increments the specified counter by one unit.
     *
     * This method provides thread-safe counter increment functionality with
     * automatic fallback handling. If a counter name is not found, the system
     * automatically increments the unknown metric counter, ensuring no metrics
     * are lost while maintaining observability.
     *
     * @param counterName The name of the counter to increment
     */
    public void incrementCounter(String counterName) {
        LongCounter counter = counterMap.getOrDefault(counterName, 
            counterMap.get(UNKNOWN_METRIC_COUNT));
        counter.add(1);
    }

    /**
     * Records a value in the specified histogram.
     *
     * This method provides thread-safe histogram recording with automatic fallback.
     * If a histogram name is not found, the operation is logged but continues
     * gracefully without throwing exceptions.
     *
     * @param histogramName The name of the histogram to record to
     * @param value The value to record
     */
    public void recordHistogram(String histogramName, double value) {
        DoubleHistogram histogram = histogramMap.get(histogramName);
        if (histogram != null) {
            histogram.record(value);
        } else {
            logger.warn("Unknown histogram: {}, recording to unknown counter instead", histogramName);
            incrementCounter(UNKNOWN_METRIC_COUNT);
        }
    }

    /**
     * Records a value in the specified histogram with additional context.
     * Convenience method for common use cases like recording durations.
     *
     * @param histogramName The name of the histogram
     * @param value The value to record
     * @param operation The operation name for context (logged for debugging)
     */
    public void recordDuration(String histogramName, double value, String operation) {
        recordHistogram(histogramName, value);
        logger.debug("Recorded duration: {}ms for operation: {}", value, operation);
    }

    /**
     * Convenience method to record successful operation completion.
     * Increments both the specific operation counter and a general success counter.
     *
     * @param operationType The type of operation (e.g., "user.created")
     */
    public void recordSuccess(String operationType) {
        incrementCounter(operationType + ".total");
    }

    /**
     * Convenience method to record operation errors.
     * Increments both the specific error counter and logs the error for debugging.
     *
     * @param operationType The type of operation that failed
     * @param error The error that occurred (optional, for logging)
     */
    public void recordError(String operationType, Throwable error) {
        incrementCounter(operationType + ".errors.total");
        if (error != null) {
            logger.debug("Recorded error for operation: {} - {}", operationType, error.getMessage());
        }
    }

    /**
     * Initializes all counter metrics from the constants definition.
     * Each counter is created with proper description and unit.
     */
    private void initializeCounters() {
        for (String[] counterDef : COUNTERS_WITH_DESCRIPTIONS) {
            String counterName = counterDef[0];
            String description = counterDef[1];
            
            LongCounter counter = meter.counterBuilder(counterName)
                .setDescription(description)
                .setUnit(COUNTER_UNIT)
                .build();
            
            counterMap.put(counterName, counter);
        }
        logger.debug("Initialized {} counters", counterMap.size());
    }

    /**
     * Initializes all histogram metrics from the constants definition.
     * Each histogram is created with proper description and unit.
     */
    private void initializeHistograms() {
        for (String[] histogramDef : HISTOGRAMS_WITH_DESCRIPTIONS) {
            String histogramName = histogramDef[0];
            String description = histogramDef[1];
            String unit = histogramDef[2];
            
            DoubleHistogram histogram = meter.histogramBuilder(histogramName)
                .setDescription(description)
                .setUnit(unit)
                .build();
            
            histogramMap.put(histogramName, histogram);
        }
        logger.debug("Initialized {} histograms", histogramMap.size());
    }
}
