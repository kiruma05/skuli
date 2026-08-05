package com.skuli.academics.internal.web;

import com.skuli.academics.api.dto.GradeDto;
import com.skuli.academics.internal.service.GradeService;
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
 * REST adapter for grade levels at {@code /api/v1/grades} (admin-only, like subjects). Tenant
 * scoping and business rules live in {@link GradeService}; errors render as RFC 7807 problem+json.
 */
@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {

    private final GradeService service;

    public GradeController(GradeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<GradeDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public GradeDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<GradeDto> create(@Valid @RequestBody GradeDto dto) {
        GradeDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/grades/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public GradeDto update(@PathVariable Integer id, @Valid @RequestBody GradeDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
