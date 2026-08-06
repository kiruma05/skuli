package com.skuli.academics.internal.web;

import com.skuli.academics.api.dto.LessonDto;
import com.skuli.academics.internal.service.LessonService;
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
 * REST adapter for lessons at {@code /api/v1/lessons} (admin or teacher). Tenant scoping lives in
 * {@link LessonService}; errors render as RFC 7807 problem+json.
 */
@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {

    private final LessonService service;

    public LessonController(LessonService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<LessonDto> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public LessonDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<LessonDto> create(@Valid @RequestBody LessonDto dto) {
        LessonDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/lessons/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public LessonDto update(@PathVariable Integer id, @Valid @RequestBody LessonDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
