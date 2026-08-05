package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.SubjectDto;
import com.skuli.academics.internal.domain.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Subject} to/from {@link SubjectDto}. */
@Mapper(componentModel = "spring")
public interface SubjectMapper {

    SubjectDto toDto(Subject entity);

    @Mapping(target = "tenantId", ignore = true)
    Subject toEntity(SubjectDto dto);
}
