package com.example.finance.transaction.dto;

import com.example.finance.transaction.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(Long id, Long accountId, Long categoryId, BigDecimal amount,
                                  TxnType txnType, LocalDate txnDate, String description,
                                  String transferGroupId) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAccountId(), t.getCategoryId(), t.getAmount(),
                t.getTxnType(), t.getTxnDate(), t.getDescription(), t.getTransferGroupId());
    }
}