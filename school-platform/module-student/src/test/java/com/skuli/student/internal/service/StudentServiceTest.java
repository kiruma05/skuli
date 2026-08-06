package com.skuli.student.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.skuli.academics.api.ClassCatalog;
import com.skuli.auth.api.KeycloakService;
import com.skuli.auth.api.ProvisioningAuditRecorder;
import com.skuli.common.domain.UserSex;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.security.TenantContext;
import com.skuli.student.api.dto.StudentDto;
import com.skuli.student.internal.domain.Student;
import com.skuli.student.internal.mapper.StudentMapperImpl;
import com.skuli.student.internal.repository.StudentRepository;
import java.time.Instant;
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
 * Unit tests for the student slice: the class-capacity rule plus the Keycloak-then-DB provisioning
 * contract (happy path, compensation, duplicate, delete ordering).
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private static final String TENANT = "default";
    private static final int CLASS_ID = 2;

    @Mock
    private StudentRepository repository;

    @Mock
    private KeycloakService keycloak;

    @Mock
    private ClassCatalog classCatalog;

    @Mock
    private ProvisioningAuditRecorder auditRecorder;

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService(repository, new StudentMapperImpl(), keycloak, classCatalog,
                auditRecorder);
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static StudentDto dto() {
        return new StudentDto("ignored", "sjones", "Sam", "Jones", "s@example.com", "123456",
                "1 Main St", null, "A+", UserSex.FEMALE, Instant.parse("2010-01-01T00:00:00Z"),
                "parent-1", CLASS_ID, 1, "password1", null);
    }

    @Test
    void create_enrolsIntoAClassWithRoom_andProvisionsKeycloak() {
        when(repository.existsByUsername("sjones")).thenReturn(false);
        when(classCatalog.capacityOf(CLASS_ID)).thenReturn(Optional.of(30));
        when(repository.countByClassId(CLASS_ID)).thenReturn(10L);
        when(keycloak.createUser(any())).thenReturn("kc-1");
        when(repository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentDto result = service.create(dto());

        verify(keycloak).assignRealmRole("kc-1", "student");
        ArgumentCaptor<Student> saved = ArgumentCaptor.forClass(Student.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo("sjones"); // id == username
        assertThat(result.password()).isNull();
    }

    @Test
    void create_rejectsWhenClassIsFull_withoutTouchingKeycloak() {
        when(repository.existsByUsername("sjones")).thenReturn(false);
        when(classCatalog.capacityOf(CLASS_ID)).thenReturn(Optional.of(30));
        when(repository.countByClassId(CLASS_ID)).thenReturn(30L);

        assertThatThrownBy(() -> service.create(dto()))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(keycloak);
    }

    @Test
    void create_rejectsWhenClassDoesNotExist() {
        when(repository.existsByUsername("sjones")).thenReturn(false);
        when(classCatalog.capacityOf(CLASS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto()))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(keycloak);
    }

    @Test
    void create_deletesKeycloakUser_whenDbWriteFails() {
        when(repository.existsByUsername("sjones")).thenReturn(false);
        when(classCatalog.capacityOf(CLASS_ID)).thenReturn(Optional.of(30));
        when(repository.countByClassId(CLASS_ID)).thenReturn(10L);
        when(keycloak.createUser(any())).thenReturn("kc-1");
        when(repository.save(any(Student.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.create(dto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");

        verify(keycloak).deleteUser("kc-1");
        verify(auditRecorder).compensationSucceeded("sjones", "kc-1");
    }

    @Test
    void create_rejectsDuplicateUsername_beforeCheckingCapacity() {
        when(repository.existsByUsername("sjones")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto()))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(keycloak, classCatalog);
    }

    @Test
    void delete_removesDbRowThenKeycloakUser() {
        Student student = new Student();
        student.setId("sjones");
        student.setUsername("sjones");
        student.setTenantId(TENANT);
        when(repository.findById("sjones")).thenReturn(Optional.of(student));
        when(keycloak.findUserId("sjones")).thenReturn(Optional.of("kc-1"));

        service.delete("sjones");

        InOrder inOrder = inOrder(repository, keycloak);
        inOrder.verify(repository).delete(student);
        inOrder.verify(keycloak).deleteUser("kc-1");
    }
}
