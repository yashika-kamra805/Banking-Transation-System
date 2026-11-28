# Banking Transaction Processing System

A Java-based banking transaction processing backend built using Spring Boot, JDBC, and MySQL. The system supports customer and account management, secure transaction processing, transaction auditing, idempotency, and concurrent transaction handling.

## 1. Technologies Used

- Java 21
- Spring Boot 3.5.6
- Maven
- MySQL 8
- JDBC and JdbcTemplate
- HikariCP connection pooling
- JUnit
- Postman
- MySQL Workbench

## 2. Main Features

- Customer management
- Bank account management
- Deposit and withdrawal operations
- Money transfers between accounts
- Transaction history and search
- Transaction audit records
- Failed transaction tracking
- Idempotency key handling
- Concurrent transaction processing
- Database connection health check
- Performance benchmarking

## 3. Project Structure

```text
bankingtransactionsystem/
├── src/
│   ├── main/
│   │   ├── java/com/banking/bankingtransactionsystem/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   ├── exception/
│   │   │   └── performance/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-performance.properties
│   └── test/
├── pom.xml
└── README.md
```

## 4. Database

The application uses MySQL.

Main tables include:

- customers
- accounts
- transactions
- transaction_audit
- failed_transactions
- idempotency_key_locks

The performance benchmark uses a separate database named `banking_performance_db` to isolate benchmark transactions from the original database.

## 5. Running the Application

Configure the database connection in `application.properties`.

Do not commit database passwords or other secrets to GitHub.

Compile the project:

```bash
mvn clean compile
```

Run the application:

```bash
mvn spring-boot:run
```

The default application port is 8080.

## 6. Performance Benchmark

The performance benchmark processes 50,000 transactions for each worker configuration.

| Workers | Transactions | Successful | Failed | Time (seconds) | TPS |
|---:|---:|---:|---:|---:|---:|
| 1 | 50,000 | 50,000 | 0 | 352.94 | 141.67 |
| 5 | 50,000 | 50,000 | 0 | 106.62 | 468.94 |
| 10 | 50,000 | 50,000 | 0 | 107.04 | 467.11 |
| 20 | 50,000 | 50,000 | 0 | 112.16 | 445.80 |

**Best measured throughput:** 468.94 transactions per second with 5 workers.

All four configurations completed with zero reported failures.

## 7. API Testing

REST APIs can be tested using Postman.

The application includes endpoints for customer management, account management, transaction processing, transaction history, and database health checks.

## 8. Future Improvements

- Add a web-based administration dashboard.
- Improve database query performance.
- Add detailed monitoring and metrics.
- Improve structured logging.
- Expand automated integration testing.
- Add deployment configuration.

## 9. Security Note

Database credentials should be stored outside source control. Use environment variables or a secure configuration mechanism for sensitive values.
