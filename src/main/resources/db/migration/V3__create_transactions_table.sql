CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    description VARCHAR(500),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_id BIGINT NOT NULL,
    target_account_id BIGINT,
    reversed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_target_account FOREIGN KEY (target_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_target_account_id ON transactions(target_account_id);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_reversed ON transactions(reversed);

-- Constraint para garantir que o valor seja positivo
ALTER TABLE transactions ADD CONSTRAINT chk_amount_positive CHECK (amount > 0);

