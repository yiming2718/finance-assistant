package com.example.finance.account;

import com.example.finance.common.ResourceNotFoundException;

public class AccountNotFoundException extends ResourceNotFoundException {
    public AccountNotFoundException(Long id) { super("Account", id); }
}