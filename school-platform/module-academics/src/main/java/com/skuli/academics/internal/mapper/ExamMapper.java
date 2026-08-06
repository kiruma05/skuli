package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.ExamDto;
import com.skuli.academics.internal.domain.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps write {@link ExamDto}s to {@link Exam}. Reads are assembled as an enriched view in the
 * service, so no {@code toDto} is generated here. */
@Mapper(componentModel = "spring")
public interface ExamMapper {

    @Mapping(target = "tenantId", ignore = true)
    Exam toEntity(ExamDto dto);
}
