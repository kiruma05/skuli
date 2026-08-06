package com.skuli.auth.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.skuli.auth.internal.domain.ProvisioningAudit;
import com.skuli.auth.internal.domain.ProvisioningAudit.Action;
import com.skuli.auth.internal.domain.ProvisioningAudit.Outcome;
import com.skuli.auth.internal.repository.ProvisioningAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvisioningAuditServiceTest {

    @Mock
    private ProvisioningAuditRepository repository;

    private ProvisioningAuditService service;

    @BeforeEach
    void setUp() {
        service = new ProvisioningAuditService(repository);
    }

    @Test
    void compensationSucceeded_recordsASuccessRow() {
        service.compensationSucceeded("jdoe", "kc-1");

        ProvisioningAudit saved = captureSaved();
        assertThat(saved.getUsername()).isEqualTo("jdoe");
        assertThat(saved.getAction()).isEqualTo(Action.COMPENSATE_DELETE);
        assertThat(saved.getOutcome()).isEqualTo(Outcome.SUCCESS);
        assertThat(saved.getDetail()).contains("kc-1");
    }

    @Test
    void compensationFailed_recordsAFailureRowWithTheError() {
        service.compensationFailed("jdoe", "kc-1", "keycloak unreachable");

        ProvisioningAudit saved = captureSaved();
        assertThat(saved.getOutcome()).isEqualTo(Outcome.FAILURE);
        assertThat(saved.getDetail()).contains("kc-1").contains("keycloak unreachable");
    }

    private ProvisioningAudit captureSaved() {
        ArgumentCaptor<ProvisioningAudit> captor = ArgumentCaptor.forClass(ProvisioningAudit.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }
}
