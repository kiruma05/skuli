package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.ResultDto;
import com.skuli.academics.internal.domain.Result;
import com.skuli.academics.internal.mapper.ResultMapper;
import com.skuli.academics.internal.repository.ResultRepository;
import com.skuli.common.error.BusinessRuleException;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for results. Tenant isolation is enforced transparently by {@code @TenantId}.
 * One business rule: a result must reference <em>exactly one</em> of an exam or an assignment
 * (never both, never neither), mirroring the mutually-exclusive nullable foreign keys.
 */
@Service
@Transactional
public class ResultService {

    private final ResultRepository repository;
    private final ResultMapper mapper;

    public ResultService(ResultRepository repository, ResultMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ResultDto> list(Pageable pageable) {
        Page<Result> page = repository.findAll(pageable);
        List<ResultDto> content = page.getContent().stream().map(mapper::toDto).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ResultDto get(Integer id) {
        return mapper.toDto(load(id));
    }

    public ResultDto create(ResultDto dto) {
        requireExactlyOneSource(dto);
        Result entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    public ResultDto update(Integer id, ResultDto dto) {
        requireExactlyOneSource(dto);
        Result entity = load(id);
        entity.setScore(dto.score());
        entity.setExamId(dto.examId());
        entity.setAssignmentId(dto.assignmentId());
        entity.setStudentId(dto.studentId());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    /** A result belongs to an exam XOR an assignment. */
    private void requireExactlyOneSource(ResultDto dto) {
        boolean hasExam = dto.examId() != null;
        boolean hasAssignment = dto.assignmentId() != null;
        if (hasExam == hasAssignment) {
            throw new BusinessRuleException(
                    "A result must reference exactly one of an exam or an assignment");
        }
    }

    private Result load(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Result", id));
    }
}
