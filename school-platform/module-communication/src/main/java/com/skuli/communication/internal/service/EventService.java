package com.skuli.communication.internal.service;

import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.security.TenantContext;
import com.skuli.common.util.PageResponse;
import com.skuli.communication.api.dto.EventDto;
import com.skuli.communication.internal.domain.Event;
import com.skuli.communication.internal.mapper.EventMapper;
import com.skuli.communication.internal.repository.EventRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for school events, following the Subject reference pattern: tenant-scoped
 * CRUD. The class is referenced by id and is optional (null = school-wide).
 */
@Service
@Transactional
public class EventService {

    private final EventRepository repository;
    private final EventMapper mapper;

    public EventService(EventRepository repository, EventMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<EventDto> list(String search, Pageable pageable) {
        String tenant = requireTenant();
        Specification<Event> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenant);
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), like));
        }
        Page<Event> page = repository.findAll(spec, pageable);
        List<EventDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public EventDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public EventDto create(EventDto dto) {
        requireTenant();
        Event entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    public EventDto update(Integer id, EventDto dto) {
        Event entity = load(id);
        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
        entity.setStartTime(dto.startTime());
        entity.setEndTime(dto.endTime());
        entity.setClassId(dto.classId());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private Event load(Integer id) {
        return repository.findByIdAndTenantId(id, requireTenant())
                .orElseThrow(() -> ResourceNotFoundException.of("Event", id));
    }

    private String requireTenant() {
        String tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant in the request context");
        }
        return tenant;
    }
}
