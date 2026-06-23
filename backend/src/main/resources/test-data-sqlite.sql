-- SQLite test data
-- sqlite3 testdb.db < test-data-sqlite.sql

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    full_name TEXT,
    status TEXT DEFAULT 'ACTIVE',
    created_at TEXT DEFAULT (datetime('now'))
);

INSERT OR IGNORE INTO users (username, email, full_name, status) VALUES
    ('alpha_coder', 'alpha@proton.me', 'Alice Chen', 'ACTIVE'),
    ('dev_ops_ninja', 'ninja@gmail.com', 'Bob Martinez', 'ACTIVE'),
    ('skyline_blue', 'skyline@outlook.com', 'Carlos Silva', 'ACTIVE'),
    ('pixel_pusha', 'ppusha@fastmail.com', 'Diana Kim', 'ACTIVE'),
    ('data_wizard', 'wizard@icloud.com', 'Elena Voss', 'SUSPENDED');

CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER REFERENCES users(id),
    amount REAL NOT NULL,
    status TEXT DEFAULT 'PENDING',
    created_at TEXT DEFAULT (datetime('now'))
);

INSERT INTO orders (user_id, amount, status)
SELECT id, ABS(RANDOM() % 1000) + 10.00,
       CASE WHEN ABS(RANDOM() % 3) = 0 THEN 'PENDING' WHEN ABS(RANDOM() % 3) = 1 THEN 'COMPLETED' ELSE 'CANCELLED' END
FROM users;
