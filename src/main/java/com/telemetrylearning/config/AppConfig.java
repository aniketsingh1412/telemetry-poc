package com.telemetrylearning.config;

/**
 * Simple configuration POJO - just data, no logic!
 * Industry standard: Config classes should be simple data holders
 */
public class AppConfig {
    
    // Server settings
    private int serverPort = 8080;
    
    // Database settings  
    private String databaseUrl = "jdbc:mysql://localhost:3306/telemetry_learning";
    private String databaseUsername = "telemetry_user";
    private String databasePassword = "telemetry_pass";
    private String databaseDriver = "com.mysql.cj.jdbc.Driver";
    
    // Telemetry settings
    private String serviceName = "telemetry-learning-production";
    private String serviceVersion = "1.0.0";
    private String otlpEndpoint = "http://localhost:4317";
    private boolean telemetryEnabled = true;
    private String environment = "production";
    
    // Getters and setters only - no business logic!
    public int getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    
    public String getDatabaseUrl() {
        return databaseUrl;
    }
    
    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
    
    public String getDatabaseUsername() {
        return databaseUsername;
    }
    
    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }
    
    public String getDatabasePassword() {
        return databasePassword;
    }
    
    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }
    
    public String getDatabaseDriver() {
        return databaseDriver;
    }
    
    public void setDatabaseDriver(String databaseDriver) {
        this.databaseDriver = databaseDriver;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getServiceVersion() {
        return serviceVersion;
    }
    
    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
    
    public String getOtlpEndpoint() {
        return otlpEndpoint;
    }
    
    public void setOtlpEndpoint(String otlpEndpoint) {
        this.otlpEndpoint = otlpEndpoint;
    }
    
    public boolean isTelemetryEnabled() {
        return telemetryEnabled;
    }
    
    public void setTelemetryEnabled(boolean telemetryEnabled) {
        this.telemetryEnabled = telemetryEnabled;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}