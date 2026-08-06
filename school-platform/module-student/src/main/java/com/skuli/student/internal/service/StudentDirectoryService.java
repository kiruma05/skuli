package com.skuli.student.internal.service;

import com.skuli.student.api.StudentDirectory;
import com.skuli.student.internal.domain.Student;
import com.skuli.student.internal.repository.StudentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements {@link StudentDirectory} over the student repository (tenant-scoped via {@code @TenantId}).
 */
@Service
public class StudentDirectoryService implements StudentDirectory {

    private final StudentRepository repository;

    public StudentDirectoryService(StudentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> classIdOf(String studentId) {
        return repository.findById(studentId).map(Student::getClassId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChildRef> childrenOf(String parentId) {
        return repository.findByParentId(parentId).stream()
                .map(s -> new ChildRef(s.getId(), s.getClassId()))
                .toList();
    }
}
