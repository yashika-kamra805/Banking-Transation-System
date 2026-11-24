package com.banking.bankingtransactionsystem.controller;

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

        private Long customerId;
        private String accountType;
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
            @RequestBody CreateAccountRequest request) {

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
