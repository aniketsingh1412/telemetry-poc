# 🚀 **Modern Cloud-Native Observability Project**

A **production-ready Java application** demonstrating all three pillars of observability following **modern cloud-native best practices** used at **Netflix, Uber, LinkedIn, and Amazon**.

## 🎯 **What You'll Master**

- **🔍 Distributed Tracing:** Request correlation with OpenTelemetry
- **📊 Metrics Collection:** Business KPIs with Prometheus
- **📝 Cloud-Native Logging:** Console-only, container-friendly structured logging
- **🏗️ Modern Architecture:** Following industry best practices for containerized environments

---

## 🚀 **Quick Start**

### **1. Start Observability Stack**
```bash
docker-compose up -d
```

### **2. Run Application**
```bash
./gradlew runApp
```

### **3. Test APIs**
```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com"}'

# Create high-value order (triggers business metrics)
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"user-123","amount":3000.00,"currency":"USD"}'
```

---

## 🌐 **Observability UIs**

| Service | URL | Purpose |
|---------|-----|---------|
| **🔍 Jaeger** | http://localhost:16686 | Distributed tracing |
| **📊 Prometheus** | http://localhost:9090 | Raw metrics |
| **📊 Grafana** | http://localhost:3000 | Dashboards (admin/admin) |
| **📊 Kibana** | http://localhost:5601 | Log analytics & search |
| **🚀 Application** | http://localhost:8080 | REST API |

---

## 📝 **Industry-Standard Log Management (ELK Stack)**

### **✅ Architecture We Implemented:**
```
Java App → Console → Docker Logs → FluentBit → Elasticsearch → Kibana
```

### **✅ Best Practices We Follow:**
- **Console-Only Output:** Logs to stdout/stderr (no files)
- **Container-Friendly:** Perfect for Docker/Kubernetes  
- **Structured Format:** Machine-parseable entries
- **Trace Correlation:** Every log linked to traces
- **Log Aggregation:** FluentBit ships logs to Elasticsearch
- **Centralized Search:** Query all logs via Kibana

### **📊 Log Format:**
```bash
2025-10-02 15:32:12.345 [HTTP-Dispatcher] INFO c.t.service.UserService [traceId=abc123] [userId=user-789] - Creating user: alice
```

### **🔍 Kibana Query Examples:**
```bash
# Search by trace ID (correlate with Jaeger)
traceId:"abc123456789"

# Find all errors
level:"ERROR"

# User service logs only
logger:"c.t.service.UserService"

# High-value order business events  
message:"High-value order" AND level:"WARN"
```

### **🏗️ Production Architecture:**
- **No File I/O:** Zero disk bottlenecks
- **Container-Native:** Docker/K8s log capture
- **Stateless Apps:** Perfect for auto-scaling
- **ELK Pipeline:** Industry-standard log management

---

## 🔍 **Complete Documentation**

📖 **See [OBSERVABILITY_FLOW.md](./OBSERVABILITY_FLOW.md)** for detailed flow documentation including:
- Complete trace journey from request to Jaeger UI
- Metrics pipeline from business logic to Grafana
- Modern logging architecture and container integration
- Infrastructure diagrams and debugging workflows

---

## 🏗️ **Project Structure**

```
src/main/java/com/telemetrylearning/
├── TelemetryApp.java           # Main application
├── service/
│   ├── UserService.java       # User business logic
│   └── OrderService.java      # Order business logic
├── repository/
│   ├── UserRepository.java    # Data access
│   └── OrderRepository.java   # Data access
├── telemetry/
│   ├── SimpleMetricsRegistry.java  # LinkedIn-style metrics
│   └── TracingHelper.java          # Distributed tracing
└── ...
```

---

## 📊 **Available Metrics**

```promql
# User creation rate
rate(users_created_total[5m])

# 95th percentile response time
histogram_quantile(0.95, rate(saveUser_duration_histogram_bucket[5m]))

# High-value order percentage
rate(high_value_orders_total[5m]) / rate(orders_created_total[5m]) * 100
```

---

## 🎓 **Learning Scenarios**

### **Complete ELK Log Pipeline Test**
```bash
# 1. Start complete stack (includes ELK)
docker-compose up -d

# 2. Run your Java app to generate logs
./gradlew runApp

# 3. Create business events
curl -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com"}'

# 4. Query logs in Kibana
# → Open http://localhost:5601
# → Search for traceId, errors, business events
# → Correlate with Jaeger traces via traceId
```

### **High-Value Order Processing**
```bash
# Create user and high-value order, observe in:
# - Jaeger: Trace with business context
# - Prometheus: high_value_orders_total metric
# - Kibana: WARN level business event logs with trace correlation
```

### **Performance Debugging**
```bash
# Generate load, then analyze:
# - Jaeger: Find slow traces
# - Prometheus: Response time percentiles
# - Kibana: Correlated logs via traceId for deep debugging
```

---

## ⚙️ **Configuration**

**Application** (`application.properties`):
```properties
service.name=telemetry-learning-production
server.port=8080
database.url=jdbc:mysql://localhost:3306/telemetry_learning
otel.endpoint=http://localhost:4317
```

**Logging** (`logback.xml`):
```xml
<!-- Console-only, structured logging -->
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [traceId=%X{traceId:-}] - %msg%n</pattern>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="CONSOLE"/>
  </root>
</configuration>
```

---

## 🏭 **Technology Stack**

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Runtime** | Java 21 | Application platform |
| **Logging** | SLF4J + Logback | Structured console logging |
| **Tracing** | OpenTelemetry | Distributed tracing |
| **Metrics** | OpenTelemetry | Business & performance metrics |
| **Storage** | Jaeger + Prometheus | Trace & metrics storage |
| **Visualization** | Grafana | Dashboards & alerts |

---

## 🔧 **Advanced Features**

- **Factory Pattern:** Clean configuration management
- **Graceful Shutdown:** Proper resource cleanup
- **Error Handling:** Exception tracking in spans
- **Business Context:** Rich metadata for analysis
- **Performance Optimized:** Batch processing, efficient exports

---

## ❓ **FAQ**

**Q: Why Console-Only Logging?**
**A:** Follows 12-factor app principles. Container orchestrators capture stdout/stderr and route to centralized logging. Eliminates file I/O and makes apps stateless.

**Q: Log4j2 vs Logback?**
**A:** Both excellent:
- **Logback:** Mature, stable, native SLF4J integration ✅
- **Log4j2:** Async logging, JSON layouts, zero-GC mode ✅
- **Our Choice:** Logback for simplicity and adoption

**Q: Production Ready?**
**A:** Yes! Deploy as containers, use K8s orchestration, configure ELK stack for log aggregation.

---

**🎯 This demonstrates enterprise observability used at Netflix, Uber, LinkedIn, and Amazon!**

For complete technical details → **[OBSERVABILITY_FLOW.md](./OBSERVABILITY_FLOW.md)**
