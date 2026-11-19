package com.example.finance.account.dto;

import com.example.finance.account.Account;
import com.example.finance.account.AccountType;
import java.math.BigDecimal;

public record AccountResponse(
        Long id, String name, AccountType accountType, BigDecimal openingBalance
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getAccountType(), a.getOpeningBalance());
    }
}