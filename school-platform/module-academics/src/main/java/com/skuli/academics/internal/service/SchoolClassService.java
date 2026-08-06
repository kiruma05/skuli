package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.SchoolClassDto;
import com.skuli.academics.internal.domain.SchoolClass;
import com.skuli.academics.internal.mapper.SchoolClassMapper;
import com.skuli.academics.internal.repository.SchoolClassRepository;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for class groups. Tenant isolation is enforced transparently by
 * {@code @TenantId}; class names are unique per tenant. The capacity value is stored here; the
 * capacity <em>rule</em> lives with student creation in module-student.
 */
@Service
@Transactional
public class SchoolClassService {

    private final SchoolClassRepository repository;
    private final SchoolClassMapper mapper;

    public SchoolClassService(SchoolClassRepository repository, SchoolClassMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<SchoolClassDto> list(String search, Pageable pageable) {
        Page<SchoolClass> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(nameContains(search), pageable);
        List<SchoolClassDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SchoolClassDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public SchoolClassDto create(SchoolClassDto dto) {
        if (repository.existsByName(dto.name())) {
            throw new BusinessRuleException("A class named '" + dto.name() + "' already exists");
        }
        SchoolClass entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    public SchoolClassDto update(Integer id, SchoolClassDto dto) {
        SchoolClass entity = load(id);
        if (!entity.getName().equals(dto.name()) && repository.existsByName(dto.name())) {
            throw new BusinessRuleException("A class named '" + dto.name() + "' already exists");
        }
        entity.setName(dto.name());
        entity.setCapacity(dto.capacity());
        entity.setSupervisorId(dto.supervisorId());
        entity.setGradeId(dto.gradeId());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private SchoolClass load(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Class", id));
    }

    private static Specification<SchoolClass> nameContains(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
    }
}
