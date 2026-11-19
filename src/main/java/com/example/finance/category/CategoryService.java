package com.example.finance.category;

import com.example.finance.category.dto.*;
import com.example.finance.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) { this.repository = repository; }

    @Transactional
    public CategoryResponse create(Long userId, CreateCategoryRequest req) {
        return CategoryResponse.from(
                repository.save(new Category(userId, req.name(), req.categoryType())));
    }

    public List<CategoryResponse> list(Long userId) {
        return repository.findByUserIdOrderByIdAsc(userId).stream()
                .map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse update(Long userId, Long id, UpdateCategoryRequest req) {
        Category category = findOwned(userId, id);
        category.rename(req.name());      // Dirty Checking，不需要 save()
        repository.flush();               // 讓唯一約束衝突在這裡就暴露，而不是等到 commit
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        repository.delete(findOwned(userId, id));
        repository.flush();               // 同上：若分類已被交易引用，這裡就會丟出例外
    }

    private Category findOwned(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

}