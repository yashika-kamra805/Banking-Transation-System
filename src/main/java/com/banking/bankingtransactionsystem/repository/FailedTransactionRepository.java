package com.banking.bankingtransactionsystem.repository;

import com.banking.bankingtransactionsystem.model.FailedTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class FailedTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public FailedTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FailedTransaction> rowMapper =
            (rs, rowNum) -> {
                FailedTransaction failed = new FailedTransaction();

                failed.setFailedId(rs.getLong("failed_id"));

                long transactionId =
                        rs.getLong("transaction_id");

                failed.setTransactionId(
                        rs.wasNull() ? null : transactionId
                );

                failed.setErrorMessage(
                        rs.getString("error_message")
                );

                Timestamp timestamp =
                        rs.getTimestamp("attempted_at");

                if (timestamp != null) {
                    failed.setAttemptedAt(
                            timestamp.toLocalDateTime()
                    );
                }

                return failed;
            };

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

    public List<FailedTransaction> findAll() {

        String sql = """
                SELECT * FROM failed_transactions
                ORDER BY attempted_at DESC
                """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<FailedTransaction> findByTransactionId(
            Long transactionId) {

        String sql = """
                SELECT * FROM failed_transactions
                WHERE transaction_id = ?
                ORDER BY attempted_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                rowMapper,
                transactionId
        );
    }
}
