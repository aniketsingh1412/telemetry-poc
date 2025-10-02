package com.telemetrylearning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.telemetrylearning.config.AppConfig;
import com.telemetrylearning.factory.ConfigFactory;
import com.telemetrylearning.factory.TelemetryFactory;
import com.telemetrylearning.entity.Order;
import com.telemetrylearning.entity.User;
import com.telemetrylearning.repository.OrderRepository;
import com.telemetrylearning.repository.UserRepository;
import com.telemetrylearning.service.OrderService;
import com.telemetrylearning.service.UserService;
import com.telemetrylearning.telemetry.SimpleMetricsRegistry;
import com.telemetrylearning.telemetry.TracingHelper;
import io.opentelemetry.api.OpenTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple REST API server with telemetry using Java's built-in HTTP server
 * - No complex frameworks, just classic Java
 * - MySQL database with plain JDBC
 * - LinkedIn-style metrics tracking
 */
public class TelemetryApp implements HttpHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TelemetryApp.class);
    
    private final AppConfig config;
    private final Connection connection;
    private final SimpleMetricsRegistry metrics;
    private final TracingHelper tracing;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final HttpServer server;
    
    public TelemetryApp() throws Exception {
        logger.info("🚀 Starting Simple Telemetry API Server");
        
        // Load configuration using factory
        this.config = ConfigFactory.createAppConfig();
        
        // Initialize database connection
        this.connection = createDatabaseConnection();
        
        // Initialize telemetry using factory
        OpenTelemetry openTelemetry = TelemetryFactory.createOpenTelemetry(config);
        this.metrics = TelemetryFactory.createMetricsRegistry(openTelemetry, config.getServiceName());
        this.tracing = TelemetryFactory.createTracingHelper(openTelemetry, config.getServiceName());
        
        // Initialize services with tracing
        this.userRepository = new UserRepository(connection, metrics);
        this.orderRepository = new OrderRepository(connection, metrics);
        this.userService = new UserService(userRepository, metrics, tracing);
        this.orderService = new OrderService(orderRepository, metrics, tracing);
        
        // Initialize JSON mapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        // Initialize HTTP server
        this.server = HttpServer.create(new InetSocketAddress(config.getServerPort()), 0);
        this.server.createContext("/api", this);
        this.server.setExecutor(null); // Use default executor
        
        logger.info("✅ Application initialized successfully");
    }
    
    private Connection createDatabaseConnection() throws SQLException {
        logger.info("🗄️ Connecting to MySQL database: {}", config.getDatabaseUrl());
        
        try {
            Class.forName(config.getDatabaseDriver());
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found", e);
        }
        
        Connection conn = DriverManager.getConnection(
            config.getDatabaseUrl(),
            config.getDatabaseUsername(),
            config.getDatabasePassword()
        );
        
        conn.setAutoCommit(true);
        logger.info("✅ Database connection established");
        return conn;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Trace the entire HTTP request
        tracing.traceVoidOperation("http.request", () -> {
            try {
                handleHttpRequest(exchange);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    private void handleHttpRequest(HttpExchange exchange) throws IOException {
        // Add CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            // Add tracing context for HTTP request
            tracing.addBusinessContext("http.request", path, method);
            
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 200, "{}");
                return;
            }
            
            logger.info("Handling {} {}", method, path);
            
            if ("GET".equals(method)) {
                handleGet(path, exchange);
            } else if ("POST".equals(method)) {
                handlePost(path, exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
            
        } catch (Exception e) {
            logger.error("API error: {}", e.getMessage(), e);
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private void handleGet(String path, HttpExchange exchange) throws IOException {
        if ("/api/health".equals(path)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("status", "healthy");
            result.put("timestamp", Instant.now());
            result.put("service", config.getServiceName());
            sendJsonResponse(exchange, 200, result);
            
        } else if ("/api/users".equals(path)) {
            List<User> users = userService.getActiveUsers();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", users);
            result.put("count", users.size());
            sendJsonResponse(exchange, 200, result);
            logger.info("Retrieved {} active users", users.size());
            
        } else if (path.startsWith("/api/orders/customer/")) {
            String customerId = path.substring("/api/orders/customer/".length());
            List<Order> orders = orderService.getOrdersByCustomer(customerId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", orders);
            result.put("count", orders.size());
            sendJsonResponse(exchange, 200, result);
            logger.info("Retrieved {} orders for customer: {}", orders.size(), customerId);
            
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found: " + path);
        }
    }
    
    private void handlePost(String path, HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        
        if ("/api/users".equals(path)) {
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
            
            String username = (String) requestData.get("username");
            String email = (String) requestData.get("email");
            
            if (username == null || email == null) {
                sendErrorResponse(exchange, 400, "Missing required fields: username, email");
                return;
            }
            
            User user = tracing.traceOperation("user.create", () -> {
                tracing.addBusinessContext("user", username, "create");
                return userService.createUser(username, email);
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", user);
            result.put("message", "User created successfully");
            sendJsonResponse(exchange, 201, result);
            logger.info("Created user: {} (ID: {})", username, user.getId());
            
        } else if ("/api/orders".equals(path)) {
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
            
            String customerId = (String) requestData.get("customerId");
            Object amountObj = requestData.get("amount");
            String currency = (String) requestData.get("currency");
            
            if (customerId == null || amountObj == null || currency == null) {
                sendErrorResponse(exchange, 400, "Missing required fields: customerId, amount, currency");
                return;
            }
            
            BigDecimal amount = new BigDecimal(amountObj.toString());
            
            Order order = tracing.traceOperation("order.create", () -> {
                tracing.addBusinessContext("order", customerId, "create");
                return orderService.createOrder(customerId, amount, currency);
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", order);
            result.put("message", "Order created successfully");
            sendJsonResponse(exchange, 201, result);
            logger.info("Created order: {} for customer: {} (Amount: {} {})", 
                order.getId(), customerId, amount, currency);
                
        } else if (path.startsWith("/api/orders/") && path.endsWith("/process")) {
            String orderId = path.substring("/api/orders/".length(), path.length() - "/process".length());
            orderService.processOrder(orderId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Order processed successfully");
            result.put("orderId", orderId);
            sendJsonResponse(exchange, 200, result);
            logger.info("Processed order: {}", orderId);
            
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found: " + path);
        }
    }
    
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    private void sendJsonResponse(HttpExchange exchange, int status, Object data) throws IOException {
        String response = objectMapper.writeValueAsString(data);
        sendResponse(exchange, status, response);
    }
    
    private void sendErrorResponse(HttpExchange exchange, int status, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", Instant.now());
        sendJsonResponse(exchange, status, error);
    }
    
    private void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }
    
    public void startServer() throws Exception {
        logger.info("🏁 Starting HTTP server...");
        server.start();
        
        // Record startup metric
        metrics.incrementCounter("application.startups.total");
        
        logger.info("🚀 Server started successfully!");
        logger.info("📋 Available endpoints:");
        logger.info("   GET  /api/health           - Health check");
        logger.info("   GET  /api/users            - List all users");
        logger.info("   POST /api/users            - Create user");
        logger.info("   POST /api/orders           - Create order");
        logger.info("   GET  /api/orders/customer/{id} - Get orders by customer");
        logger.info("   POST /api/orders/{id}/process  - Process order");
        logger.info("");
        logger.info("🧪 Test with curl:");
        logger.info("   curl http://localhost:{}/api/health", config.getServerPort());
        logger.info("   curl -X POST http://localhost:{}/api/users -H 'Content-Type: application/json' -d '{{\"username\":\"alice\",\"email\":\"alice@example.com\"}}'", config.getServerPort());
        
        // Keep server running
        logger.info("⏳ Server is running... (Press Ctrl+C to stop)");
        Thread.currentThread().join(); // Keep main thread alive
    }
    
    public void shutdown() {
        logger.info("🛑 Shutting down application...");
        
        if (server != null) {
            server.stop(0);
            logger.info("✅ HTTP server stopped");
        }
        
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("✅ Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
        
        logger.info("✅ Application shutdown complete");
    }
    
    public static void main(String[] args) {
        TelemetryApp app = null;
        try {
            app = new TelemetryApp();
            
            // Create final reference for shutdown hook
            final TelemetryApp finalApp = app;
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (finalApp != null) {
                    finalApp.shutdown();
                }
            }));
            
            // Start server (this blocks)
            app.startServer();
            
        } catch (Exception e) {
            logger.error("❌ Application failed", e);
            System.exit(1);
        } finally {
            if (app != null) {
                app.shutdown();
            }
        }
    }
}