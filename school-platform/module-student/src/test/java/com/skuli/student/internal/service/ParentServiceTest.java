package com.skuli.student.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.skuli.auth.api.KeycloakService;
import com.skuli.auth.api.ProvisioningAuditRecorder;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.security.TenantContext;
import com.skuli.student.api.dto.ParentDto;
import com.skuli.student.internal.domain.Parent;
import com.skuli.student.internal.mapper.ParentMapperImpl;
import com.skuli.student.internal.repository.ParentRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the parent slice: Keycloak-then-DB provisioning, compensation, duplicate
 * rejection, and delete ordering.
 */
@ExtendWith(MockitoExtension.class)
class ParentServiceTest {

    private static final String TENANT = "default";

    @Mock
    private ParentRepository repository;

    @Mock
    private KeycloakService keycloak;

    @Mock
    private ProvisioningAuditRecorder auditRecorder;

    private ParentService service;

    @BeforeEach
    void setUp() {
        service = new ParentService(repository, new ParentMapperImpl(), keycloak, auditRecorder);
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static ParentDto dto() {
        return new ParentDto("ignored", "pmum", "Pat", "Mum", "p@example.com", "123456",
                "1 Main St", "password1", null);
    }

    @Test
    void create_provisionsKeycloakThenPersists_withParentRole() {
        when(repository.existsByUsername("pmum")).thenReturn(false);
        when(keycloak.createUser(any())).thenReturn("kc-1");
        when(repository.save(any(Parent.class))).thenAnswer(inv -> inv.getArgument(0));

        ParentDto result = service.create(dto());

        verify(keycloak).assignRealmRole("kc-1", "parent");
        ArgumentCaptor<Parent> saved = ArgumentCaptor.forClass(Parent.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo("pmum"); // id == username
        assertThat(result.password()).isNull();
    }

    @Test
    void create_deletesKeycloakUser_whenDbWriteFails() {
        when(repository.existsByUsername("pmum")).thenReturn(false);
        when(keycloak.createUser(any())).thenReturn("kc-1");
        when(repository.save(any(Parent.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.create(dto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");

        verify(keycloak).deleteUser("kc-1");
        verify(auditRecorder).compensationSucceeded("pmum", "kc-1");
    }

    @Test
    void create_rejectsDuplicateUsername_withoutTouchingKeycloak() {
        when(repository.existsByUsername("pmum")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto()))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(keycloak);
    }

    @Test
    void delete_removesDbRowThenKeycloakUser() {
        Parent parent = new Parent();
        parent.setId("pmum");
        parent.setUsername("pmum");
        parent.setTenantId(TENANT);
        when(repository.findById("pmum")).thenReturn(Optional.of(parent));
        when(keycloak.findUserId("pmum")).thenReturn(Optional.of("kc-1"));

        service.delete("pmum");

        InOrder inOrder = inOrder(repository, keycloak);
        inOrder.verify(repository).delete(parent);
        inOrder.verify(keycloak).deleteUser("kc-1");
    }
}
