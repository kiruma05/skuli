package com.skuli.communication.internal.web;

import com.skuli.common.util.PageResponse;
import com.skuli.communication.api.dto.EventDto;
import com.skuli.communication.internal.service.EventService;
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
 * REST adapter for school events at {@code /api/v1/events} (admin or teacher). Tenant scoping lives
 * in {@link EventService}; errors render as RFC 7807 problem+json.
 */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EventDto> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public EventDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<EventDto> create(@Valid @RequestBody EventDto dto) {
        EventDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/events/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public EventDto update(@PathVariable Integer id, @Valid @RequestBody EventDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
