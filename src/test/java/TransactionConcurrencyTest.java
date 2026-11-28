package com.banking.bankingtransactionsystem;

import com.banking.bankingtransactionsystem.exception.InsufficientBalanceException;
import com.banking.bankingtransactionsystem.model.Transaction;
import com.banking.bankingtransactionsystem.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransactionConcurrencyTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void onlyOneWithdrawalShouldSucceedWhenBalanceIsInsufficientForBoth()
            throws Exception {

        Long accountId = 4L;

        BigDecimal originalBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM accounts WHERE account_id = ?",
                BigDecimal.class,
                accountId
        );

        assertTrue(originalBalance != null,
                "Test account must exist");

        String key1 = "TEST-" + UUID.randomUUID();
        String key2 = "TEST-" + UUID.randomUUID();

        long previousFailedId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(failed_id), 0) FROM failed_transactions",
                Long.class
        );

        try {
            jdbcTemplate.update(
                    "UPDATE accounts SET balance = 10000 WHERE account_id = ?",
                    accountId
            );

            CountDownLatch startSignal = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<Boolean> withdrawal1 = executor.submit(() -> {
                    startSignal.await();
                    try {
                        transactionService.withdraw(
                                accountId,
                                new BigDecimal("7000"),
                                "Concurrency test withdrawal 7000",
                                key1
                        );
                        return true;
                    } catch (InsufficientBalanceException exception) {
                        return false;
                    }
                });

                Future<Boolean> withdrawal2 = executor.submit(() -> {
                    startSignal.await();
                    try {
                        transactionService.withdraw(
                                accountId,
                                new BigDecimal("5000"),
                                "Concurrency test withdrawal 5000",
                                key2
                        );
                        return true;
                    } catch (IllegalArgumentException exception) {
                        return false;
                    }
                });

                startSignal.countDown();

                boolean result1 = withdrawal1.get();
                boolean result2 = withdrawal2.get();

                int successfulWithdrawals =
                        (result1 ? 1 : 0) + (result2 ? 1 : 0);

                assertEquals(1, successfulWithdrawals,
                        "Exactly one withdrawal should succeed");

                BigDecimal finalBalance = jdbcTemplate.queryForObject(
                        "SELECT balance FROM accounts WHERE account_id = ?",
                        BigDecimal.class,
                        accountId
                );

                assertTrue(finalBalance.compareTo(BigDecimal.ZERO) >= 0,
                        "Account balance must never be negative");

                assertTrue(
                        finalBalance.compareTo(new BigDecimal("3000")) == 0
                                || finalBalance.compareTo(new BigDecimal("5000")) == 0,
                        "Final balance should be 3000 or 5000"
                );

            } finally {
                executor.shutdownNow();
            }

        } finally {
            jdbcTemplate.update(
                    "DELETE FROM transaction_audit WHERE transaction_id IN " +
                            "(SELECT transaction_id FROM transactions " +
                            "WHERE idempotency_key IN (?, ?))",
                    key1, key2
            );

            jdbcTemplate.update(
                    "DELETE FROM transactions WHERE idempotency_key IN (?, ?)",
                    key1, key2
            );

            jdbcTemplate.update(
                    "DELETE FROM failed_transactions WHERE failed_id > ?",
                    previousFailedId
            );

            jdbcTemplate.update(
                    "UPDATE accounts SET balance = ? WHERE account_id = ?",
                    originalBalance, accountId
            );
        }
    }
}
