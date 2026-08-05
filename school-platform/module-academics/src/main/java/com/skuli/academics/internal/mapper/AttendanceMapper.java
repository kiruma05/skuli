package com.skuli.academics.internal.mapper;

import com.skuli.academics.api.dto.AttendanceDto;
import com.skuli.academics.internal.domain.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Attendance} to/from {@link AttendanceDto}. */
@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    AttendanceDto toDto(Attendance entity);

    @Mapping(target = "tenantId", ignore = true)
    Attendance toEntity(AttendanceDto dto);
}
