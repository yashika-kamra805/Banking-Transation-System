package com.banking.bankingtransactionsystem.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import com.banking.bankingtransactionsystem.model.Account;
import com.banking.bankingtransactionsystem.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    public static class CreateAccountRequest {

        @NotNull(message = "Customer ID is required")
        @Positive(message = "Customer ID must be greater than zero")
        private Long customerId;
        @NotBlank(message = "Account type is required")
        @Pattern(
                regexp = "(?i)^(SAVINGS|CURRENT)$",
                message = "Account type must be SAVINGS or CURRENT"
        )
        private String accountType;
        @NotNull(message = "Opening balance is required")
        @DecimalMin(
                value = "0.00",
                message = "Opening balance cannot be negative"
        )
        private BigDecimal openingBalance;

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public String getAccountType() {
            return accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public BigDecimal getOpeningBalance() {
            return openingBalance;
        }

        public void setOpeningBalance(BigDecimal openingBalance) {
            this.openingBalance = openingBalance;
        }
    }
    @PostMapping
    public ResponseEntity<Account> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        Account account = accountService.createAccount(
                request.getCustomerId(),
                request.getAccountType(),
                request.getOpeningBalance()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Account>> getAccountsByCustomerId(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                accountService.getAccountsByCustomerId(customerId)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidRequest(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
