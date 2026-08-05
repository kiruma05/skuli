package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.GradeDto;
import com.skuli.academics.internal.domain.Grade;
import com.skuli.academics.internal.mapper.GradeMapper;
import com.skuli.academics.internal.repository.GradeRepository;
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
 * Application service for grade levels, following the Subject reference pattern. The natural key
 * is {@code level} (unique per tenant) rather than a name.
 */
@Service
@Transactional
public class GradeService {

    private final GradeRepository repository;
    private final GradeMapper mapper;

    public GradeService(GradeRepository repository, GradeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<GradeDto> list(Pageable pageable) {
        String tenant = requireTenant();
        Specification<Grade> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenant);
        Page<Grade> page = repository.findAll(spec, pageable);
        List<GradeDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public GradeDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public GradeDto create(GradeDto dto) {
        String tenant = requireTenant();
        if (repository.existsByTenantIdAndLevel(tenant, dto.level())) {
            throw new BusinessRuleException("A grade with level " + dto.level() + " already exists");
        }
        Grade entity = mapper.toEntity(dto);
        entity.setId(null); // never trust a client-supplied id on create; the DB assigns it
        return mapper.toDto(repository.save(entity));
    }

    public GradeDto update(Integer id, GradeDto dto) {
        String tenant = requireTenant();
        Grade entity = load(id);
        if (!entity.getLevel().equals(dto.level())
                && repository.existsByTenantIdAndLevel(tenant, dto.level())) {
            throw new BusinessRuleException("A grade with level " + dto.level() + " already exists");
        }
        entity.setLevel(dto.level());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private Grade load(Integer id) {
        return repository.findByIdAndTenantId(id, requireTenant())
                .orElseThrow(() -> ResourceNotFoundException.of("Grade", id));
    }

    private String requireTenant() {
        String tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant in the request context");
        }
        return tenant;
    }
}
