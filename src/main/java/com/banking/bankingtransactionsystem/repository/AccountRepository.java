package com.banking.bankingtransactionsystem.repository;

import com.banking.bankingtransactionsystem.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> {
        Account account = new Account();

        account.setAccountId(rs.getLong("account_id"));
        account.setCustomerId(rs.getLong("customer_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setAccountType(rs.getString("account_type"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setStatus(rs.getString("status"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            account.setCreatedAt(timestamp.toLocalDateTime());
        }

        return account;
    };

    public Account save(Account account) {

        String sql = """
                INSERT INTO accounts
                (customer_id, account_number, account_type, balance, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
            );

            ps.setLong(1, account.getCustomerId());
            ps.setString(2, account.getAccountNumber());
            ps.setString(3, account.getAccountType());
            ps.setBigDecimal(4, account.getBalance());
            ps.setString(5, account.getStatus());

            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId != null) {
            account.setAccountId(generatedId.longValue());
        }

        return account;
    }

    public List<Account> findByCustomerId(Long customerId) {

        String sql = """
                SELECT * FROM accounts
                WHERE customer_id = ?
                ORDER BY account_id
                """;

        return jdbcTemplate.query(sql, accountRowMapper, customerId);
    }

    public List<Account> findByAccountNumber(String accountNumber) {

        String sql = """
                SELECT * FROM accounts
                WHERE account_number = ?
                """;

        return jdbcTemplate.query(sql, accountRowMapper, accountNumber);
    }

    public List<Account> findAll() {

        String sql = "SELECT * FROM accounts ORDER BY account_id";

        return jdbcTemplate.query(sql, accountRowMapper);
    }
}
