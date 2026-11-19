package com.example.finance.category;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderByIdAsc(Long userId);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
}