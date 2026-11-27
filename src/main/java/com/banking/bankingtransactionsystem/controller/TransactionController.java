
package com.banking.bankingtransactionsystem.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.banking.bankingtransactionsystem.model.Transaction;
import com.banking.bankingtransactionsystem.service.TransactionService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public static class DepositRequest {

        @NotNull(message = "Account ID is required")
        @Positive(message = "Account ID must be greater than zero")
        private Long accountId;

        @NotNull(message = "Transaction amount is required")
        @DecimalMin(value = "0.01",
                message = "Transaction amount must be greater than zero")
        private BigDecimal amount;

        private String description;

        @NotBlank(message = "Idempotency key is required")
        private String idempotencyKey;

        public Long getAccountId() {
            return accountId;
        }

        public void setAccountId(Long accountId) {
            this.accountId = accountId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIdempotencyKey() {
            return idempotencyKey;
        }

        public void setIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
        }
    }

    public static class WithdrawRequest {

        @NotNull(message = "Account ID is required")
        @Positive(message = "Account ID must be greater than zero")
        private Long accountId;

        @NotNull(message = "Transaction amount is required")
        @DecimalMin(value = "0.01",
                message = "Transaction amount must be greater than zero")
        private BigDecimal amount;

        private String description;

        @NotBlank(message = "Idempotency key is required")
        private String idempotencyKey;

        public Long getAccountId() {
            return accountId;
        }

        public void setAccountId(Long accountId) {
            this.accountId = accountId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIdempotencyKey() {
            return idempotencyKey;
        }

        public void setIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
        }
    }

    public static class TransferRequest {

        @NotNull(message = "Source account ID is required")
        @Positive(message = "Source account ID must be greater than zero")
        private Long fromAccountId;

        @NotNull(message = "Destination account ID is required")
        @Positive(message = "Destination account ID must be greater than zero")
        private Long toAccountId;

        @NotNull(message = "Transaction amount is required")
        @DecimalMin(value = "0.01",
                message = "Transaction amount must be greater than zero")
        private BigDecimal amount;

        private String description;

        @NotBlank(message = "Idempotency key is required")
        private String idempotencyKey;

        public Long getFromAccountId() {
            return fromAccountId;
        }

        public void setFromAccountId(Long fromAccountId) {
            this.fromAccountId = fromAccountId;
        }

        public Long getToAccountId() {
            return toAccountId;
        }

        public void setToAccountId(Long toAccountId) {
            this.toAccountId = toAccountId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIdempotencyKey() {
            return idempotencyKey;
        }

        public void setIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(
            @Valid @RequestBody DepositRequest request) {

        Transaction transaction = transactionService.deposit(
                request.getAccountId(),
                request.getAmount(),
                request.getDescription(),
                request.getIdempotencyKey()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @Valid @RequestBody WithdrawRequest request) {

        Transaction transaction = transactionService.withdraw(
                request.getAccountId(),
                request.getAmount(),
                request.getDescription(),
                request.getIdempotencyKey()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            @Valid @RequestBody TransferRequest request) {

        Transaction transaction = transactionService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription(),
                request.getIdempotencyKey()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(
                transactionService.getAllTransactions()
        );
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountId(
            @PathVariable Long accountId) {

        return ResponseEntity.ok(
                transactionService.getTransactionsByAccountId(accountId)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidRequest(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @GetMapping("/search")
    public List<Transaction> searchTransactions(
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate) {

        return transactionService.searchTransactions(
                transactionType,
                status,
                startDate,
                endDate
        );
    }
}
