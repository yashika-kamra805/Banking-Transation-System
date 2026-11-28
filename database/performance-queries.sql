USE banking_db;

-- 1. Top 10 accounts by transaction volume
SELECT
    account_id,
    COUNT(*) AS transaction_count
FROM (
    SELECT from_account_id AS account_id
    FROM transactions
    WHERE from_account_id IS NOT NULL

    UNION ALL

    SELECT to_account_id AS account_id
    FROM transactions
    WHERE to_account_id IS NOT NULL
) AS account_transactions
GROUP BY account_id
ORDER BY transaction_count DESC
LIMIT 10;


-- 2. Failed transactions grouped by reason
SELECT
    error_message,
    COUNT(*) AS failure_count
FROM failed_transactions
GROUP BY error_message
ORDER BY failure_count DESC;


-- 3. Daily transaction amount by transaction type
SELECT
    DATE(created_at) AS transaction_date,
    transaction_type,
    COUNT(*) AS transaction_count,
    SUM(amount) AS total_amount
FROM transactions
GROUP BY DATE(created_at), transaction_type
ORDER BY transaction_date DESC;


-- 4. Accounts with more than N transactions
-- Change 5 to your required threshold.
SELECT
    account_id,
    COUNT(*) AS transaction_count
FROM (
    SELECT from_account_id AS account_id
    FROM transactions
    WHERE from_account_id IS NOT NULL

    UNION ALL

    SELECT to_account_id AS account_id
    FROM transactions
    WHERE to_account_id IS NOT NULL
) AS account_transactions
GROUP BY account_id
HAVING COUNT(*) > 5
ORDER BY transaction_count DESC;


-- 5. Complete transaction history for a customer
-- Change 1 to the required customer ID.
SELECT
    c.customer_id,
    c.full_name,
    t.transaction_id,
    t.from_account_id,
    t.to_account_id,
    t.transaction_type,
    t.amount,
    t.status,
    t.description,
    t.created_at
FROM customers c
JOIN accounts a
    ON a.customer_id = c.customer_id
JOIN transactions t
    ON t.from_account_id = a.account_id
    OR t.to_account_id = a.account_id
WHERE c.customer_id = 1
ORDER BY t.created_at DESC;