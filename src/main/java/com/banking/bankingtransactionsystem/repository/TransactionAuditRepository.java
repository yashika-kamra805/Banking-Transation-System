package com.banking.bankingtransactionsystem.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAudit(Long transactionId, String action, String details) {

        String sql = """
                INSERT INTO transaction_audit
                (transaction_id, action, details)
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.update(sql, transactionId, action, details);
    }
}
