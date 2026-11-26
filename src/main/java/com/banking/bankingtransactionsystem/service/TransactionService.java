package com.banking.bankingtransactionsystem.service;

import com.banking.bankingtransactionsystem.service.FailedTransactionService;
import com.banking.bankingtransactionsystem.model.Transaction;
import com.banking.bankingtransactionsystem.repository.TransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.banking.bankingtransactionsystem.repository.TransactionAuditRepository;
import com.banking.bankingtransactionsystem.repository.FailedTransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final FailedTransactionRepository failedTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionAuditRepository auditRepository;
    private final FailedTransactionService failedTransactionService;

    public TransactionService(
            TransactionRepository transactionRepository,
            JdbcTemplate jdbcTemplate,
            TransactionAuditRepository auditRepository,
            FailedTransactionRepository failedTransactionRepository,
            FailedTransactionService failedTransactionService) {

        this.transactionRepository = transactionRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.auditRepository = auditRepository;
        this.failedTransactionRepository = failedTransactionRepository;
        this.failedTransactionService = failedTransactionService;
    }

    private Map<String, Object> lockAccount(Long accountId) {

        List<Map<String, Object>> accounts = jdbcTemplate.queryForList(
                "SELECT account_id, balance, status FROM accounts " +
                        "WHERE account_id = ? FOR UPDATE",
                accountId
        );

        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }

        Map<String, Object> account = accounts.get(0);

        if (!"ACTIVE".equals(account.get("status"))) {
            throw new IllegalArgumentException("Account is not active");
        }

        return account;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
    }

    @Transactional
    public Transaction deposit(
            Long accountId,
            BigDecimal amount,
            String description) {

        try {
            validateAmount(amount);

            Map<String, Object> account = lockAccount(accountId);

            jdbcTemplate.update(
                    "UPDATE accounts SET balance = balance + ? WHERE account_id = ?",
                    amount, accountId
            );

            Transaction transaction = new Transaction();
            transaction.setFromAccountId(null);
            transaction.setToAccountId(accountId);
            transaction.setTransactionType("DEPOSIT");
            transaction.setAmount(amount);
            transaction.setStatus("SUCCESS");
            transaction.setDescription(description);

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditRepository.saveAudit(
                    savedTransaction.getTransactionId(),
                    "DEPOSIT",
                    "Deposited " + amount + " into account " + accountId
            );

            return savedTransaction;

        } catch (RuntimeException ex) {

            failedTransactionService.recordFailure(
                    null,
                    ex.getMessage()
            );

            throw ex;
        }
    }

    @Transactional
    public Transaction withdraw(
            Long accountId,
            BigDecimal amount,
            String description) {

        try {
            validateAmount(amount);

            Map<String, Object> account = lockAccount(accountId);

            BigDecimal balance = (BigDecimal) account.get("balance");

            if (balance.compareTo(amount) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient account balance");
            }

            jdbcTemplate.update(
                    "UPDATE accounts SET balance = balance - ? WHERE account_id = ?",
                    amount, accountId
            );

            Transaction transaction = new Transaction();
            transaction.setFromAccountId(accountId);
            transaction.setToAccountId(null);
            transaction.setTransactionType("WITHDRAWAL");
            transaction.setAmount(amount);
            transaction.setStatus("SUCCESS");
            transaction.setDescription(description);

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditRepository.saveAudit(
                    savedTransaction.getTransactionId(),
                    "WITHDRAWAL",
                    "Withdrawn " + amount + " from account " + accountId
            );

            return savedTransaction;

        } catch (RuntimeException ex) {

            failedTransactionService.recordFailure(
                    null,
                    ex.getMessage()
            );

            throw ex;
        }
    }

    @Transactional
    public Transaction transfer(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            String description) {

        try {
            validateAmount(amount);

            if (fromAccountId == null || toAccountId == null ||
                    fromAccountId.equals(toAccountId)) {
                throw new IllegalArgumentException(
                        "Please provide two different account IDs");
            }

            Long firstId = Math.min(fromAccountId, toAccountId);
            Long secondId = Math.max(fromAccountId, toAccountId);

            Map<String, Object> firstAccount = lockAccount(firstId);
            Map<String, Object> secondAccount = lockAccount(secondId);

            Map<String, Object> sourceAccount =
                    fromAccountId.equals(firstId) ? firstAccount : secondAccount;

            BigDecimal balance = (BigDecimal) sourceAccount.get("balance");

            if (balance.compareTo(amount) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient account balance");
            }

            jdbcTemplate.update(
                    "UPDATE accounts SET balance = balance - ? WHERE account_id = ?",
                    amount, fromAccountId
            );

            jdbcTemplate.update(
                    "UPDATE accounts SET balance = balance + ? WHERE account_id = ?",
                    amount, toAccountId
            );

            Transaction transaction = new Transaction();
            transaction.setFromAccountId(fromAccountId);
            transaction.setToAccountId(toAccountId);
            transaction.setTransactionType("TRANSFER");
            transaction.setAmount(amount);
            transaction.setStatus("SUCCESS");
            transaction.setDescription(description);

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditRepository.saveAudit(
                    savedTransaction.getTransactionId(),
                    "TRANSFER",
                    "Transferred " + amount +
                            " from account " + fromAccountId +
                            " to account " + toAccountId
            );

            return savedTransaction;

        } catch (RuntimeException ex) {

            failedTransactionService.recordFailure(
                    null,
                    ex.getMessage()
            );

            throw ex;
        }
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
}
