package com.skuli.student.internal.service;

import com.skuli.academics.api.ClassCatalog;
import com.skuli.auth.api.KeycloakService;
import com.skuli.auth.api.KeycloakService.NewUser;
import com.skuli.auth.api.KeycloakService.UpdateUser;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import com.skuli.student.api.dto.StudentDto;
import com.skuli.student.internal.domain.Student;
import com.skuli.student.internal.mapper.StudentMapper;
import com.skuli.student.internal.repository.StudentRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for students. Tenant isolation is enforced transparently by {@code @TenantId}.
 * Like teachers, creation provisions a Keycloak user before the DB row (with a compensating delete
 * on failure); additionally it enforces the class-capacity rule: a student cannot be enrolled into
 * a class that is already full. The class capacity comes from module-academics via
 * {@link ClassCatalog} — a cross-module call through the exposed interface (itself tenant-scoped).
 */
@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);
    private static final String STUDENT_ROLE = "student";

    private final StudentRepository repository;
    private final StudentMapper mapper;
    private final KeycloakService keycloak;
    private final ClassCatalog classCatalog;

    public StudentService(StudentRepository repository, StudentMapper mapper,
                          KeycloakService keycloak, ClassCatalog classCatalog) {
        this.repository = repository;
        this.mapper = mapper;
        this.keycloak = keycloak;
        this.classCatalog = classCatalog;
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentDto> list(String search, Pageable pageable) {
        Page<Student> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(matches(search), pageable);
        List<StudentDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public StudentDto get(String id) {
        return mapper.toDto(load(id));
    }

    /**
     * Enforces the capacity rule, then provisions Keycloak and persists; the Keycloak user is
     * removed if the DB write fails.
     */
    public StudentDto create(StudentDto dto) {
        String username = dto.username();
        if (repository.existsByUsername(username)) {
            throw new BusinessRuleException("Username already taken: " + username);
        }
        assertClassHasRoom(dto.classId());

        String keycloakUserId = keycloak.createUser(
                new NewUser(username, dto.email(), dto.name(), dto.surname(), dto.password()));
        try {
            keycloak.assignRealmRole(keycloakUserId, STUDENT_ROLE);
            Student entity = mapper.toEntity(dto);
            entity.setId(username); // id == username invariant
            return mapper.toDto(repository.save(entity));
        } catch (RuntimeException ex) {
            compensate(username, keycloakUserId, ex);
            throw ex;
        }
    }

    public StudentDto update(String id, StudentDto dto) {
        Student entity = load(id);
        entity.setName(dto.name());
        entity.setSurname(dto.surname());
        entity.setEmail(dto.email());
        entity.setPhone(dto.phone());
        entity.setAddress(dto.address());
        entity.setImg(dto.img());
        entity.setBloodType(dto.bloodType());
        entity.setSex(dto.sex());
        entity.setBirthday(dto.birthday());
        entity.setParentId(dto.parentId());
        // Re-check capacity only when the student is being moved to a different class.
        if (!entity.getClassId().equals(dto.classId())) {
            assertClassHasRoom(dto.classId());
            entity.setClassId(dto.classId());
        }
        entity.setGradeId(dto.gradeId());
        Student saved = repository.save(entity);

        keycloak.findUserId(id).ifPresent(userId -> keycloak.updateUser(userId,
                new UpdateUser(dto.email(), dto.name(), dto.surname(), dto.password())));
        return mapper.toDto(saved);
    }

    /** Deletes the DB row first, then the Keycloak user (plan §5.3). */
    public void delete(String id) {
        Student entity = load(id);
        repository.delete(entity);
        keycloak.findUserId(id).ifPresent(keycloak::deleteUser);
    }

    /** Rejects enrolment when the target class is full (or does not exist in this tenant). */
    private void assertClassHasRoom(Integer classId) {
        int capacity = classCatalog.capacityOf(classId)
                .orElseThrow(() -> ResourceNotFoundException.of("Class", classId));
        long enrolled = repository.countByClassId(classId);
        if (enrolled >= capacity) {
            throw new BusinessRuleException(
                    "Class " + classId + " is at capacity (" + capacity + ")");
        }
    }

    private Student load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));
    }

    private void compensate(String username, String keycloakUserId, RuntimeException cause) {
        log.error("Persisting student '{}' failed after creating Keycloak user {}; "
                + "compensating by deleting the Keycloak user", username, keycloakUserId, cause);
        try {
            keycloak.deleteUser(keycloakUserId);
            log.info("Compensation succeeded: removed Keycloak user {} for '{}'",
                    keycloakUserId, username);
        } catch (RuntimeException compensationFailure) {
            log.error("Compensation FAILED: Keycloak user {} for '{}' is orphaned and must be "
                    + "removed manually", keycloakUserId, username, compensationFailure);
        }
    }

    private static Specification<Student> matches(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("surname")), like),
                cb.like(cb.lower(root.get("username")), like));
    }
}
