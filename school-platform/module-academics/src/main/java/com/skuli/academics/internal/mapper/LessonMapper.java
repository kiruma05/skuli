package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.LessonDto;
import com.skuli.academics.internal.domain.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Lesson} to/from {@link LessonDto}. */
@Mapper(componentModel = "spring")
public interface LessonMapper {

    LessonDto toDto(Lesson entity);

    @Mapping(target = "tenantId", ignore = true)
    Lesson toEntity(LessonDto dto);
}
