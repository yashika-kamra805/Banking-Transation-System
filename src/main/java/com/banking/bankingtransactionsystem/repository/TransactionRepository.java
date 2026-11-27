package com.banking.bankingtransactionsystem.repository;

import java.util.Optional;
import com.banking.bankingtransactionsystem.model.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Transaction> rowMapper = (rs, rowNum) -> {
        Transaction transaction = new Transaction();

        transaction.setTransactionId(rs.getLong("transaction_id"));

        long fromId = rs.getLong("from_account_id");
        transaction.setFromAccountId(
                rs.wasNull() ? null : fromId);

        long toId = rs.getLong("to_account_id");
        transaction.setToAccountId(
                rs.wasNull() ? null : toId);

        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setStatus(rs.getString("status"));
        transaction.setDescription(rs.getString("description"));
        transaction.setIdempotencyKey(rs.getString("idempotency_key"));

        if (rs.getTimestamp("created_at") != null) {
            transaction.setCreatedAt(
                    rs.getTimestamp("created_at").toLocalDateTime());
        }

        return transaction;
    };

    public Transaction save(Transaction transaction) {

        String sql = """
    INSERT INTO transactions
    (from_account_id, to_account_id, transaction_type,
     amount, status, description, idempotency_key)
    VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);

            if (transaction.getFromAccountId() == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, transaction.getFromAccountId());
            }

            if (transaction.getToAccountId() == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, transaction.getToAccountId());
            }

            ps.setString(3, transaction.getTransactionType());
            ps.setBigDecimal(4, transaction.getAmount());
            ps.setString(5, transaction.getStatus());
            ps.setString(6, transaction.getDescription());
            ps.setString(7, transaction.getIdempotencyKey());

            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            transaction.setTransactionId(keyHolder.getKey().longValue());
        }

        // Fetch the complete saved transaction from MySQL
        return jdbcTemplate.queryForObject(
                "SELECT * FROM transactions WHERE transaction_id = ?",
                rowMapper,
                transaction.getTransactionId()
        );
    }

    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }


    public List<Transaction> searchTransactions(
            String transactionType,
            String status,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate) {

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM transactions WHERE 1=1"
        );

        List<Object> parameters = new java.util.ArrayList<>();

        if (transactionType != null && !transactionType.isBlank()) {
            sql.append(" AND transaction_type = ?");
            parameters.add(transactionType);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            parameters.add(status);
        }

        if (startDate != null) {
            sql.append(" AND created_at >= ?");
            parameters.add(java.sql.Timestamp.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append(" AND created_at <= ?");
            parameters.add(java.sql.Timestamp.valueOf(endDate));
        }

        sql.append(" ORDER BY created_at DESC");

        return jdbcTemplate.query(
                sql.toString(),
                rowMapper,
                parameters.toArray()
        );
    }
    public List<Transaction> findByAccountId(Long accountId) {

        String sql = """
            SELECT * FROM transactions
            WHERE from_account_id = ? OR to_account_id = ?
            ORDER BY created_at DESC
            """;

        return jdbcTemplate.query(
                sql,
                rowMapper,
                accountId,
                accountId
        );
    }
    public void lockIdempotencyKey(String idempotencyKey) {

        String insertSql = """
        INSERT INTO idempotency_key_locks (idempotency_key)
        VALUES (?)
        ON DUPLICATE KEY UPDATE
        idempotency_key = VALUES(idempotency_key)
        """;

        jdbcTemplate.update(insertSql, idempotencyKey);

        jdbcTemplate.queryForObject(
                """
                SELECT idempotency_key
                FROM idempotency_key_locks
                WHERE idempotency_key = ?
                FOR UPDATE
                """,
                String.class,
                idempotencyKey
        );
    }
    public Optional<Transaction> findByIdempotencyKey(String idempotencyKey) {

        String sql = """
        SELECT * FROM transactions
        WHERE idempotency_key = ?
        """;

        List<Transaction> results = jdbcTemplate.query(
                sql,
                rowMapper,
                idempotencyKey
        );

        return results.isEmpty()
                ? Optional.empty()
                : Optional.of(results.get(0));
    }

}
