-- Database Setup Script for PostgreSQL
-- Run these commands in your PostgreSQL terminal or pgAdmin

-- 1. Create the database
CREATE DATABASE expense_tracker;

-- 2. Create the users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- 3. Create the transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    date VARCHAR(255) NOT NULL,
    income BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Note: Hibernate will automatically create these tables if spring.jpa.hibernate.ddl-auto=update is set.
