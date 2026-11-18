--liquibase formatted sql

--changeset minibank:003-create-transactions-index dbms:postgresql
CREATE INDEX idx_transactions_account_date
    ON transactions (account_id, amount, type, transaction_date);

--rollback DROP INDEX idx_transactions_account_date;
