package com.skuli.staff.internal.web;

import com.skuli.common.util.PageResponse;
import com.skuli.common.validation.OnCreate;
import com.skuli.staff.api.dto.TeacherDto;
import com.skuli.staff.internal.service.TeacherService;
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
 * REST adapter for teachers at {@code /api/v1/teachers} (admin or teacher). Creation additionally
 * validates the {@link OnCreate} group so a password is required on {@code POST} but optional on
 * {@code PUT}. Provisioning and tenant scoping live in {@link TeacherService}.
 */
@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService service;

    public TeacherController(TeacherService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<TeacherDto> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public TeacherDto get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<TeacherDto> create(
            @Validated({Default.class, OnCreate.class}) @RequestBody TeacherDto dto) {
        TeacherDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/teachers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public TeacherDto update(@PathVariable String id, @Valid @RequestBody TeacherDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
