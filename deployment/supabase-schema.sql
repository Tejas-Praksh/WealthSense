-- WealthSense consolidated schema for Supabase (PostgreSQL).
-- Run in SQL Editor on a fresh database before SPRING_PROFILES_ACTIVE=production (ddl-auto: validate).
-- Adjust FKs if you split databases per service.

-- Users (user-service)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255),
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone VARCHAR(15),
  role VARCHAR(50) DEFAULT 'USER',
  enabled BOOLEAN DEFAULT false,
  email_verified BOOLEAN DEFAULT false,
  account_locked BOOLEAN DEFAULT false,
  failed_login_attempts INT DEFAULT 0,
  provider VARCHAR(50) DEFAULT 'LOCAL',
  provider_id VARCHAR(255),
  profile_image_url VARCHAR(500),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  last_login_at TIMESTAMPTZ
);

-- Refresh tokens (user-service)
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  token VARCHAR(500) UNIQUE NOT NULL,
  expiry_date TIMESTAMPTZ NOT NULL,
  revoked BOOLEAN DEFAULT false,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Accounts (transaction-service)
CREATE TABLE IF NOT EXISTS accounts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  account_number VARCHAR(20) UNIQUE,
  balance DECIMAL(19, 4) DEFAULT 0,
  available_balance DECIMAL(19, 4) DEFAULT 0,
  currency VARCHAR(3) DEFAULT 'INR',
  account_type VARCHAR(50),
  is_active BOOLEAN DEFAULT true,
  version BIGINT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Transactions (transaction-service)
CREATE TABLE IF NOT EXISTS transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  account_id UUID NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
  amount DECIMAL(19, 4) NOT NULL,
  currency VARCHAR(3) DEFAULT 'INR',
  type VARCHAR(20) NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  category VARCHAR(100),
  merchant_name VARCHAR(255),
  merchant_id VARCHAR(255),
  description VARCHAR(500),
  idempotency_key VARCHAR(255) UNIQUE,
  correlation_id VARCHAR(255),
  metadata JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  processed_at TIMESTAMPTZ
);

-- Outbox (transaction-service) — payload stored as JSON text compatible with JPA JSON mapping
CREATE TABLE IF NOT EXISTS outbox_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aggregate_id UUID NOT NULL,
  event_type VARCHAR(100),
  payload TEXT NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  topic VARCHAR(255),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  processed_at TIMESTAMPTZ,
  retry_count INT DEFAULT 0
);

-- Fraud alerts (fraud-detection-service)
CREATE TABLE IF NOT EXISTS fraud_alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  transaction_id UUID NOT NULL,
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  severity VARCHAR(20),
  risk_score DOUBLE PRECISION,
  rule_triggered VARCHAR(255),
  recommended_action VARCHAR(50),
  reason VARCHAR(500),
  evidence JSONB,
  status VARCHAR(20) DEFAULT 'OPEN',
  created_at TIMESTAMPTZ DEFAULT NOW(),
  resolved_at TIMESTAMPTZ
);

-- Investments (investment-service)
CREATE TABLE IF NOT EXISTS investments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  name VARCHAR(255),
  type VARCHAR(50),
  amount_paise DECIMAL(19, 4),
  monthly_sip_paise DECIMAL(19, 4),
  expected_return_rate DECIMAL(5, 2),
  start_date DATE,
  end_date DATE,
  current_value_paise DECIMAL(19, 4),
  status VARCHAR(20),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Goals (investment-service)
CREATE TABLE IF NOT EXISTS goals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  name VARCHAR(255),
  target_amount_paise DECIMAL(19, 4),
  current_amount_paise DECIMAL(19, 4),
  monthly_saving_paise DECIMAL(19, 4),
  target_date DATE,
  status VARCHAR(20) DEFAULT 'ACTIVE',
  priority INT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Optional: notification audit in SQL (notification-service is mostly Kafka/Rabbit; add if you persist logs here)
CREATE TABLE IF NOT EXISTS notification_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  channel VARCHAR(50),
  recipient VARCHAR(255),
  subject VARCHAR(500),
  body TEXT,
  status VARCHAR(50),
  error_message TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions (status);
CREATE INDEX IF NOT EXISTS idx_fraud_alerts_user_id ON fraud_alerts (user_id);
CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox_events (status);
