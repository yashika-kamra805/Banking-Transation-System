package com.banking.bankingtransactionsystem.service;

import com.banking.bankingtransactionsystem.model.Account;
import com.banking.bankingtransactionsystem.repository.AccountRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final JdbcTemplate jdbcTemplate;
    private final SecureRandom random = new SecureRandom();

    public AccountService(
            AccountRepository accountRepository,
            JdbcTemplate jdbcTemplate) {
        this.accountRepository = accountRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Account createAccount(
            Long customerId,
            String accountType,
            BigDecimal openingBalance) {

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        Integer customerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers WHERE customer_id = ?",
                Integer.class,
                customerId
        );

        if (customerCount == null || customerCount == 0) {
            throw new IllegalArgumentException("Customer not found");
        }

        if (accountType == null ||
                !(accountType.equalsIgnoreCase("SAVINGS")
                        || accountType.equalsIgnoreCase("CURRENT"))) {
            throw new IllegalArgumentException(
                    "Account type must be SAVINGS or CURRENT"
            );
        }

        if (openingBalance == null ||
                openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Opening balance cannot be negative or null"
            );
        }

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (!accountRepository
                .findByAccountNumber(accountNumber).isEmpty());

        Account account = new Account();

        account.setCustomerId(customerId);
        account.setAccountNumber(accountNumber);
        account.setAccountType(accountType.toUpperCase());
        account.setBalance(openingBalance);
        account.setStatus("ACTIVE");

        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            number.append(random.nextInt(10));
        }

        return number.toString();
    }

    public java.util.List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public java.util.List<Account> getAccountsByCustomerId(
            Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }
}
