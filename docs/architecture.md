
# Banking Transaction Processing System — Architecture

## System Architecture

```mermaid
flowchart TD
    A[Client / Postman] --> B[Spring Boot Application]

    B --> C[REST Controllers]

    C --> D[Customer Controller]
    C --> E[Account Controller]
    C --> F[Transaction Controller]
    C --> G[Health Controller]

    D --> H[Service Layer]
    E --> H
    F --> H
    G --> I[Database Health Check]

    H --> J[Repository / JDBC Layer]
    J --> K[(MySQL Database)]

    K --> L[Customers]
    K --> M[Accounts]
    K --> N[Transactions]
    K --> O[Transaction Audit]
    K --> P[Failed Transactions]
    K --> Q[Idempotency Key Locks]
```

## Main Components

1. **Client:** Sends HTTP requests using Postman.
2. **Controller Layer:** Handles REST API requests.
3. **Service Layer:** Contains banking business logic.
4. **Repository / JDBC Layer:** Executes SQL operations using JDBC.
5. **MySQL:** Stores customer, account, transaction, audit, and failure records.

## Transaction Processing

- Deposit
- Withdrawal
- Transfer
- Account locking
- Idempotency handling
- Transaction auditing
- Failed transaction tracking

## Performance Testing

The performance testing component executes concurrent deposits using 1, 5, 10, and 20 worker threads against the separate performance database.
