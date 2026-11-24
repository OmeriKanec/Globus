package com.minibank;

public class AccountNotFoundException extends IllegalArgumentException {

    public AccountNotFoundException(Long accountId) {
        super("Account not found: " + accountId);
    }
}
