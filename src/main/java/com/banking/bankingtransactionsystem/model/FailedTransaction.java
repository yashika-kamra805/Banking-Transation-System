package com.banking.bankingtransactionsystem.model;

import com.banking.bankingtransactionsystem.model.FailedTransaction;
import java.time.LocalDateTime;

public class FailedTransaction {

    private Long failedId;
    private Long transactionId;
    private String errorMessage;
    private LocalDateTime attemptedAt;

    public FailedTransaction() {
    }

    public Long getFailedId() {
        return failedId;
    }

    public void setFailedId(Long failedId) {
        this.failedId = failedId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(LocalDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }
}
