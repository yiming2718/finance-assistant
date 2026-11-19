package com.example.finance.transaction;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByTransferGroupId(String transferGroupId);

    @Query("select sum(t.amount) from Transaction t where t.accountId = :accountId")
    BigDecimal sumByAccountId(@Param("accountId") Long accountId);
}