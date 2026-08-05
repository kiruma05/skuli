package com.skuli.auth.api;

/**
 * Raised when a Keycloak Admin API call fails (non-success status or transport error). Distinct
 * from domain errors so callers can tell "the identity provider rejected this" apart from a
 * business-rule violation.
 */
public class KeycloakProvisioningException extends RuntimeException {

    public KeycloakProvisioningException(String message) {
        super(message);
    }

    public KeycloakProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
