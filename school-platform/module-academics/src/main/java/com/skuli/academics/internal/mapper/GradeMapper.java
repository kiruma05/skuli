package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.GradeDto;
import com.skuli.academics.internal.domain.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Grade} to/from {@link GradeDto}. */
@Mapper(componentModel = "spring")
public interface GradeMapper {

    GradeDto toDto(Grade entity);

    @Mapping(target = "tenantId", ignore = true)
    Grade toEntity(GradeDto dto);
}
