package com.skuli.academics.internal.web;

import com.skuli.academics.api.dto.SubjectDto;
import com.skuli.academics.internal.service.SubjectService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for subjects at {@code /api/v1/subjects}. Role access (admin-only) is enforced by
 * the security filter chain; tenant scoping and business rules live in {@link SubjectService}.
 * Errors are rendered as RFC 7807 problem+json by the shared {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService service;

    public SubjectController(SubjectService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<SubjectDto> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public SubjectDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<SubjectDto> create(@Valid @RequestBody SubjectDto dto) {
        SubjectDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/subjects/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public SubjectDto update(@PathVariable Integer id, @Valid @RequestBody SubjectDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
