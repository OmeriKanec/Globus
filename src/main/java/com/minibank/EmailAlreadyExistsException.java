package com.minibank;

public class EmailAlreadyExistsException extends IllegalStateException {

    public EmailAlreadyExistsException(String email, Throwable cause) {
        super("Client email already exists: " + email, cause);
    }
}
