
INSERT INTO products (name, price, stock, category, is_active, created_at) VALUES
('iPhone 15 Pro', 1299.99, 50, 'Electronics', true, CURRENT_TIMESTAMP),
('Samsung Galaxy S24', 999.99, 30, 'Electronics', true, CURRENT_TIMESTAMP),
('MacBook Pro M3', 2499.99, 20, 'Computers', true, CURRENT_TIMESTAMP),
('Dell XPS 15', 1799.99, 15, 'Computers', true, CURRENT_TIMESTAMP),
('Sony WH-1000XM5', 349.99, 100, 'Audio', true, CURRENT_TIMESTAMP),
('Nike Air Max 270', 150.00, 200, 'Shoes', true, CURRENT_TIMESTAMP),
('Adidas Ultraboost 23', 180.00, 150, 'Shoes', true, CURRENT_TIMESTAMP),
('Samsung 4K TV 55"', 799.99, 25, 'Electronics', true, CURRENT_TIMESTAMP),
('iPad Pro 12.9', 1099.99, 40, 'Electronics', true, CURRENT_TIMESTAMP),
('Logitech MX Master 3', 99.99, 75, 'Accessories', true, CURRENT_TIMESTAMP),
('Mechanical Keyboard', 149.99, 60, 'Accessories', true, CURRENT_TIMESTAMP),
('Out of Stock Item', 49.99, 0, 'Test', false, CURRENT_TIMESTAMP);


INSERT INTO orders (customer_name, customer_email, order_date, status, total_amount) VALUES
('Alisher Karimov', 'alisher@example.com', CURRENT_TIMESTAMP, 'DELIVERED', 1449.98),
('Malika Yusupova', 'malika@example.com', CURRENT_TIMESTAMP, 'CONFIRMED', 999.99),
('Bobur Toshmatov', 'bobur@example.com', CURRENT_TIMESTAMP, 'PENDING', 2649.98),
('Nodira Rahimova', 'nodira@example.com', CURRENT_TIMESTAMP, 'SHIPPED', 349.99),
('Jasur Mirzayev', 'jasur@example.com', CURRENT_TIMESTAMP, 'CANCELLED', 150.00);


INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES
(1, 1, 1, 1299.99, 1299.99),
(1, 10, 1, 99.99, 99.99),
(2, 2, 1, 999.99, 999.99),
(3, 3, 1, 2499.99, 2499.99),
(3, 11, 1, 149.99, 149.99),
(4, 5, 1, 349.99, 349.99),
(5, 6, 1, 150.00, 150.00);
