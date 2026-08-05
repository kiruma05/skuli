package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.AssignmentDto;
import com.skuli.academics.internal.domain.Assignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Assignment} to/from {@link AssignmentDto}. */
@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    AssignmentDto toDto(Assignment entity);

    @Mapping(target = "tenantId", ignore = true)
    Assignment toEntity(AssignmentDto dto);
}
