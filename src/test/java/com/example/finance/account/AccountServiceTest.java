package com.example.finance.account;

import com.example.finance.account.dto.AccountResponse;
import com.example.finance.account.dto.CreateAccountRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository repository;
    @InjectMocks AccountService service;

    @Test
    void create_savesAccountForGivenUser() {
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse res = service.create(1L,
                new CreateAccountRequest("薪轉戶", AccountType.BANK, new BigDecimal("1000.00")));

        assertThat(res.name()).isEqualTo("薪轉戶");
        assertThat(res.openingBalance()).isEqualByComparingTo("1000.00");
        verify(repository).save(argThat(a -> a.getUserId().equals(1L)));
    }

    @Test
    void get_accountOfAnotherUser_throwsNotFound() {
        when(repository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(1L, 99L))
                .isInstanceOf(AccountNotFoundException.class);
    }
}