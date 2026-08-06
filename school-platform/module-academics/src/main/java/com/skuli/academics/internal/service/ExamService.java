package com.skuli.academics.internal.service;

import com.skuli.academics.api.dto.ExamDto;
import com.skuli.academics.internal.domain.Exam;
import com.skuli.academics.internal.domain.Lesson;
import com.skuli.academics.internal.domain.SchoolClass;
import com.skuli.academics.internal.domain.Subject;
import com.skuli.academics.internal.mapper.ExamMapper;
import com.skuli.academics.internal.repository.ExamRepository;
import com.skuli.academics.internal.repository.LessonRepository;
import com.skuli.academics.internal.repository.SchoolClassRepository;
import com.skuli.academics.internal.repository.SubjectRepository;
import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.util.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for exams. Tenant isolation is enforced transparently by {@code @TenantId}.
 * Reads return a view enriched with the exam's lesson/subject/class names (resolved from the
 * lesson) so the UI can render human-readable columns without extra round-trips.
 */
@Service
@Transactional
public class ExamService {

    private final ExamRepository repository;
    private final ExamMapper mapper;
    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository classRepository;

    public ExamService(ExamRepository repository, ExamMapper mapper,
                       LessonRepository lessonRepository, SubjectRepository subjectRepository,
                       SchoolClassRepository classRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.lessonRepository = lessonRepository;
        this.subjectRepository = subjectRepository;
        this.classRepository = classRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ExamDto> list(String search, Pageable pageable) {
        Page<Exam> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findAll(titleContains(search), pageable);
        List<ExamDto> content = page.getContent().stream().map(this::toView).toList();
        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ExamDto get(Integer id) {
        return toView(load(id));
    }

    public ExamDto create(ExamDto dto) {
        Exam entity = mapper.toEntity(dto);
        entity.setId(null);
        return toView(repository.save(entity));
    }

    public ExamDto update(Integer id, ExamDto dto) {
        Exam entity = load(id);
        entity.setTitle(dto.title());
        entity.setStartTime(dto.startTime());
        entity.setEndTime(dto.endTime());
        entity.setLessonId(dto.lessonId());
        return toView(repository.save(entity));
    }

    public void delete(Integer id) {
        repository.delete(load(id));
    }

    /** Builds the read view, resolving the lesson's name/subject/class/teacher (all tenant-scoped). */
    private ExamDto toView(Exam exam) {
        String lessonName = null;
        String subjectName = null;
        String className = null;
        String teacherId = null;
        Lesson lesson = lessonRepository.findById(exam.getLessonId()).orElse(null);
        if (lesson != null) {
            lessonName = lesson.getName();
            teacherId = lesson.getTeacherId();
            subjectName = subjectRepository.findById(lesson.getSubjectId())
                    .map(Subject::getName).orElse(null);
            className = classRepository.findById(lesson.getClassId())
                    .map(SchoolClass::getName).orElse(null);
        }
        return new ExamDto(exam.getId(), exam.getTitle(), exam.getStartTime(), exam.getEndTime(),
                exam.getLessonId(), lessonName, subjectName, className, teacherId);
    }

    private Exam load(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Exam", id));
    }

    private static Specification<Exam> titleContains(String search) {
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), like);
    }
}
