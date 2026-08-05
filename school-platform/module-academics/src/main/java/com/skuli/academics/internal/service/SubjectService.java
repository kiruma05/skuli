package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.SubjectDto;
import com.skuli.academics.internal.domain.Subject;
import com.skuli.academics.internal.mapper.SubjectMapper;
import com.skuli.academics.internal.repository.SubjectRepository;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.security.TenantContext;
import com.skuli.common.util.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for subjects — the Phase 3 reference vertical slice that every other
 * resource copies. All operations are scoped to the current request's tenant (school) read from
 * {@link TenantContext}: a caller can never read, mutate, or collide with another school's data.
 * Subject names are unique per tenant, enforced here and mirrored by a DB constraint.
 */
@Service
@Transactional
public class SubjectService {

    private final SubjectRepository repository;
    private final SubjectMapper mapper;

    public SubjectService(SubjectRepository repository, SubjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<SubjectDto> list(String search, Pageable pageable) {
        String tenant = requireTenant();
        Specification<Subject> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenant);
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), like));
        }
        Page<Subject> page = repository.findAll(spec, pageable);
        List<SubjectDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SubjectDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public SubjectDto create(SubjectDto dto) {
        String tenant = requireTenant();
        if (repository.existsByTenantIdAndName(tenant, dto.name())) {
            throw new BusinessRuleException("A subject named '" + dto.name() + "' already exists");
        }
        Subject entity = mapper.toEntity(dto);
        entity.setId(null); // never trust a client-supplied id on create; the DB assigns it
        return mapper.toDto(repository.save(entity));
    }

    public SubjectDto update(Integer id, SubjectDto dto) {
        String tenant = requireTenant();
        Subject entity = load(id);
        if (!entity.getName().equals(dto.name())
                && repository.existsByTenantIdAndName(tenant, dto.name())) {
            throw new BusinessRuleException("A subject named '" + dto.name() + "' already exists");
        }
        entity.setName(dto.name());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private Subject load(Integer id) {
        return repository.findByIdAndTenantId(id, requireTenant())
                .orElseThrow(() -> ResourceNotFoundException.of("Subject", id));
    }

    private String requireTenant() {
        String tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant in the request context");
        }
        return tenant;
    }
}
