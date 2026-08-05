package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.SchoolClass;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link SchoolClass}.
 */
public interface SchoolClassRepository
        extends JpaRepository<SchoolClass, Integer>, JpaSpecificationExecutor<SchoolClass> {

    Optional<SchoolClass> findByName(String name);
}
