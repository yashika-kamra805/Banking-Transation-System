package com.banking.bankingtransactionsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.List;
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

    private static final Logger logger =
            LoggerFactory.getLogger(TransactionService.class);
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
    private Transaction checkDuplicateTransaction(
            String idempotencyKey,
            String transactionType,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required"
            );
        }

        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {

            Transaction existing = existingTransaction.get();

            boolean sameRequest =
                    existing.getTransactionType().equals(transactionType)
                            && java.util.Objects.equals(
                            existing.getFromAccountId(), fromAccountId)
                            && java.util.Objects.equals(
                            existing.getToAccountId(), toAccountId)
                            && existing.getAmount().compareTo(amount) == 0;
            if (!sameRequest) {
                throw new IllegalArgumentException(
                        "Idempotency key has already been used for a different request"
                );
            }
            return existing;
        }
        return null;
    }


    @Transactional
    public Transaction deposit(
            Long accountId,
            BigDecimal amount,
            String description,
            String idempotencyKey) {

        long startTime = System.nanoTime();

        logger.info("Deposit started. accountId={}, thread={}",
                accountId,
                Thread.currentThread().getName());

        try {
            validateAmount(amount);

            transactionRepository.lockIdempotencyKey(idempotencyKey);

            Transaction existingTransaction = checkDuplicateTransaction(
                    idempotencyKey,
                    "DEPOSIT",
                    null,
                    accountId,
                    amount
            );

            if (existingTransaction != null) {
                logger.info(
                        "Duplicate deposit request detected. transactionId={}, durationMs={}",
                        existingTransaction.getTransactionId(),
                        (System.nanoTime() - startTime) / 1_000_000
                );

                return existingTransaction;
            }

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
            transaction.setIdempotencyKey(idempotencyKey);

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditRepository.saveAudit(
                    savedTransaction.getTransactionId(),
                    "DEPOSIT",
                    "Deposited " + amount + " into account " + accountId
            );

            logger.info(
                    "Deposit successful. transactionId={}, accountId={}, durationMs={}",
                    savedTransaction.getTransactionId(),
                    accountId,
                    (System.nanoTime() - startTime) / 1_000_000
            );

            return savedTransaction;

        } catch (RuntimeException ex) {

            logger.error(
                    "Deposit failed. accountId={}, errorType={}, durationMs={}",
                    accountId,
                    ex.getClass().getSimpleName(),
                    (System.nanoTime() - startTime) / 1_000_000
            );

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
            String description,
            String idempotencyKey) {

        long startTime = System.nanoTime();

        logger.info("Withdrawal started. accountId={}, thread={}",
                accountId,
                Thread.currentThread().getName());

        try {
            validateAmount(amount);

            transactionRepository.lockIdempotencyKey(idempotencyKey);

            Transaction existingTransaction = checkDuplicateTransaction(
                    idempotencyKey,
                    "WITHDRAWAL",
                    accountId,
                    null,
                    amount
            );

            if (existingTransaction != null) {
                logger.info(
                        "Duplicate withdrawal detected. transactionId={}, durationMs={}",
                        existingTransaction.getTransactionId(),
                        (System.nanoTime() - startTime) / 1_000_000
                );

                return existingTransaction;
            }

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
            transaction.setIdempotencyKey(idempotencyKey);

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditRepository.saveAudit(
                    savedTransaction.getTransactionId(),
                    "WITHDRAWAL",
                    "Withdrawn " + amount + " from account " + accountId
            );

            logger.info(
                    "Withdrawal successful. transactionId={}, accountId={}, durationMs={}",
                    savedTransaction.getTransactionId(),
                    accountId,
                    (System.nanoTime() - startTime) / 1_000_000
            );

            return savedTransaction;

        } catch (RuntimeException ex) {

            logger.error(
                    "Withdrawal failed. accountId={}, errorType={}, durationMs={}",
                    accountId,
                    ex.getClass().getSimpleName(),
                    (System.nanoTime() - startTime) / 1_000_000
            );

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
            String description,
            String idempotencyKey) {

        long startTime = System.nanoTime();

        logger.info(
                "Transfer started. fromAccountId={}, toAccountId={}, thread={}",
                fromAccountId,
                toAccountId,
                Thread.currentThread().getName()
        );

        try {
            validateAmount(amount);

            transactionRepository.lockIdempotencyKey(idempotencyKey);

            Transaction existingTransaction = checkDuplicateTransaction(
                    idempotencyKey,
                    "TRANSFER",
                    fromAccountId,
                    toAccountId,
                    amount
            );

            if (existingTransaction != null) {
                logger.info(
                        "Duplicate transfer detected. transactionId={}, durationMs={}",
                        existingTransaction.getTransactionId(),
                        (System.nanoTime() - startTime) / 1_000_000
                );

                return existingTransaction;
            }

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
            transaction.setIdempotencyKey(idempotencyKey);

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditRepository.saveAudit(
                    savedTransaction.getTransactionId(),
                    "TRANSFER",
                    "Transferred " + amount +
                            " from account " + fromAccountId +
                            " to account " + toAccountId
            );

            logger.info(
                    "Transfer successful. transactionId={}, fromAccountId={}, toAccountId={}, durationMs={}",
                    savedTransaction.getTransactionId(),
                    fromAccountId,
                    toAccountId,
                    (System.nanoTime() - startTime) / 1_000_000
            );

            return savedTransaction;

        } catch (RuntimeException ex) {

            logger.error(
                    "Transfer failed. fromAccountId={}, toAccountId={}, errorType={}, durationMs={}",
                    fromAccountId,
                    toAccountId,
                    ex.getClass().getSimpleName(),
                    (System.nanoTime() - startTime) / 1_000_000
            );

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

    public List<Transaction> searchTransactions(
            String transactionType,
            String status,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate) {

        if (startDate != null && endDate != null
                && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        if (transactionType != null
                && !transactionType.isBlank()) {
            transactionType = transactionType.toUpperCase();

            if (!List.of("DEPOSIT", "WITHDRAWAL", "TRANSFER")
                    .contains(transactionType)) {
                throw new IllegalArgumentException(
                        "Invalid transaction type"
                );
            }
        }

        if (status != null && !status.isBlank()) {
            status = status.toUpperCase();

            if (!List.of("SUCCESS", "FAILED").contains(status)) {
                throw new IllegalArgumentException(
                        "Invalid transaction status"
                );
            }
        }

        return transactionRepository.searchTransactions(
                transactionType,
                status,
                startDate,
                endDate
        );
    }

}
