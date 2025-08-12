-- Sample data for Brokerage Trading System

-- Insert sample customers (passwords are encoded with BCrypt)
-- Password for all customers is "password123"
INSERT INTO customers (customer_id, username, password, email, first_name, last_name, active, created_date) VALUES
                                                                                                                ('CUST001', 'john_doe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JiOV7oqv6gN3hBv.S', 'john.doe@example.com', 'John', 'Doe', true, '2025-01-01 10:00:00'),
                                                                                                                ('CUST002', 'jane_smith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JiOV7oqv6gN3hBv.S', 'jane.smith@example.com', 'Jane', 'Smith', true, '2025-01-01 11:00:00'),
                                                                                                                ('CUST003', 'bob_wilson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM1JiOV7oqv6gN3hBv.S', 'bob.wilson@example.com', 'Bob', 'Wilson', true, '2025-01-01 12:00:00');

-- Insert sample assets (including TRY for all customers)
INSERT INTO assets (customer_id, asset_name, size, usable_size) VALUES
-- Customer 1 assets
('CUST001', 'TRY', 10000.00, 8500.00),
('CUST001', 'AAPL', 50.00, 50.00),
('CUST001', 'GOOGL', 20.00, 15.00),

-- Customer 2 assets
('CUST002', 'TRY', 15000.00, 12000.00),
('CUST002', 'MSFT', 30.00, 25.00),
('CUST002', 'TSLA', 10.00, 10.00),

-- Customer 3 assets
('CUST003', 'TRY', 5000.00, 5000.00),
('CUST003', 'AAPL', 25.00, 20.00);

-- Insert sample orders
INSERT INTO orders (order_id, customer_id, asset_name, order_side, size, price, status, create_date) VALUES
                                                                                                         ('ORD001', 'CUST001', 'AAPL', 'BUY', 10.00, 150.00, 'PENDING', '2025-01-15 09:00:00'),
                                                                                                         ('ORD002', 'CUST001', 'GOOGL', 'SELL', 5.00, 2800.00, 'PENDING', '2025-01-15 09:30:00'),
                                                                                                         ('ORD003', 'CUST002', 'MSFT', 'SELL', 5.00, 420.00, 'PENDING', '2025-01-15 10:00:00'),
                                                                                                         ('ORD004', 'CUST002', 'TSLA', 'BUY', 2.00, 250.00, 'MATCHED', '2025-01-14 14:00:00'),
                                                                                                         ('ORD005', 'CUST003', 'AAPL', 'SELL', 5.00, 155.00, 'CANCELED', '2025-01-14 16:00:00');