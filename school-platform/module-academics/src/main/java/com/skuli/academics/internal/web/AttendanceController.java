package com.skuli.academics.internal.web;

import com.skuli.academics.api.dto.AttendanceDto;
import com.skuli.academics.internal.service.AttendanceService;
import com.skuli.common.util.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for attendance records at {@code /api/v1/attendance} (admin or teacher). Tenant
 * scoping lives in {@link AttendanceService}; errors render as RFC 7807 problem+json.
 */
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AttendanceDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public AttendanceDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<AttendanceDto> create(@Valid @RequestBody AttendanceDto dto) {
        AttendanceDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/attendance/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public AttendanceDto update(@PathVariable Integer id, @Valid @RequestBody AttendanceDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
