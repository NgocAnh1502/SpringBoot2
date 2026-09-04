package com.example.usermanagement.exception;

public class KeycloakCompensationException extends RuntimeException {
    private final Object[] args;

    public KeycloakCompensationException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
