package com.banking.bankingtransactionsystem.controller;

import com.banking.bankingtransactionsystem.model.TransactionAudit;
import com.banking.bankingtransactionsystem.repository.TransactionAuditRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class TransactionAuditController {

    private final TransactionAuditRepository auditRepository;

    public TransactionAuditController(
            TransactionAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping
    public List<TransactionAudit> getAllAuditRecords() {
        return auditRepository.findAll();
    }

    @GetMapping("/transaction/{transactionId}")
    public List<TransactionAudit> getAuditByTransactionId(
            @PathVariable Long transactionId) {

        return auditRepository.findByTransactionId(transactionId);
    }
}
