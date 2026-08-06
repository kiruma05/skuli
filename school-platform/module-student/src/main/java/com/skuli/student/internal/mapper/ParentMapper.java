package com.skuli.student.internal.mapper;

import com.skuli.student.api.dto.ParentDto;
import com.skuli.student.internal.domain.Parent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link Parent} to/from {@link ParentDto}.
 */
@Mapper(componentModel = "spring")
public interface ParentMapper {

    @Mapping(target = "password", ignore = true) // write-only; never leaves the entity
    ParentDto toDto(Parent entity);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Parent toEntity(ParentDto dto);
}
