-- MySQL test data
-- mysql -h localhost -u testuser -p testdb < test-data-mysql.sql

CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO users (username, email, full_name, status) VALUES
    ('alpha_coder', 'alpha@proton.me', 'Alice Chen', 'ACTIVE'),
    ('dev_ops_ninja', 'ninja@gmail.com', 'Bob Martinez', 'ACTIVE'),
    ('skyline_blue', 'skyline@outlook.com', 'Carlos Silva', 'ACTIVE'),
    ('pixel_pusha', 'ppusha@fastmail.com', 'Diana Kim', 'ACTIVE'),
    ('data_wizard', 'wizard@icloud.com', 'Elena Voss', 'SUSPENDED');

CREATE TABLE IF NOT EXISTS orders (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36),
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO orders (user_id, amount, status)
SELECT id, ROUND(RAND() * 1000 + 10, 2),
       CASE WHEN RAND() < 0.3 THEN 'PENDING' WHEN RAND() < 0.7 THEN 'COMPLETED' ELSE 'CANCELLED' END
FROM users, (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
             UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
             UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
             UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20) nums;
