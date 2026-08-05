package com.skuli.student.internal.web;

import com.skuli.common.util.PageResponse;
import com.skuli.common.validation.OnCreate;
import com.skuli.student.api.dto.StudentDto;
import com.skuli.student.internal.service.StudentService;
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
 * REST adapter for students at {@code /api/v1/students} (admin or teacher). Creation validates the
 * {@link OnCreate} group so a password is required on {@code POST} but optional on {@code PUT}.
 * Provisioning, the capacity rule, and tenant scoping live in {@link StudentService}.
 */
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<StudentDto> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public StudentDto get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<StudentDto> create(
            @Validated({Default.class, OnCreate.class}) @RequestBody StudentDto dto) {
        StudentDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/students/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public StudentDto update(@PathVariable String id, @Valid @RequestBody StudentDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
