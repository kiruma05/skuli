package com.skuli.academics.internal.service;

import com.skuli.academics.api.ClassCatalog;
import com.skuli.academics.internal.domain.SchoolClass;
import com.skuli.academics.internal.repository.SchoolClassRepository;
import com.skuli.common.security.TenantContext;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the {@link ClassCatalog} cross-module contract over the class repository, tenant-
 * scoped via {@link TenantContext}.
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
        String tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant in the request context");
        }
        return repository.findByIdAndTenantId(classId, tenant).map(SchoolClass::getCapacity);
    }
}
