--liquibase formatted sql

--changeset minibank:002-insert-test-data dbms:postgresql splitStatements:true
INSERT INTO clients (name, email)
SELECT 'Test Client ' || n,
       'client' || n || '@minibank.example'
FROM generate_series(1, 100) AS clients(n);

INSERT INTO accounts (client_id, account_number)
SELECT c.id,
       'TEST-' || lpad(c.id::text, 10, '0') || '-' || n
FROM clients AS c
CROSS JOIN generate_series(1, 10) AS accounts(n)
WHERE c.email IN (
    SELECT 'client' || n || '@minibank.example'
    FROM generate_series(1, 100) AS clients(n)
)
ORDER BY c.id, n;


INSERT INTO transactions (account_id, amount, type, transaction_date)
SELECT a.id,
       (100 + (n % 20) * 10)::numeric(19,4),
       'DEPOSIT',
       TIMESTAMP '2026-01-01 00:00:00' + (n - 1) * INTERVAL '3 days'
FROM accounts AS a
CROSS JOIN generate_series(1, 100) AS operations(n)
WHERE a.account_number LIKE 'TEST-%'
ORDER BY a.id, n;

UPDATE accounts AS a
SET balance = totals.balance
FROM (
    SELECT account_id, SUM(amount) AS balance
    FROM transactions
    GROUP BY account_id
) AS totals
WHERE a.id = totals.account_id
  AND a.account_number LIKE 'TEST-%';
