USE banking_db;

-- Insert sample customers without duplicating emails
INSERT INTO customers (full_name, email, phone, address)
VALUES
('Sample Customer 1', 'sample1@example.com', '9876500001', 'Delhi'),
('Sample Customer 2', 'sample2@example.com', '9876500002', 'Mumbai'),
('Sample Customer 3', 'sample3@example.com', '9876500003', 'Jaipur'),
('Sample Customer 4', 'sample4@example.com', '9876500004', 'Pune')
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Insert sample accounts using customer email to find the correct IDs
INSERT IGNORE INTO accounts
(customer_id, account_number, account_type, balance, status)
SELECT customer_id, '9100000000000001', 'SAVINGS', 10000.00, 'ACTIVE'
FROM customers WHERE email = 'sample1@example.com'
UNION ALL
SELECT customer_id, '9100000000000002', 'SAVINGS', 15000.00, 'ACTIVE'
FROM customers WHERE email = 'sample2@example.com'
UNION ALL
SELECT customer_id, '9100000000000003', 'CURRENT', 20000.00, 'ACTIVE'
FROM customers WHERE email = 'sample3@example.com'
UNION ALL
SELECT customer_id, '9100000000000004', 'SAVINGS', 25000.00, 'ACTIVE'
FROM customers WHERE email = 'sample4@example.com';