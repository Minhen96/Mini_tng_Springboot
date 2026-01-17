-- V4: Create Audit Logs Table for Compliance
--
-- This table records ALL important events in the system for:
-- - Regulatory compliance (PCI DSS, GDPR, MAS, etc.)
-- - Fraud investigation
-- - Dispute resolution
-- - Security analysis
-- - Business analytics
--
-- CRITICAL: This is an APPEND-ONLY table
-- - Never UPDATE records
-- - Never DELETE records (kept forever for legal compliance)

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- When the event occurred
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- What action occurred
    -- Examples: TRANSFER_INITIATED, USER_LOGIN, ADMIN_DELETE_USER
    action VARCHAR(50) NOT NULL,
    
    -- Transaction ID (for wallet transfers)
    transaction_id VARCHAR(255),
    
    -- User columns (sender and receiver)
    from_user_id UUID,
    to_user_id UUID,
    
    -- Wallet-specific fields (nullable for non-wallet events)
    from_wallet_id UUID,
    to_wallet_id UUID,
    amount DECIMAL(18, 2),
    
    -- Current status at time of audit
    status VARCHAR(50),
    
    -- Severity level for alerting: LOW, MEDIUM, HIGH, CRITICAL
    severity VARCHAR(20) DEFAULT 'LOW',
    
    -- Security context
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    
    -- Additional metadata (JSON format)
    -- Can store full snapshots, error messages, domain-specific data
    metadata TEXT,
    
    -- Human-readable description
    description VARCHAR(1000),
    
    -- Auto-populated timestamp
    created_at TIMESTAMP DEFAULT NOW()
);

-- ========================================
-- Indexes for Performance
-- ========================================

-- Most common query: Recent events by time
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);

-- User activity tracking (queries by sender)
CREATE INDEX idx_audit_logs_from_user_id ON audit_logs(from_user_id, timestamp DESC);
CREATE INDEX idx_audit_logs_to_user_id ON audit_logs(to_user_id, timestamp DESC);

-- Transaction history
CREATE INDEX idx_audit_logs_transaction_id ON audit_logs(transaction_id);

-- High-severity events (for alerting)
-- Partial index - only indexes rows matching the WHERE clause
CREATE INDEX idx_audit_logs_high_severity 
    ON audit_logs(timestamp DESC) 
    WHERE severity IN ('HIGH', 'CRITICAL');

-- Action-based queries (e.g., all LOGIN events)
CREATE INDEX idx_audit_logs_action ON audit_logs(action, timestamp DESC);

-- ========================================
-- Comments for Documentation
-- ========================================

COMMENT ON TABLE audit_logs IS 'Append-only compliance audit log for all system events';
COMMENT ON COLUMN audit_logs.action IS 'Action type: TRANSFER_INITIATED, USER_LOGIN, etc.';
COMMENT ON COLUMN audit_logs.severity IS 'Alert level: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN audit_logs.metadata IS 'JSON metadata with full event context';
