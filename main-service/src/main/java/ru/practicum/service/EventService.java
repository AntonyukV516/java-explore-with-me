package ru.practicum.service;

import ru.practicum.model.dto.*;

import java.util.List;

public interface EventService {

    List<ResponseEventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    ResponseEventFullDto addEvent(Long userId, RequestEventDto requestEventDto);

    ResponseEventFullDto getEventByUser(Long userId, Long eventId);

    ResponseEventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<ResponseEventFullDto> getEventsByAdmin(List<Long> users, List<String> states,
                                        List<Long> categories, String rangeStart,
                                        String rangeEnd, Integer from, Integer size);

    ResponseEventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    List<ResponseEventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                        String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                        String sort, Integer from, Integer size);

    ResponseEventFullDto getEventPublic(Long id);
}
