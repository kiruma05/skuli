package com.skuli.academics.internal.web;

import com.skuli.academics.api.dto.SchoolClassDto;
import com.skuli.academics.internal.service.SchoolClassService;
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
 * REST adapter for class groups at {@code /api/v1/classes}. Role access (admin or teacher) is
 * enforced by the security filter chain; tenant scoping and business rules live in
 * {@link SchoolClassService}. Errors render as RFC 7807 problem+json.
 */
@RestController
@RequestMapping("/api/v1/classes")
public class SchoolClassController {

    private final SchoolClassService service;

    public SchoolClassController(SchoolClassService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<SchoolClassDto> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public SchoolClassDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<SchoolClassDto> create(@Valid @RequestBody SchoolClassDto dto) {
        SchoolClassDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/classes/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public SchoolClassDto update(@PathVariable Integer id, @Valid @RequestBody SchoolClassDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
