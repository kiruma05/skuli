package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.AttendanceDto;
import com.skuli.academics.internal.domain.Attendance;
import com.skuli.academics.internal.mapper.AttendanceMapper;
import com.skuli.academics.internal.repository.AttendanceRepository;
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
 * Application service for attendance records, following the Subject reference pattern: tenant-
 * scoped CRUD over a student and a lesson (referenced by id).
 */
@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository repository;
    private final AttendanceMapper mapper;

    public AttendanceService(AttendanceRepository repository, AttendanceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceDto> list(Pageable pageable) {
        String tenant = requireTenant();
        Specification<Attendance> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenant);
        Page<Attendance> page = repository.findAll(spec, pageable);
        List<AttendanceDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AttendanceDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public AttendanceDto create(AttendanceDto dto) {
        requireTenant();
        Attendance entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    public AttendanceDto update(Integer id, AttendanceDto dto) {
        Attendance entity = load(id);
        entity.setDate(dto.date());
        entity.setPresent(dto.present());
        entity.setStudentId(dto.studentId());
        entity.setLessonId(dto.lessonId());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    private Attendance load(Integer id) {
        return repository.findByIdAndTenantId(id, requireTenant())
                .orElseThrow(() -> ResourceNotFoundException.of("Attendance", id));
    }

    private String requireTenant() {
        String tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant in the request context");
        }
        return tenant;
    }
}
