package com.skuli.staff.internal.mapper;

import com.skuli.staff.api.dto.TeacherDto;
import com.skuli.staff.internal.domain.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link Teacher} to/from {@link TeacherDto}. Tenant and audit fields are owned by the
 * persistence layer ({@code tenant_id} from the tenant context, {@code created_at} from JPA
 * auditing), so they are never taken from an inbound DTO.
 */
@Mapper(componentModel = "spring")
public interface TeacherMapper {

    @Mapping(target = "password", ignore = true) // write-only; never leaves the entity
    TeacherDto toDto(Teacher entity);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Teacher toEntity(TeacherDto dto);
}
