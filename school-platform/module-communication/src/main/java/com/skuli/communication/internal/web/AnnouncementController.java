package com.skuli.communication.internal.web;

import com.skuli.common.util.PageResponse;
import com.skuli.communication.api.dto.AnnouncementDto;
import com.skuli.communication.internal.service.AnnouncementService;
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
 * REST adapter for announcements at {@code /api/v1/announcements} (admin or teacher). Tenant
 * scoping lives in {@link AnnouncementService}; errors render as RFC 7807 problem+json.
 */
@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AnnouncementDto> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public AnnouncementDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<AnnouncementDto> create(@Valid @RequestBody AnnouncementDto dto) {
        AnnouncementDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/announcements/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public AnnouncementDto update(@PathVariable Integer id, @Valid @RequestBody AnnouncementDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
