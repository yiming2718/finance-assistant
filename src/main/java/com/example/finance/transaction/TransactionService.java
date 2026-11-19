package com.example.finance.transaction;

import com.example.finance.account.*;
import com.example.finance.category.*;
import com.example.finance.common.*;
import com.example.finance.transaction.dto.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository txRepo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;

    public TransactionService(TransactionRepository txRepo, AccountRepository accountRepo,
                              CategoryRepository categoryRepo) {
        this.txRepo = txRepo;
        this.accountRepo = accountRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional
    public TransactionResponse create(Long userId, CreateTransactionRequest req) {
        if (req.txnType() == TxnType.TRANSFER) {
            throw new BusinessRuleException("Use POST /api/v1/transfers for transfers");
        }
        Account account = ownedAccount(userId, req.accountId());

        if (req.categoryId() != null) {
            Category category = categoryRepo.findByIdAndUserId(req.categoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", req.categoryId()));
            if (!category.getCategoryType().name().equals(req.txnType().name())) {
                throw new BusinessRuleException(
                        "Category type " + category.getCategoryType() + " does not match " + req.txnType());
            }
        }

        BigDecimal signed = req.txnType() == TxnType.EXPENSE ? req.amount().negate() : req.amount();
        Transaction saved = txRepo.save(new Transaction(account.getId(), req.categoryId(), signed,
                req.txnType(), req.txnDate(), req.description(), null));
        return TransactionResponse.from(saved);
    }

    @Transactional
    public List<TransactionResponse> transfer(Long userId, CreateTransferRequest req) {
        if (req.fromAccountId().equals(req.toAccountId())) {
            throw new BusinessRuleException("Source and destination accounts must differ");
        }
        Account from = ownedAccount(userId, req.fromAccountId());
        Account to = ownedAccount(userId, req.toAccountId());

        String groupId = UUID.randomUUID().toString();
        Transaction out = txRepo.save(new Transaction(from.getId(), null, req.amount().negate(),
                TxnType.TRANSFER, req.txnDate(), req.description(), groupId));
        Transaction in = txRepo.save(new Transaction(to.getId(), null, req.amount(),
                TxnType.TRANSFER, req.txnDate(), req.description(), groupId));
        return List.of(TransactionResponse.from(out), TransactionResponse.from(in));
    }

    public PageResponse<TransactionResponse> search(Long userId, Long accountId, Long categoryId,
                                                    LocalDate from, LocalDate to, int page, int size) {
        List<Long> ownedIds = accountRepo.findByUserIdOrderByIdAsc(userId).stream()
                .map(Account::getId).toList();
        if (accountId != null && !ownedIds.contains(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        List<Long> scope = accountId != null ? List.of(accountId) : ownedIds;
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        if (scope.isEmpty()) {
            return new PageResponse<>(List.of(), safePage, safeSize, 0, 0);
        }

        Specification<Transaction> spec = (root, q, cb) -> root.get("accountId").in(scope);
        if (categoryId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("categoryId"), categoryId));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get("txnDate"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get("txnDate"), to));
        }

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Order.desc("txnDate"), Sort.Order.desc("id")));
        Page<Transaction> result = txRepo.findAll(spec, pageable);

        return new PageResponse<>(result.getContent().stream().map(TransactionResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Transaction t = txRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        accountRepo.findByIdAndUserId(t.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        if (t.getTransferGroupId() != null) {
            txRepo.deleteAll(txRepo.findByTransferGroupId(t.getTransferGroupId()));  // 轉帳兩列一起刪
        } else {
            txRepo.delete(t);
        }
    }

    public BalanceResponse balance(Long userId, Long accountId) {
        Account account = ownedAccount(userId, accountId);
        BigDecimal sum = txRepo.sumByAccountId(accountId);
        BigDecimal opening = account.getOpeningBalance();
        return new BalanceResponse(accountId, opening, opening.add(sum == null ? BigDecimal.ZERO : sum));
    }

    private Account ownedAccount(Long userId, Long accountId) {
        return accountRepo.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}