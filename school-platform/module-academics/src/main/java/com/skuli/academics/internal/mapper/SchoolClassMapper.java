package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.SchoolClassDto;
import com.skuli.academics.internal.domain.SchoolClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link SchoolClass} to/from {@link SchoolClassDto}. */
@Mapper(componentModel = "spring")
public interface SchoolClassMapper {

    SchoolClassDto toDto(SchoolClass entity);

    @Mapping(target = "tenantId", ignore = true)
    SchoolClass toEntity(SchoolClassDto dto);
}
