package com.skuli.student.internal.web;

import com.skuli.common.util.PageResponse;
import com.skuli.common.validation.OnCreate;
import com.skuli.student.api.dto.ParentDto;
import com.skuli.student.internal.service.ParentService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
 * REST adapter for parents at {@code /api/v1/parents} (admin or teacher). Creation validates the
 * {@link OnCreate} group so a password is required on {@code POST} but optional on {@code PUT}.
 * Provisioning and tenant scoping live in {@link ParentService}.
 */
@RestController
@RequestMapping("/api/v1/parents")
public class ParentController {

    private final ParentService service;

    public ParentController(ParentService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ParentDto> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public ParentDto get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<ParentDto> create(
            @Validated({Default.class, OnCreate.class}) @RequestBody ParentDto dto) {
        ParentDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/parents/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ParentDto update(@PathVariable String id, @Valid @RequestBody ParentDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
