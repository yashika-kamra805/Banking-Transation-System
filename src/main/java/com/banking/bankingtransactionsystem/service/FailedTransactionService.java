package com.banking.bankingtransactionsystem.service;

import com.banking.bankingtransactionsystem.repository.FailedTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FailedTransactionService {

    private final FailedTransactionRepository failedTransactionRepository;

    public FailedTransactionService(
            FailedTransactionRepository failedTransactionRepository) {

        this.failedTransactionRepository = failedTransactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long transactionId, String errorMessage) {

        failedTransactionRepository.saveFailedTransaction(
                transactionId,
                errorMessage
        );
    }
}
