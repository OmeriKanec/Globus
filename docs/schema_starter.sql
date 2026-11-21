-- MiniBank — стартовая схема для модуля 4.
--
-- Это ЧЕРНОВИК, а не готовое решение — в нём намеренно не хватает
-- нескольких вещей, которые нужно добавить самостоятельно:
--   - FOREIGN KEY между таблицами
--   - UNIQUE на email клиента
--   - индекс(ы) под запрос "все транзакции счёта за период"
--
-- Задание модуля 4 — не просто скопировать это, а доработать и
-- объяснить, почему каждое добавленное ограничение/индекс нужен.

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE --нельзя регать несколько аккаунтов на одну почту
);

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id), --нужно чтобы нельзя было создать счёт, который никому не принадлежит
    account_number VARCHAR(34) NOT NULL,
    balance NUMERIC(19,4) NOT NULL DEFAULT 0
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id), --нужно чтобы нельзя было создать транзакцию, которая не принадлежит аккаунту
    amount NUMERIC(19,4) NOT NULL,
    type VARCHAR(20) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_account_date
    ON transactions (account_id, amount, type, transaction_date);
