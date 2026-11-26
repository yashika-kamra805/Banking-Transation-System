package com.banking.bankingtransactionsystem.repository;

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
             amount, status, description)
            VALUES (?, ?, ?, ?, ?, ?)
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

    public List<Transaction> findByAccountId(Long accountId) {
        String sql = """
                SELECT * FROM transactions
                WHERE from_account_id = ? OR to_account_id = ?
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(sql, rowMapper, accountId, accountId);
    }
}
