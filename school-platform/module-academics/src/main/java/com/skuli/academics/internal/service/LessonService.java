package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.LessonDto;
import com.skuli.academics.internal.domain.Lesson;
import com.skuli.academics.internal.mapper.LessonMapper;
import com.skuli.academics.internal.repository.LessonRepository;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for lessons. Tenant isolation is enforced transparently by {@code @TenantId};
 * subject, class and teacher are referenced by id and validated at the DB (foreign keys).
 */
@Service
@Transactional
public class LessonService {

    private final LessonRepository repository;
    private final LessonMapper mapper;

    public LessonService(LessonRepository repository, LessonMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<LessonDto> list(String search, Pageable pageable) {
        Page<Lesson> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(nameContains(search), pageable);
        List<LessonDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public LessonDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public LessonDto create(LessonDto dto) {
        Lesson entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    public LessonDto update(Integer id, LessonDto dto) {
        Lesson entity = load(id);
        entity.setName(dto.name());
        entity.setDay(dto.day());
        entity.setStartTime(dto.startTime());
        entity.setEndTime(dto.endTime());
        entity.setSubjectId(dto.subjectId());
        entity.setClassId(dto.classId());
        entity.setTeacherId(dto.teacherId());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private Lesson load(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Lesson", id));
    }

    private static Specification<Lesson> nameContains(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
    }
}
