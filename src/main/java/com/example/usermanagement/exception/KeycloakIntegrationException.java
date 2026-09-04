package com.example.usermanagement.exception;

public class KeycloakIntegrationException extends RuntimeException {
    private final Object[] args;

    public KeycloakIntegrationException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
