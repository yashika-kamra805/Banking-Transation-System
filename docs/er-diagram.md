
# Banking Transaction Processing System — ER Diagram

## Entity Relationship Diagram

```mermaid
erDiagram
    CUSTOMERS ||--o{ ACCOUNTS : owns
    ACCOUNTS ||--o{ TRANSACTIONS : sends
    ACCOUNTS ||--o{ TRANSACTIONS : receives
    TRANSACTIONS ||--o{ TRANSACTION_AUDIT : has

    CUSTOMERS {
        bigint customer_id PK
        varchar full_name
        varchar email UK
        varchar phone
        varchar address
        timestamp created_at
    }

    ACCOUNTS {
        bigint account_id PK
        bigint customer_id FK
        varchar account_number UK
        varchar account_type
        decimal balance
        varchar status
        timestamp created_at
    }

    TRANSACTIONS {
        bigint transaction_id PK
        bigint from_account_id FK
        bigint to_account_id FK
        varchar transaction_type
        decimal amount
        varchar status
        varchar description
        timestamp created_at
        varchar idempotency_key UK
    }

    TRANSACTION_AUDIT {
        bigint audit_id PK
        bigint transaction_id FK
        varchar action
        text details
        timestamp created_at
    }

    FAILED_TRANSACTIONS {
        bigint failed_id PK
        bigint transaction_id
        text error_message
        timestamp attempted_at
    }

    IDEMPOTENCY_KEY_LOCKS {
        varchar idempotency_key PK
        timestamp created_at
    }
```

## Relationship Description

1. One customer can own multiple accounts.
2. One account can send multiple transactions.
3. One account can receive multiple transactions.
4. One transaction can have multiple audit records.
5. Failed transactions store information about unsuccessful operations.
6. Idempotency key locks help coordinate requests using the same idempotency key.
