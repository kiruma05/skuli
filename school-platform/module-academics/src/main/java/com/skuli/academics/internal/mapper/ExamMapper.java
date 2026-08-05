package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.ExamDto;
import com.skuli.academics.internal.domain.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Exam} to/from {@link ExamDto}. */
@Mapper(componentModel = "spring")
public interface ExamMapper {

    ExamDto toDto(Exam entity);

    @Mapping(target = "tenantId", ignore = true)
    Exam toEntity(ExamDto dto);
}
