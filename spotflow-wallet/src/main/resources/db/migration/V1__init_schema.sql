CREATE TABLE wallet (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    balance NUMERIC(19,4) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE wallet_transaction (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallet (id),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference VARCHAR(100) NOT NULL UNIQUE,
    spotflow_reference VARCHAR(100),
    webhook_event_id VARCHAR(100) UNIQUE,
    dynamic_account_number VARCHAR(20),
    dynamic_account_bank VARCHAR(100),
    dynamic_account_expires_at TIMESTAMP,
    destination_account_number VARCHAR(20),
    destination_bank_code VARCHAR(20),
    destination_account_name VARCHAR(150),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX idx_wallet_transaction_webhook_event_id ON wallet_transaction (webhook_event_id);
CREATE INDEX idx_wallet_transaction_status_created_at ON wallet_transaction (status, created_at);
