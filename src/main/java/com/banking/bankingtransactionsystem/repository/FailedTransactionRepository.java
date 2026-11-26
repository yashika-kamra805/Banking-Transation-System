package com.banking.bankingtransactionsystem.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FailedTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public FailedTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveFailedTransaction(
            Long transactionId,
            String errorMessage) {

        String sql = """
                INSERT INTO failed_transactions
                (transaction_id, error_message)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, transactionId, errorMessage);
    }
}
