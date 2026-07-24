package com.skuli.common.error;

/**
 * Thrown when a domain invariant is violated (e.g. enrolling into a full class). Mapped to
 * HTTP 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
