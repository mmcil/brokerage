CREATE TABLE IF NOT EXISTS customers (
                                        customer_id VARCHAR(255) PRIMARY KEY,
                                        username VARCHAR(50) UNIQUE NOT NULL,
                                        password VARCHAR(255) NOT NULL,
                                        email VARCHAR(255) NOT NULL,
                                        first_name VARCHAR(100) NOT NULL,
                                        last_name VARCHAR(100) NOT NULL,
                                        active BOOLEAN NOT NULL DEFAULT TRUE,
                                        created_date TIMESTAMP NOT NULL,
                                        last_login_date TIMESTAMP
    );

-- Create assets table
CREATE TABLE IF NOT EXISTS assets (
                                    customer_id VARCHAR(255) NOT NULL,
                                    asset_name VARCHAR(50) NOT NULL,
                                    size DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                                    usable_size DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                                    PRIMARY KEY (customer_id, asset_name)
    );

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
                                    order_id VARCHAR(255) PRIMARY KEY,
                                    customer_id VARCHAR(255) NOT NULL,
                                    asset_name VARCHAR(50) NOT NULL,
                                    order_side VARCHAR(10) NOT NULL CHECK (order_side IN ('BUY', 'SELL')),
                                    size DECIMAL(19,2) NOT NULL,
                                    price DECIMAL(19,2) NOT NULL,
                                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'MATCHED', 'CANCELED')),
                                    create_date TIMESTAMP NOT NULL
    );

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_create_date ON orders(create_date);
CREATE INDEX IF NOT EXISTS idx_assets_customer_id ON assets(customer_id);