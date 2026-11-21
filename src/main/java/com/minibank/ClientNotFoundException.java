package com.minibank;

public class ClientNotFoundException extends IllegalArgumentException {

    public ClientNotFoundException(Long clientId) {
        super("Client not found: " + clientId);
    }
}
