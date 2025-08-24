package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.model.dto.*;
import ru.practicum.model.entity.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    Event toEntity(RequestEventDto requestEventDto);

    ResponseEventFullDto toFullDto(Event event);

    ResponseEventShortDto toShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    void updateEntityFromUserRequest(UpdateEventUserRequest request, @MappingTarget Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    void updateEntityFromAdminRequest(UpdateEventAdminRequest request, @MappingTarget Event event);
}
