package com.skuli.auth.api;

/**
 * Records the outcome of a Keycloak-then-DB provisioning compensation (plan §5.2). Exposed so the
 * provisioning services in other modules (teacher/student/parent) can persist an audit row from
 * their compensation path. Each method writes in its own transaction, so the record survives even
 * though the surrounding DB write that triggered the compensation was rolled back.
 */
public interface ProvisioningAuditRecorder {

    /** The orphaned Keycloak user was successfully deleted after the DB write failed. */
    void compensationSucceeded(String username, String keycloakUserId);

    /**
     * Compensation itself failed: the Keycloak user could not be deleted and is now orphaned,
     * requiring manual cleanup. {@code error} captures why.
     */
    void compensationFailed(String username, String keycloakUserId, String error);
}
