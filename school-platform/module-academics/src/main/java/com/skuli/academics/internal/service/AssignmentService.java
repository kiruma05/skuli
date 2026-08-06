package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.AssignmentDto;
import com.skuli.academics.internal.domain.Assignment;
import com.skuli.academics.internal.mapper.AssignmentMapper;
import com.skuli.academics.internal.repository.AssignmentRepository;
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
 * Application service for assignments, following the Subject reference pattern: tenant-scoped CRUD
 * over a lesson (referenced by id).
 */
@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository repository;
    private final AssignmentMapper mapper;

    public AssignmentService(AssignmentRepository repository, AssignmentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AssignmentDto> list(String search, Pageable pageable) {
        String tenant = requireTenant();
        Specification<Assignment> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenant);
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), like));
        }
        Page<Assignment> page = repository.findAll(spec, pageable);
        List<AssignmentDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AssignmentDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public AssignmentDto create(AssignmentDto dto) {
        requireTenant();
        Assignment entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    public AssignmentDto update(Integer id, AssignmentDto dto) {
        Assignment entity = load(id);
        entity.setTitle(dto.title());
        entity.setStartDate(dto.startDate());
        entity.setDueDate(dto.dueDate());
        entity.setLessonId(dto.lessonId());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private Assignment load(Integer id) {
        return repository.findByIdAndTenantId(id, requireTenant())
                .orElseThrow(() -> ResourceNotFoundException.of("Assignment", id));
    }

    private String requireTenant() {
        String tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant in the request context");
        }
        return tenant;
    }
}
