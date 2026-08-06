package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.ExamDto;
import com.skuli.academics.internal.domain.Exam;
import com.skuli.academics.internal.mapper.ExamMapper;
import com.skuli.academics.internal.repository.ExamRepository;
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
 * Application service for exams, following the Subject reference pattern: tenant-scoped CRUD over
 * a lesson (referenced by id).
 */
@Service
@Transactional
public class ExamService {

    private final ExamRepository repository;
    private final ExamMapper mapper;

    public ExamService(ExamRepository repository, ExamMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ExamDto> list(String search, Pageable pageable) {
        String tenant = requireTenant();
        Specification<Exam> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenant);
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), like));
        }
        Page<Exam> page = repository.findAll(spec, pageable);
        List<ExamDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ExamDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public ExamDto create(ExamDto dto) {
        requireTenant();
        Exam entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    public ExamDto update(Integer id, ExamDto dto) {
        Exam entity = load(id);
        entity.setTitle(dto.title());
        entity.setStartTime(dto.startTime());
        entity.setEndTime(dto.endTime());
        entity.setLessonId(dto.lessonId());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private Exam load(Integer id) {
        return repository.findByIdAndTenantId(id, requireTenant())
                .orElseThrow(() -> ResourceNotFoundException.of("Exam", id));
    }

    private String requireTenant() {
        String tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant in the request context");
        }
        return tenant;
    }
}
