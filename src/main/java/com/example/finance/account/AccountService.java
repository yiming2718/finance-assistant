package com.example.finance.account;

import com.example.finance.account.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AccountResponse create(Long userId, CreateAccountRequest req) {
        Account saved = repository.save(
                new Account(userId, req.name(), req.accountType(), req.openingBalance()));
        return AccountResponse.from(saved);
    }

    public List<AccountResponse> list(Long userId) {
        return repository.findByUserIdOrderByIdAsc(userId).stream()
                .map(AccountResponse::from).toList();
    }

    public AccountResponse get(Long userId, Long id) {
        return AccountResponse.from(findOwned(userId, id));
    }

    @Transactional
    public AccountResponse update(Long userId, Long id, UpdateAccountRequest req) {
        Account account = findOwned(userId, id);
        account.update(req.name(), req.accountType());
        return AccountResponse.from(account);   // 不用呼叫 save()，下面解釋
    }

    @Transactional
    public void delete(Long userId, Long id) {
        repository.delete(findOwned(userId, id));
    }

    private Account findOwned(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}