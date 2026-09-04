package com.example.usermanagement.exception;

public class DuplicateResourceException extends RuntimeException {
    private final Object[] args;

    public DuplicateResourceException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
