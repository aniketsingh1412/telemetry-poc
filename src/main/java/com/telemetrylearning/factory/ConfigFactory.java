package com.telemetrylearning.factory;

import com.telemetrylearning.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Factory for creating and loading application configuration
 * Industry standard: Separate configuration loading from data holding
 */
public class ConfigFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigFactory.class);
    
    /**
     * Creates AppConfig by loading from properties file
     * All loading logic is here, not in the config class
     */
    public static AppConfig createAppConfig() {
        AppConfig config = new AppConfig();
        
        // Load from properties file
        Properties properties = loadProperties();
        
        // Map properties to config object
        if (properties != null) {
            mapProperties(properties, config);
        }
        
        logger.info("Configuration loaded: service={}, port={}", 
            config.getServiceName(), config.getServerPort());
        
        return config;
    }
    
    private static Properties loadProperties() {
        Properties properties = new Properties();
        
        try (InputStream input = ConfigFactory.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input != null) {
                properties.load(input);
                logger.debug("Loaded {} properties from application.properties", properties.size());
                return properties;
            } else {
                logger.warn("application.properties not found, using defaults");
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Failed to load application.properties: {}", e.getMessage());
            return null;
        }
    }
    
    private static void mapProperties(Properties properties, AppConfig config) {
        // Server configuration
        if (properties.containsKey("server.port")) {
            config.setServerPort(Integer.parseInt(properties.getProperty("server.port")));
        }
        
        // Database configuration
        if (properties.containsKey("database.url")) {
            config.setDatabaseUrl(properties.getProperty("database.url"));
        }
        if (properties.containsKey("database.username")) {
            config.setDatabaseUsername(properties.getProperty("database.username"));
        }
        if (properties.containsKey("database.password")) {
            config.setDatabasePassword(properties.getProperty("database.password"));
        }
        if (properties.containsKey("database.driver")) {
            config.setDatabaseDriver(properties.getProperty("database.driver"));
        }
        
        // Telemetry configuration
        if (properties.containsKey("telemetry.service.name")) {
            config.setServiceName(properties.getProperty("telemetry.service.name"));
        }
        if (properties.containsKey("telemetry.service.version")) {
            config.setServiceVersion(properties.getProperty("telemetry.service.version"));
        }
        if (properties.containsKey("telemetry.otlp.endpoint")) {
            config.setOtlpEndpoint(properties.getProperty("telemetry.otlp.endpoint"));
        }
        if (properties.containsKey("telemetry.enabled")) {
            config.setTelemetryEnabled(Boolean.parseBoolean(properties.getProperty("telemetry.enabled")));
        }
        if (properties.containsKey("telemetry.environment")) {
            config.setEnvironment(properties.getProperty("telemetry.environment"));
        }
    }
}
