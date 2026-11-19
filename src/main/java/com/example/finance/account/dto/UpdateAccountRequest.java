package com.example.finance.account.dto;

import com.example.finance.account.AccountType;
import jakarta.validation.constraints.*;

public record UpdateAccountRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull AccountType accountType
) {}