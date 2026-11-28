# Banking Transaction Processing System
# Performance Benchmark Report

## 1. Objective

Evaluate the transaction processing performance of the banking application under different concurrency levels.

## 2. Test Configuration

| Parameter | Configuration |
|---|---|
| Programming language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Database | MySQL 8 |
| Connection pool | HikariCP |
| Maximum pool size | 25 |
| Minimum idle connections | 5 |
| Transactions per configuration | 50,000 |
| Worker configurations | 1, 5, 10, 20 |
| Transaction type | Deposit |
| Transaction amount | ₹1.00 |
| Benchmark database | banking_performance_db |

## 3. Benchmark Results

| Workers | Total Transactions | Successful | Failed | Execution Time (seconds) | TPS |
|---:|---:|---:|---:|---:|---:|
| 1 | 50,000 | 50,000 | 0 | 352.9441 | 141.67 |
| 5 | 50,000 | 50,000 | 0 | 106.6242 | 468.94 |
| 10 | 50,000 | 50,000 | 0 | 107.0408 | 467.11 |
| 20 | 50,000 | 50,000 | 0 | 112.1569 | 445.80 |

## 4. Overall Results

- Total transactions processed: 200,000
- Successful transactions: 200,000
- Failed transactions reported by the benchmark: 0
- Overall success rate: 100%
- Best throughput: 468.94 transactions per second
- Best worker configuration: 5 workers

## 5. Performance Analysis

The single-worker configuration achieved approximately 141.67 TPS.

Increasing concurrency to five workers improved throughput to approximately 468.94 TPS.

The ten-worker configuration achieved similar throughput at 467.11 TPS.

The twenty-worker configuration achieved 445.80 TPS, showing that additional worker threads did not improve throughput.

Five workers provided the best measured performance in this test environment.

## 6. Database Verification

The four dedicated benchmark accounts were checked after testing.

Each account started with ₹100,000 and received ₹50,100 in deposits, including the preliminary trials.

Final balance per benchmark account: ₹150,100.

The transaction records and account balances were checked using MySQL queries.

## 7. Conclusion

The performance benchmark successfully processed 200,000 transactions across four worker configurations, with zero reported failures.

The best measured throughput was approximately 468.94 transactions per second using five worker threads.

Further investigation of database query execution, locking, connection-pool utilization, and thread scheduling may help identify performance limitations.

## 8. Important Notes

These results represent the tested local development environment. They should not be treated as production capacity measurements.

The benchmark used deposit operations and four dedicated accounts. Results may differ for transfers, withdrawals, different hardware, or production workloads.