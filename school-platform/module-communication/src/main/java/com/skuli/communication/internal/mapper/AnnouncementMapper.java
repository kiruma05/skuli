package com.skuli.communication.internal.mapper;

import com.skuli.communication.api.dto.AnnouncementDto;
import com.skuli.communication.internal.domain.Announcement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Announcement} to/from {@link AnnouncementDto}. */
@Mapper(componentModel = "spring")
public interface AnnouncementMapper {

    AnnouncementDto toDto(Announcement entity);

    @Mapping(target = "tenantId", ignore = true)
    Announcement toEntity(AnnouncementDto dto);
}
