package ru.practicum.mapper;


import ru.practicum.model.dto.event.EventDto;
import ru.practicum.model.dto.event.EventState;
import ru.practicum.model.entity.Category;
import ru.practicum.model.entity.Event;
import ru.practicum.model.entity.User;

import java.time.LocalDateTime;

public class EventMapper {
    public static EventDto toEventDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.categoryToDto(event.getCategory()).getId())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }


    public static Event newRequestToEvent(EventDto eventDto, User user, Category category) {
        return Event.builder()
                .initiator(user)
                .category(category)
                .title(eventDto.getTitle())
                .paid(eventDto.getPaid() != null && eventDto.getPaid())
                .requestModeration(eventDto.getRequestModeration() == null || eventDto.getRequestModeration())
                .participantLimit(eventDto.getParticipantLimit() == null ? 0 : eventDto.getParticipantLimit())
                .location(eventDto.getLocation())
                .annotation(eventDto.getAnnotation())
                .eventDate(eventDto.getEventDate())
                .description(eventDto.getDescription())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .confirmedRequests(0L)
                .views(0L)
                .build();
    }
}