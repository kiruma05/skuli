package com.skuli.staff.internal.mapper;

import com.skuli.staff.api.dto.AdminDto;
import com.skuli.staff.internal.domain.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link Admin} to/from {@link AdminDto}.
 */
@Mapper(componentModel = "spring")
public interface AdminMapper {

    AdminDto toDto(Admin entity);

    @Mapping(target = "tenantId", ignore = true)
    Admin toEntity(AdminDto dto);
}
