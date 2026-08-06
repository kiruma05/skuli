package com.skuli.communication.internal.service;

import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import com.skuli.communication.api.dto.AnnouncementDto;
import com.skuli.communication.internal.domain.Announcement;
import com.skuli.communication.internal.mapper.AnnouncementMapper;
import com.skuli.communication.internal.repository.AnnouncementRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for school announcements. Tenant isolation is enforced transparently by
 * {@code @TenantId}; the class is referenced by id and is optional (null = school-wide).
 */
@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository repository;
    private final AnnouncementMapper mapper;

    public AnnouncementService(AnnouncementRepository repository, AnnouncementMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AnnouncementDto> list(String search, Pageable pageable) {
        Page<Announcement> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(titleContains(search), pageable);
        List<AnnouncementDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AnnouncementDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public AnnouncementDto create(AnnouncementDto dto) {
        Announcement entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    public AnnouncementDto update(Integer id, AnnouncementDto dto) {
        Announcement entity = load(id);
        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
        entity.setDate(dto.date());
        entity.setClassId(dto.classId());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private Announcement load(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Announcement", id));
    }

    private static Specification<Announcement> titleContains(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), like);
    }
}
