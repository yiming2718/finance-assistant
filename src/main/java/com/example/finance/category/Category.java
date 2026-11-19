package com.example.finance.category;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 10)
    private CategoryType categoryType;

    protected Category() {}

    public Category(Long userId, String name, CategoryType categoryType) {
        this.userId = userId;
        this.name = name;
        this.categoryType = categoryType;
    }
    public void rename(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public CategoryType getCategoryType() { return categoryType; }

}