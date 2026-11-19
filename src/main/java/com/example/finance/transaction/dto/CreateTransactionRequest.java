package com.example.finance.transaction.dto;

import com.example.finance.transaction.TxnType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotNull Long accountId,
        Long categoryId,
        @NotNull TxnType txnType,                       // 只接受 INCOME / EXPENSE
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotNull LocalDate txnDate,
        @Size(max = 255) String description) {}