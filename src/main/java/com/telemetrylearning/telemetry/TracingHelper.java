package com.telemetrylearning.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.function.Supplier;

/**
 * Simple tracing helper for creating spans and managing trace context
 * Industry standard: Provide simple API for distributed tracing
 */
public class TracingHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(TracingHelper.class);
    private final Tracer tracer;
    
    public TracingHelper(OpenTelemetry openTelemetry, String serviceName) {
        this.tracer = openTelemetry.getTracer(serviceName);
        logger.info("TracingHelper initialized for service: {}", serviceName);
    }
    
    /**
     * Executes operation within a span with proper MDC context
     */
    public <T> T traceOperation(String operationName, Supplier<T> operation) {
        Span span = tracer.spanBuilder(operationName).startSpan();
        try (Scope scope = span.makeCurrent()) {
            // Set MDC for logging correlation
            setTracingMDC(span);
            
            T result = operation.get();
            span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            // Clean up MDC
            clearTracingMDC();
            span.end();
        }
    }
    
    /**
     * Executes void operation within a span
     */
    public void traceVoidOperation(String operationName, Runnable operation) {
        traceOperation(operationName, () -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * Adds business context to current span
     */
    public void addBusinessContext(String entityType, String entityId, String operation) {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            currentSpan.setAttribute("business.entity.type", entityType);
            currentSpan.setAttribute("business.entity.id", entityId);
            currentSpan.setAttribute("business.operation", operation);
        }
    }
    
    /**
     * Records a business event in the current span
     */
    public void recordBusinessEvent(String eventName, String entityType, String entityId) {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            currentSpan.addEvent(eventName);
            currentSpan.setAttribute("event.entity.type", entityType);
            currentSpan.setAttribute("event.entity.id", entityId);
        }
    }
    
    /**
     * Sets tracing context in MDC for log correlation
     */
    private void setTracingMDC(Span span) {
        SpanContext spanContext = span.getSpanContext();
        if (spanContext.isValid()) {
            MDC.put("traceId", spanContext.getTraceId());
            MDC.put("spanId", spanContext.getSpanId());
        }
    }
    
    /**
     * Clears tracing context from MDC
     */
    private void clearTracingMDC() {
        MDC.remove("traceId");
        MDC.remove("spanId");
    }
    
    /**
     * Manually set MDC for current span (useful for async operations)
     */
    public void updateMDCForCurrentSpan() {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            setTracingMDC(currentSpan);
        }
    }
}
