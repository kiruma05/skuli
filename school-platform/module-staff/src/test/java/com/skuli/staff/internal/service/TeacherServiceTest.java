package com.skuli.staff.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.skuli.auth.api.KeycloakService;
import com.skuli.auth.api.ProvisioningAuditRecorder;
import com.skuli.common.domain.UserSex;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.staff.api.dto.TeacherDto;
import com.skuli.staff.internal.domain.Teacher;
import com.skuli.staff.internal.mapper.TeacherMapperImpl;
import com.skuli.staff.internal.repository.TeacherRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.skuli.common.security.TenantContext;

/**
 * Unit tests for the teacher slice, focused on the Keycloak-then-DB provisioning contract:
 * happy path, create-compensation on DB failure, duplicate rejection, and delete ordering.
 */
@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    private static final String TENANT = "default";

    @Mock
    private TeacherRepository repository;

    @Mock
    private KeycloakService keycloak;

    @Mock
    private ProvisioningAuditRecorder auditRecorder;

    private TeacherService service;

    @BeforeEach
    void setUp() {
        service = new TeacherService(repository, new TeacherMapperImpl(), keycloak, auditRecorder);
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static TeacherDto dto() {
        return new TeacherDto("ignored", "jdoe", "John", "Doe", "j@example.com", "123456",
                "1 Main St", null, "O+", UserSex.MALE, Instant.parse("1990-01-01T00:00:00Z"),
                Set.of(1, 2), "password1", null);
    }

    @Test
    void create_provisionsKeycloakThenPersists_withIdEqualToUsername() {
        when(repository.existsByUsername("jdoe")).thenReturn(false);
        when(keycloak.createUser(any())).thenReturn("kc-1");
        when(repository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));

        TeacherDto result = service.create(dto());

        verify(keycloak).assignRealmRole("kc-1", "teacher");
        ArgumentCaptor<Teacher> saved = ArgumentCaptor.forClass(Teacher.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo("jdoe"); // id == username
        assertThat(result.id()).isEqualTo("jdoe");
        assertThat(result.password()).isNull(); // write-only, never returned
    }

    @Test
    void create_deletesKeycloakUser_whenDbWriteFails() {
        when(repository.existsByUsername("jdoe")).thenReturn(false);
        when(keycloak.createUser(any())).thenReturn("kc-1");
        when(repository.save(any(Teacher.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.create(dto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");

        verify(keycloak).deleteUser("kc-1"); // compensation
        verify(auditRecorder).compensationSucceeded("jdoe", "kc-1"); // audit trail
    }

    @Test
    void create_rejectsDuplicateUsername_withoutTouchingKeycloak() {
        when(repository.existsByUsername("jdoe")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto()))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(keycloak);
    }

    @Test
    void delete_removesDbRowThenKeycloakUser() {
        Teacher teacher = new Teacher();
        teacher.setId("jdoe");
        teacher.setUsername("jdoe");
        teacher.setTenantId(TENANT);
        when(repository.findById("jdoe")).thenReturn(Optional.of(teacher));
        when(keycloak.findUserId("jdoe")).thenReturn(Optional.of("kc-1"));

        service.delete("jdoe");

        InOrder inOrder = inOrder(repository, keycloak);
        inOrder.verify(repository).delete(teacher);
        inOrder.verify(keycloak).deleteUser("kc-1");
    }
}
