package com.example.finance.category.dto;

import com.example.finance.category.*;

public record CategoryResponse(Long id, String name, CategoryType categoryType) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getCategoryType());
    }
}