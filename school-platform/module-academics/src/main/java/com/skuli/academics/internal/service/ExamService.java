package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.ExamDto;
import com.skuli.academics.internal.domain.Exam;
import com.skuli.academics.internal.mapper.ExamMapper;
import com.skuli.academics.internal.repository.ExamRepository;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for exams. Tenant isolation is enforced transparently by {@code @TenantId};
 * the lesson is referenced by id.
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
        Page<Exam> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(titleContains(search), pageable);
        List<ExamDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ExamDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public ExamDto create(ExamDto dto) {
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
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Exam", id));
    }

    private static Specification<Exam> titleContains(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), like);
    }
}
