package com.example.finance.transaction;

import com.example.finance.TestcontainersConfiguration;
import com.example.finance.account.*;
import com.example.finance.account.dto.CreateAccountRequest;
import com.example.finance.common.PageResponse;
import com.example.finance.transaction.dto.*;
import com.example.finance.category.*;
import com.example.finance.category.dto.CreateCategoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransactionServiceIntegrationTest {

    @Autowired TransactionService txService;
    @Autowired AccountService accountService;
    @Autowired CategoryService categoryService;
    @Autowired JdbcTemplate jdbc;

    Long userId;
    Long otherUserId;

    @BeforeEach
    void cleanDatabase() {
        for (String table : List.of("transactions", "budgets", "recurring_rules",
                "categories", "accounts", "users")) {
            jdbc.update("DELETE FROM " + table);     // 依外鍵順序，子表先刪
        }
        userId = createUser("a@test.com");
        otherUserId = createUser("b@test.com");
    }

    @Test
    void balance_isOpeningPlusSignedTransactions() {
        Long acc = newAccount(userId, "Bank", "1000.00");
        txService.create(userId, income(acc, "500.00", LocalDate.of(2026, 11, 1)));
        txService.create(userId, expense(acc, "200.00", LocalDate.of(2026, 11, 2)));   // 同帳戶第二筆，驗證 V2 修正

        assertThat(txService.balance(userId, acc).currentBalance()).isEqualByComparingTo("1300.00");
    }

    @Test
    void transfer_movesMoneyAndCreatesTwoLinkedRows() {
        Long a = newAccount(userId, "A", "1000.00");
        Long b = newAccount(userId, "B", "0.00");

        List<TransactionResponse> rows = txService.transfer(userId,
                new CreateTransferRequest(a, b, new BigDecimal("300.00"), LocalDate.of(2026, 11, 3), "move"));

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).transferGroupId()).isEqualTo(rows.get(1).transferGroupId());
        assertThat(txService.balance(userId, a).currentBalance()).isEqualByComparingTo("700.00");
        assertThat(txService.balance(userId, b).currentBalance()).isEqualByComparingTo("300.00");
    }

    @Test
    void transfer_toSomeoneElsesAccount_isRejectedAndNothingPersisted() {
        Long mine = newAccount(userId, "Mine", "1000.00");
        Long theirs = newAccount(otherUserId, "Theirs", "0.00");

        assertThatThrownBy(() -> txService.transfer(userId,
                new CreateTransferRequest(mine, theirs, new BigDecimal("100.00"), LocalDate.of(2026, 11, 3), null)))
                .isInstanceOf(AccountNotFoundException.class);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transactions", Integer.class)).isZero();
    }

    @Test
    void deleteTransfer_removesBothRows() {
        Long a = newAccount(userId, "A", "1000.00");
        Long b = newAccount(userId, "B", "0.00");
        List<TransactionResponse> rows = txService.transfer(userId,
                new CreateTransferRequest(a, b, new BigDecimal("300.00"), LocalDate.of(2026, 11, 3), null));

        txService.delete(userId, rows.get(0).id());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transactions", Integer.class)).isZero();
    }

    @Test
    void search_paginatesAndSortsNewestFirst() {
        Long acc = newAccount(userId, "Bank", "0.00");
        for (int day = 1; day <= 5; day++) {
            txService.create(userId, income(acc, "10.00", LocalDate.of(2026, 11, day)));
        }

        PageResponse<TransactionResponse> page =
                txService.search(userId, acc, null, null, null, 0, 2);

        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.content()).hasSize(2);
        assertThat(page.content().get(0).txnDate()).isEqualTo(LocalDate.of(2026, 11, 5));
    }

    @Test
    void deleteCategory_stillReferencedByTransaction_isRejected() {
        Long acc = newAccount(userId, "Bank", "0.00");
        Long catId = categoryService.create(userId,
                new CreateCategoryRequest("餐飲", CategoryType.EXPENSE)).id();
        txService.create(userId, new CreateTransactionRequest(
                acc, catId, TxnType.EXPENSE, new BigDecimal("50.00"), LocalDate.of(2026, 11, 1), null));

        assertThatThrownBy(() -> categoryService.delete(userId, catId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------- helpers ----------
    private Long createUser(String email) {
        jdbc.update("INSERT INTO users (email, password_hash) VALUES (?, 'x')", email);
        return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }

    private Long newAccount(Long uid, String name, String opening) {
        return accountService.create(uid,
                new CreateAccountRequest(name, AccountType.BANK, new BigDecimal(opening))).id();
    }

    private CreateTransactionRequest income(Long acc, String amount, LocalDate date) {
        return new CreateTransactionRequest(acc, null, TxnType.INCOME, new BigDecimal(amount), date, null);
    }

    private CreateTransactionRequest expense(Long acc, String amount, LocalDate date) {
        return new CreateTransactionRequest(acc, null, TxnType.EXPENSE, new BigDecimal(amount), date, null);
    }
}