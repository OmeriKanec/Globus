package com.minibank;

public class InsufficientFundsException extends IllegalStateException {

    public InsufficientFundsException(Long accountId) {
        super("Insufficient funds on account " + accountId);
    }
}
