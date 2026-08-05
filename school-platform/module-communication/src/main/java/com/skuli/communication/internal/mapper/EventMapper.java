package com.skuli.communication.internal.mapper;

import com.skuli.communication.api.dto.EventDto;
import com.skuli.communication.internal.domain.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps {@link Event} to/from {@link EventDto}. */
@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDto toDto(Event entity);

    @Mapping(target = "tenantId", ignore = true)
    Event toEntity(EventDto dto);
}
