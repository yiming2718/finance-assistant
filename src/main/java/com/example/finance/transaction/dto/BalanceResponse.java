package com.example.finance.transaction.dto;

import java.math.BigDecimal;

public record BalanceResponse(Long accountId, BigDecimal openingBalance, BigDecimal currentBalance) {}