package com.skuli.student.internal.service;

import com.skuli.auth.api.KeycloakService;
import com.skuli.auth.api.KeycloakService.NewUser;
import com.skuli.auth.api.KeycloakService.UpdateUser;
import com.skuli.auth.api.ProvisioningAuditRecorder;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import com.skuli.student.api.dto.ParentDto;
import com.skuli.student.internal.domain.Parent;
import com.skuli.student.internal.mapper.ParentMapper;
import com.skuli.student.internal.repository.ParentRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for parents/guardians. Tenant isolation is enforced transparently by
 * {@code @TenantId}. Follows the same Keycloak-then-DB provisioning contract as teachers and
 * students (role {@code parent}, {@code id == username}, compensating delete on DB failure,
 * DB-then-Keycloak on delete), with no additional business rules.
 */
@Service
public class ParentService {

    private static final Logger log = LoggerFactory.getLogger(ParentService.class);
    private static final String PARENT_ROLE = "parent";

    private final ParentRepository repository;
    private final ParentMapper mapper;
    private final KeycloakService keycloak;
    private final ProvisioningAuditRecorder auditRecorder;

    public ParentService(ParentRepository repository, ParentMapper mapper, KeycloakService keycloak,
                         ProvisioningAuditRecorder auditRecorder) {
        this.repository = repository;
        this.mapper = mapper;
        this.keycloak = keycloak;
        this.auditRecorder = auditRecorder;
    }

    @Transactional(readOnly = true)
    public PageResponse<ParentDto> list(String search, Pageable pageable) {
        Page<Parent> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(matches(search), pageable);
        List<ParentDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ParentDto get(String id) {
        return mapper.toDto(load(id));
    }

    public ParentDto create(ParentDto dto) {
        String username = dto.username();
        if (repository.existsByUsername(username)) {
            throw new BusinessRuleException("Username already taken: " + username);
        }

        String keycloakUserId = keycloak.createUser(
                new NewUser(username, dto.email(), dto.name(), dto.surname(), dto.password()));
        try {
            keycloak.assignRealmRole(keycloakUserId, PARENT_ROLE);
            Parent entity = mapper.toEntity(dto);
            entity.setId(username); // id == username invariant
            return mapper.toDto(repository.save(entity));
        } catch (RuntimeException ex) {
            compensate(username, keycloakUserId, ex);
            throw ex;
        }
    }

    public ParentDto update(String id, ParentDto dto) {
        Parent entity = load(id);
        entity.setName(dto.name());
        entity.setSurname(dto.surname());
        entity.setEmail(dto.email());
        entity.setPhone(dto.phone());
        entity.setAddress(dto.address());
        Parent saved = repository.save(entity);

        keycloak.findUserId(id).ifPresent(userId -> keycloak.updateUser(userId,
                new UpdateUser(dto.email(), dto.name(), dto.surname(), dto.password())));
        return mapper.toDto(saved);
    }

    /** Deletes the DB row first, then the Keycloak user (plan §5.3). */
    public void delete(String id) {
        Parent entity = load(id);
        repository.delete(entity);
        keycloak.findUserId(id).ifPresent(keycloak::deleteUser);
    }

    private Parent load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Parent", id));
    }

    private void compensate(String username, String keycloakUserId, RuntimeException cause) {
        log.error("Persisting parent '{}' failed after creating Keycloak user {}; "
                + "compensating by deleting the Keycloak user", username, keycloakUserId, cause);
        try {
            keycloak.deleteUser(keycloakUserId);
            log.info("Compensation succeeded: removed Keycloak user {} for '{}'",
                    keycloakUserId, username);
            auditRecorder.compensationSucceeded(username, keycloakUserId);
        } catch (RuntimeException compensationFailure) {
            log.error("Compensation FAILED: Keycloak user {} for '{}' is orphaned and must be "
                    + "removed manually", keycloakUserId, username, compensationFailure);
            auditRecorder.compensationFailed(username, keycloakUserId,
                    compensationFailure.getMessage());
        }
    }

    private static Specification<Parent> matches(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("surname")), like),
                cb.like(cb.lower(root.get("username")), like));
    }
}
