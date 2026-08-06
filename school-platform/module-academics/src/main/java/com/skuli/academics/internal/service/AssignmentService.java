package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.AssignmentDto;
import com.skuli.academics.internal.domain.Assignment;
import com.skuli.academics.internal.mapper.AssignmentMapper;
import com.skuli.academics.internal.repository.AssignmentRepository;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for assignments. Tenant isolation is enforced transparently by
 * {@code @TenantId}; the lesson is referenced by id.
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
        Page<Assignment> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(titleContains(search), pageable);
        List<AssignmentDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AssignmentDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public AssignmentDto create(AssignmentDto dto) {
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
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Assignment", id));
    }

    private static Specification<Assignment> titleContains(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), like);
    }
}
