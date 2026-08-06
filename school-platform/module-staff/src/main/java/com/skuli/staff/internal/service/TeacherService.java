package com.skuli.staff.internal.service;

import com.skuli.auth.api.KeycloakService;
import com.skuli.auth.api.KeycloakService.NewUser;
import com.skuli.auth.api.KeycloakService.UpdateUser;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import com.skuli.staff.api.dto.TeacherDto;
import com.skuli.staff.internal.domain.Teacher;
import com.skuli.staff.internal.mapper.TeacherMapper;
import com.skuli.staff.internal.repository.TeacherRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for teachers. Tenant isolation is enforced transparently by {@code @TenantId}
 * (queries and inserts are auto-scoped). Creating a teacher provisions a Keycloak user first and
 * only then writes the DB row, because Keycloak is the identity source and is not transactional
 * with the database (plan §5):
 *
 * <ul>
 *   <li><b>Create</b>: Keycloak user -> realm role -> DB row. If the DB write fails, the Keycloak
 *       user is deleted (compensation). id == username throughout.</li>
 *   <li><b>Delete</b>: DB row first, then the Keycloak user.</li>
 *   <li><b>Update</b>: DB row, then propagate profile/password changes to Keycloak.</li>
 * </ul>
 */
@Service
public class TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherService.class);
    private static final String TEACHER_ROLE = "teacher";

    private final TeacherRepository repository;
    private final TeacherMapper mapper;
    private final KeycloakService keycloak;

    public TeacherService(TeacherRepository repository, TeacherMapper mapper,
                          KeycloakService keycloak) {
        this.repository = repository;
        this.mapper = mapper;
        this.keycloak = keycloak;
    }

    @Transactional(readOnly = true)
    public PageResponse<TeacherDto> list(String search, Pageable pageable) {
        Page<Teacher> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(matches(search), pageable);
        List<TeacherDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public TeacherDto get(String id) {
        return mapper.toDto(load(id));
    }

    /**
     * Provisions the Keycloak user, then persists the teacher; deletes the Keycloak user if the
     * DB write fails so the two systems cannot drift.
     */
    public TeacherDto create(TeacherDto dto) {
        String username = dto.username();
        if (repository.existsByUsername(username)) {
            throw new BusinessRuleException("Username already taken: " + username);
        }

        String keycloakUserId = keycloak.createUser(
                new NewUser(username, dto.email(), dto.name(), dto.surname(), dto.password()));
        try {
            keycloak.assignRealmRole(keycloakUserId, TEACHER_ROLE);
            Teacher entity = mapper.toEntity(dto);
            entity.setId(username); // id == username invariant
            return mapper.toDto(repository.save(entity));
        } catch (RuntimeException ex) {
            compensate(username, keycloakUserId, ex);
            throw ex;
        }
    }

    public TeacherDto update(String id, TeacherDto dto) {
        Teacher entity = load(id);
        entity.setName(dto.name());
        entity.setSurname(dto.surname());
        entity.setEmail(dto.email());
        entity.setPhone(dto.phone());
        entity.setAddress(dto.address());
        entity.setImg(dto.img());
        entity.setBloodType(dto.bloodType());
        entity.setSex(dto.sex());
        entity.setBirthday(dto.birthday());
        if (dto.subjectIds() != null) {
            entity.setSubjectIds(dto.subjectIds());
        }
        Teacher saved = repository.save(entity);

        keycloak.findUserId(id).ifPresent(userId -> keycloak.updateUser(userId,
                new UpdateUser(dto.email(), dto.name(), dto.surname(), dto.password())));
        return mapper.toDto(saved);
    }

    /** Deletes the DB row first, then the Keycloak user (plan §5.3). */
    public void delete(String id) {
        Teacher entity = load(id);
        repository.delete(entity);
        keycloak.findUserId(id).ifPresent(keycloak::deleteUser);
    }

    private Teacher load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", id));
    }

    private void compensate(String username, String keycloakUserId, RuntimeException cause) {
        log.error("Persisting teacher '{}' failed after creating Keycloak user {}; "
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

    private static Specification<Teacher> matches(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("surname")), like),
                cb.like(cb.lower(root.get("username")), like));
    }
}
