package ru.practicum.service;

import ru.practicum.model.dto.event.*;

import java.util.List;

public interface EventService {
    List<EventDto> findByUserId(Long userId, Integer from, Integer size);

    EventDto findByIdAndUser(Long userId, Long eventId);

    List<EventDto> searchCommon(EventSearchCommon search);

    List<EventDto> searchAdmin(EventSearchAdmin search);

    EventDto findById(Long eventId);

    EventDto create(Long userId, EventDto newEventDto);

    EventDto updateByAdmin(long eventId, UpdateAdminEventDto eventDto);

    EventDto updateByUser(Long userId, Long eventId, UpdateEventDto eventDto);
}
