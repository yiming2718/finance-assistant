package com.example.finance.account;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Version
    private Long version;

    protected Account() {}   // JPA 需要，但不開放給外部使用

    public Account(Long userId, String name, AccountType accountType, BigDecimal openingBalance) {
        this.userId = userId;
        this.name = name;
        this.accountType = accountType;
        this.openingBalance = openingBalance;
    }

    public void update(String name, AccountType accountType) {
        this.name = name;
        this.accountType = accountType;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public AccountType getAccountType() { return accountType; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public Long getVersion() { return version; }
}