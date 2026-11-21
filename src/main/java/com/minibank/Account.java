package com.minibank;

import java.math.BigDecimal;

public class Account {

    private final Long id;
    private final Long clientId;
    private final String accountNumber;
    private BigDecimal balance;

    public Account(Long id, Long clientId, String accountNumber, BigDecimal balance) {
        this.id = id;
        this.clientId = clientId;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
