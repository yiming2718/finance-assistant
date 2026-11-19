package com.example.finance.account.dto;

import com.example.finance.account.AccountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull AccountType accountType,
        @NotNull @Digits(integer = 17, fraction = 2) BigDecimal openingBalance
) {}