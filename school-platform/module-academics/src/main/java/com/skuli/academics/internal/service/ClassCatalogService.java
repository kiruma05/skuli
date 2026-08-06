package com.skuli.academics.internal.service;

import com.skuli.academics.api.ClassCatalog;
import com.skuli.academics.internal.domain.SchoolClass;
import com.skuli.academics.internal.repository.SchoolClassRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the {@link ClassCatalog} cross-module contract over the class repository. The lookup
 * is tenant-scoped automatically by Hibernate's {@code @TenantId} discriminator, so a caller in
 * another module can only ever see its own tenant's classes.
 */
@Service
public class ClassCatalogService implements ClassCatalog {

    private final SchoolClassRepository repository;

    public ClassCatalogService(SchoolClassRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> capacityOf(Integer classId) {
        return repository.findById(classId).map(SchoolClass::getCapacity);
    }
}
