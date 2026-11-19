package com.example.finance.category.dto;

import com.example.finance.category.CategoryType;
import jakarta.validation.constraints.*;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull CategoryType categoryType) {}