DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, version INT, name VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL, username VARCHAR(255),
enabled BOOLEAN NOT NULL, roles VARCHAR(500), created_by VARCHAR(100), last_modified_by VARCHAR(100), created_date TIMESTAMP, last_modified_date TIMESTAMP);