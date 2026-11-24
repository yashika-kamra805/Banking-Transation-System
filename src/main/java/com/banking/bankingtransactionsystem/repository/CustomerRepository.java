package com.banking.bankingtransactionsystem.repository;

import com.banking.bankingtransactionsystem.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> {
        Customer customer = new Customer();

        customer.setCustomerId(rs.getLong("customer_id"));
        customer.setFullName(rs.getString("full_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            customer.setCreatedAt(timestamp.toLocalDateTime());
        }

        return customer;
    };

    public int save(Customer customer) {
        String sql = """
                INSERT INTO customers
                (full_name, email, phone, address)
                VALUES (?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
        );
    }

    public Optional<Customer> findById(Long customerId) {
        String sql = """
                SELECT * FROM customers
                WHERE customer_id = ?
                """;

        List<Customer> customers = jdbcTemplate.query(
                sql, customerRowMapper, customerId
        );

        return customers.stream().findFirst();
    }

    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers ORDER BY customer_id";

        return jdbcTemplate.query(sql, customerRowMapper);
    }

    public boolean existsByEmail(String email) {
        String sql = """
                SELECT COUNT(*) FROM customers
                WHERE email = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql, Integer.class, email
        );

        return count != null && count > 0;
    }
}

