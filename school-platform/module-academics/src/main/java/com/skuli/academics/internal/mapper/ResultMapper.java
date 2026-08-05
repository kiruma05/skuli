package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.ResultDto;
import com.skuli.academics.internal.domain.Result;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Result} to/from {@link ResultDto}. */
@Mapper(componentModel = "spring")
public interface ResultMapper {

    ResultDto toDto(Result entity);

    @Mapping(target = "tenantId", ignore = true)
    Result toEntity(ResultDto dto);
}
