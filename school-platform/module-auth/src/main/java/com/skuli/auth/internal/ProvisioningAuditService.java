package com.skuli.auth.internal;

import com.skuli.auth.api.ProvisioningAuditRecorder;
import com.skuli.auth.internal.domain.ProvisioningAudit;
import com.skuli.auth.internal.domain.ProvisioningAudit.Action;
import com.skuli.auth.internal.domain.ProvisioningAudit.Outcome;
import com.skuli.auth.internal.repository.ProvisioningAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists provisioning-compensation outcomes to {@link ProvisioningAudit}. Each write runs in a
 * {@code REQUIRES_NEW} transaction so the audit row commits independently of the failed
 * provisioning transaction that triggered it.
 */
@Service
public class ProvisioningAuditService implements ProvisioningAuditRecorder {

    private static final int MAX_DETAIL = 2000;

    private final ProvisioningAuditRepository repository;

    public ProvisioningAuditService(ProvisioningAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensationSucceeded(String username, String keycloakUserId) {
        save(username, Outcome.SUCCESS, "Deleted orphaned Keycloak user " + keycloakUserId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensationFailed(String username, String keycloakUserId, String error) {
        save(username, Outcome.FAILURE,
                "Keycloak user " + keycloakUserId + " left orphaned; delete failed: " + error);
    }

    private void save(String username, Outcome outcome, String detail) {
        ProvisioningAudit audit = new ProvisioningAudit();
        audit.setUsername(username);
        audit.setAction(Action.COMPENSATE_DELETE);
        audit.setOutcome(outcome);
        audit.setDetail(detail.length() > MAX_DETAIL ? detail.substring(0, MAX_DETAIL) : detail);
        repository.save(audit);
    }
}
