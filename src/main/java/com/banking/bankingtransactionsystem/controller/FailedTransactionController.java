package com.banking.bankingtransactionsystem.controller;

import com.banking.bankingtransactionsystem.model.FailedTransaction;
import com.banking.bankingtransactionsystem.repository.FailedTransactionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/failed-transactions")
public class FailedTransactionController {

    private final FailedTransactionRepository failedTransactionRepository;

    public FailedTransactionController(
            FailedTransactionRepository failedTransactionRepository) {
        this.failedTransactionRepository = failedTransactionRepository;
    }

    @GetMapping
    public List<FailedTransaction> getAllFailedTransactions() {
        return failedTransactionRepository.findAll();
    }

    @GetMapping("/transaction/{transactionId}")
    public List<FailedTransaction> getFailedTransactionsByTransactionId(
            @PathVariable Long transactionId) {

        return failedTransactionRepository.findByTransactionId(
                transactionId
        );
    }
}
