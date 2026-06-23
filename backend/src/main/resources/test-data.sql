-- PostgreSQL test data (run via psql or any SQL client)
-- psql -h localhost -U testuser -d testdb -f test-data.sql

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT now()
);

INSERT INTO users (username, email, full_name, status) VALUES
    ('alpha_coder', 'alpha@proton.me', 'Alice Chen', 'ACTIVE'),
    ('dev_ops_ninja', 'ninja@gmail.com', 'Bob Martinez', 'ACTIVE'),
    ('skyline_blue', 'skyline@outlook.com', 'Carlos Silva', 'ACTIVE'),
    ('pixel_pusha', 'ppusha@fastmail.com', 'Diana Kim', 'ACTIVE'),
    ('data_wizard', 'wizard@icloud.com', 'Elena Voss', 'SUSPENDED')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT now()
);

INSERT INTO orders (user_id, amount, status)
SELECT id, floor(random() * 1000 + 10)::numeric(10,2),
       CASE WHEN random() < 0.3 THEN 'PENDING' WHEN random() < 0.7 THEN 'COMPLETED' ELSE 'CANCELLED' END
FROM users
CROSS JOIN generate_series(1, 20);
