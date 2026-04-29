-- Initial migration: members table with password hash and role
CREATE TABLE IF NOT EXISTS members (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL
);

-- You can insert the initial known members here with placeholder passwords to be reset.
-- Example (replace with secure hashes later):
-- INSERT INTO members (name, password_hash, role) VALUES ('Thomas', '$2a$10$...', 'ROLE_MEMBER');
