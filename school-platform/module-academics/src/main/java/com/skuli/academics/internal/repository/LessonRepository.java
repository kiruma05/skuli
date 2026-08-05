package com.skuli.academics.internal.repository;

import com.skuli.academics.internal.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data access for {@link Lesson}.
 */
public interface LessonRepository
        extends JpaRepository<Lesson, Integer>, JpaSpecificationExecutor<Lesson> {
}
