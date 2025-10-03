CREATE DATABASE IF NOT EXISTS telemetry_learning;
USE telemetry_learning;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(255) PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status)
);

-- Insert some sample data
INSERT INTO users (id, username, email, status) VALUES 
('user-1', 'alice.johnson', 'alice@example.com', 'ACTIVE'),
('user-2', 'bob.smith', 'bob@example.com', 'ACTIVE')
ON DUPLICATE KEY UPDATE username=VALUES(username);

INSERT INTO orders (id, customer_id, amount, currency, status) VALUES 
('order-1', 'user-1', 99.99, 'USD', 'COMPLETED'),
('order-2', 'user-2', 149.50, 'EUR', 'CREATED')
ON DUPLICATE KEY UPDATE amount=VALUES(amount);
