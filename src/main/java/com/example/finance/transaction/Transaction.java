package com.example.finance.transaction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;   // 帶正負號：收入 +、支出 -

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false, length = 10)
    private TxnType txnType;

    @Column(name = "txn_date", nullable = false)
    private LocalDate txnDate;

    @Column(length = 255)
    private String description;

    @Column(name = "transfer_group_id", length = 36)
    private String transferGroupId;

    protected Transaction() {}

    public Transaction(Long accountId, Long categoryId, BigDecimal amount, TxnType txnType,
                       LocalDate txnDate, String description, String transferGroupId) {
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.txnType = txnType;
        this.txnDate = txnDate;
        this.description = description;
        this.transferGroupId = transferGroupId;
    }

    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public Long getCategoryId() { return categoryId; }
    public BigDecimal getAmount() { return amount; }
    public TxnType getTxnType() { return txnType; }
    public LocalDate getTxnDate() { return txnDate; }
    public String getDescription() { return description; }
    public String getTransferGroupId() { return transferGroupId; }
}