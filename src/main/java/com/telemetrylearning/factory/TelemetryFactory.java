package com.telemetrylearning.factory;

import com.telemetrylearning.config.AppConfig;
import com.telemetrylearning.telemetry.SimpleMetricsRegistry;
import com.telemetrylearning.telemetry.TracingHelper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Factory for creating telemetry components
 * Industry standard: Use factories for complex object creation
 */
public class TelemetryFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(TelemetryFactory.class);
    
    /**
     * Creates a fully configured OpenTelemetry instance
     * All complex logic is here, not in config classes
     */
    public static OpenTelemetry createOpenTelemetry(AppConfig config) {
        if (!config.isTelemetryEnabled()) {
            logger.info("Telemetry disabled, using no-op implementation");
            return OpenTelemetry.noop();
        }
        
        logger.info("Creating OpenTelemetry for service: {}", config.getServiceName());
        
        // Create resource
        Resource resource = createResource(config);
        
        // Create tracer provider
        SdkTracerProvider tracerProvider = createTracerProvider(resource, config);
        
        // Create meter provider
        SdkMeterProvider meterProvider = createMeterProvider(resource, config);
        
        // Create logger provider
        SdkLoggerProvider loggerProvider = createLoggerProvider(resource, config);
        
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .build();
        
        logger.info("✅ OpenTelemetry initialized successfully");
        return openTelemetry;
    }
    
    /**
     * Creates metrics registry using the factory pattern
     */
    public static SimpleMetricsRegistry createMetricsRegistry(OpenTelemetry openTelemetry, String serviceName) {
        return new SimpleMetricsRegistry(openTelemetry, serviceName);
    }
    
    /**
     * Creates tracing helper using the factory pattern
     */
    public static TracingHelper createTracingHelper(OpenTelemetry openTelemetry, String serviceName) {
        return new TracingHelper(openTelemetry, serviceName);
    }
    
    private static Resource createResource(AppConfig config) {
        return Resource.getDefault()
            .merge(Resource.create(Attributes.of(
                AttributeKey.stringKey("service.name"), config.getServiceName(),
                AttributeKey.stringKey("service.version"), config.getServiceVersion(),
                AttributeKey.stringKey("deployment.environment"), config.getEnvironment()
            )));
    }
    
    private static SdkTracerProvider createTracerProvider(Resource resource, AppConfig config) {
        return SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(
                OtlpGrpcSpanExporter.builder()
                    .setEndpoint(config.getOtlpEndpoint())
                    .build())
                .build())
            .setResource(resource)
            .setSampler(createSampler(config.getEnvironment()))
            .build();
    }
    
    private static SdkMeterProvider createMeterProvider(Resource resource, AppConfig config) {
        return SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(
                OtlpGrpcMetricExporter.builder()
                    .setEndpoint(config.getOtlpEndpoint())
                    .build())
                .setInterval(Duration.ofSeconds(30))
                .build())
            .setResource(resource)
            .build();
    }
    
    private static SdkLoggerProvider createLoggerProvider(Resource resource, AppConfig config) {
        return SdkLoggerProvider.builder()
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(
                OtlpGrpcLogRecordExporter.builder()
                    .setEndpoint(config.getOtlpEndpoint())
                    .build())
                .build())
            .setResource(resource)
            .build();
    }
    
    private static Sampler createSampler(String environment) {
        // Simple sampling strategy based on environment
        if ("production".equals(environment)) {
            return Sampler.traceIdRatioBased(0.1);   // 10% sampling in prod
        } else if ("staging".equals(environment)) {
            return Sampler.traceIdRatioBased(0.5);   // 50% sampling in staging
        } else {
            return Sampler.alwaysOn();               // Always sample in dev
        }
    }
}
