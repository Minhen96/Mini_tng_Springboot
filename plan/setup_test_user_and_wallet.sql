-- setup_test_users.sql

-- Insert Alice
INSERT INTO users (id, email, name, password, status, email_verified)
VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'alice@example.com',
  'Alice',
  '$2a$10$...',  -- Hashed password
  'ACTIVE',
  true
);

-- Insert Bob
INSERT INTO users (id, email, name, password, status, email_verified)
VALUES (
  'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'bob@example.com',
  'Bob',
  '$2a$10$...',
  'ACTIVE',
  true
);

-- Create Alice's wallet with $1000
INSERT INTO wallets (id, user_id, balance, frozen_balance, unreleased_balance)
VALUES (
  gen_random_uuid(),
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  1000.00,
  0.00,
  0.00
);

-- Create Bob's wallet with $0
INSERT INTO wallets (id, user_id, balance, frozen_balance, unreleased_balance)
VALUES (
  gen_random_uuid(),
  'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  0.00,
  0.00,
  0.00
);