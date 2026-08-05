package com.skuli.student.internal.repository;

import com.skuli.student.internal.domain.Parent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Parent}.
 */
public interface ParentRepository
        extends JpaRepository<Parent, String>, JpaSpecificationExecutor<Parent> {

    Optional<Parent> findByUsername(String username);

    boolean existsByUsername(String username);
}
