package com.skuli.academics.internal.web;

import com.skuli.academics.api.dto.ResultDto;
import com.skuli.academics.internal.service.ResultService;
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
 * REST adapter for results at {@code /api/v1/results} (admin or teacher). The exam-XOR-assignment
 * rule and tenant scoping live in {@link ResultService}; errors render as RFC 7807 problem+json.
 */
@RestController
@RequestMapping("/api/v1/results")
public class ResultController {

    private final ResultService service;

    public ResultController(ResultService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ResultDto> list(@PageableDefault(size = 10) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public ResultDto get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<ResultDto> create(@Valid @RequestBody ResultDto dto) {
        ResultDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/results/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResultDto update(@PathVariable Integer id, @Valid @RequestBody ResultDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
