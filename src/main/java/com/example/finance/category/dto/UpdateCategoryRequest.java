package com.example.finance.category.dto;

import jakarta.validation.constraints.*;

public record UpdateCategoryRequest(@NotBlank @Size(max = 100) String name) {}