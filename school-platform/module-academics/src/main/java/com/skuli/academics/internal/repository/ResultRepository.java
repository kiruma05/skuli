package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Result}.
 */
public interface ResultRepository
        extends JpaRepository<Result, Integer>, JpaSpecificationExecutor<Result> {
}
