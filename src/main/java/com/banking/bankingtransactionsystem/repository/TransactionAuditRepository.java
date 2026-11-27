
package com.banking.bankingtransactionsystem.repository;

import com.banking.bankingtransactionsystem.model.TransactionAudit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class TransactionAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TransactionAudit> auditRowMapper =
            (rs, rowNum) -> {
                TransactionAudit audit = new TransactionAudit();

                audit.setAuditId(rs.getLong("audit_id"));
                audit.setTransactionId(
                        rs.getLong("transaction_id"));
                audit.setAction(rs.getString("action"));
                audit.setDetails(rs.getString("details"));

                Timestamp timestamp =
                        rs.getTimestamp("created_at");

                if (timestamp != null) {
                    audit.setCreatedAt(
                            timestamp.toLocalDateTime());
                }

                return audit;
            };

    public void saveAudit(Long transactionId,
                          String action,
                          String details) {

        String sql = """
                INSERT INTO transaction_audit
                (transaction_id, action, details)
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.update(sql, transactionId, action, details);
    }

    public List<TransactionAudit> findAll() {

        String sql = """
                SELECT * FROM transaction_audit
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(sql, auditRowMapper);
    }

    public List<TransactionAudit> findByTransactionId(
            Long transactionId) {

        String sql = """
                SELECT * FROM transaction_audit
                WHERE transaction_id = ?
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                auditRowMapper,
                transactionId
        );
    }
}
