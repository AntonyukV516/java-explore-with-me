package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.SimpleDateTimeFormatter;
import ru.practicum.dto.StatsDto;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.dto.event.*;
import ru.practicum.model.entity.Category;
import ru.practicum.model.entity.Event;
import ru.practicum.model.entity.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.EventService;
import ru.practicum.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final StatsClient statsClient;

    @Override
    public List<EventDto> findByUserId(Long userId, Integer from, Integer size) {
        return eventRepository.findAllByInitiatorId(userId, from, size)
                .stream()
                .map(EventMapper::toEventDto)
                .toList();
    }

    @Override
    public EventDto findByIdAndUser(Long userId, Long eventId) {
        userService.findUserById(userId);
        Event event = findEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Просмотр полной информации о событии доступен только для создателя события");
        }

        return EventMapper.toEventDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> searchCommon(EventSearchCommon search) {
        if (search.getRangeEnd() != null && search.getRangeStart() != null &&
                search.getRangeEnd().isBefore(search.getRangeStart())) {
            throw new DateValidationException("Дата начала не должна быть позже даты окончания");
        }

        List<Event> events = eventRepository.findCommonEventsByFilters(search);

        Map<Long, Long> viewsMap = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> getViewsFromStatsServer(event.getId())
                ));

        return events.stream()
                .map(event -> {
                    EventDto dto = EventMapper.toEventDto(event);
                    dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<EventDto> searchAdmin(EventSearchAdmin search) {
        List<Event> events = eventRepository.findAdminEventsByFilters(search);
        return events.stream()
                .map(EventMapper::toEventDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventDto findById(Long eventId) {
        Event event = findEventById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        Long currentViews = getViewsFromStatsServer(eventId);

        EventDto dto = EventMapper.toEventDto(event);

        dto.setViews(currentViews == 0L ? 1L : currentViews);

        return dto;
    }

    private Long getViewsFromStatsServer(Long eventId) {
        try {
            List<StatsDto> stats = statsClient.getStats(
                    "1900-01-01 00:00:00",
                    SimpleDateTimeFormatter.toString(LocalDateTime.now()),
                    List.of("/events/" + eventId),
                    true
            );
            return stats.isEmpty() ? 0L : stats.getFirst().getHits();
        } catch (Exception e) {
            log.warn("Failed to get views from stats server: {}", e.getMessage());
            return 0L;
        }
    }

    @Transactional
    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    @Override
    public EventDto create(Long userId, EventDto newEventDto) {
        User initiator = userService.findUserById(userId);
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена"));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DateValidationException("Дата начала события должна быть не ранее чем через 2 часа от даты создания.");
        }
        Event e = EventMapper.newRequestToEvent(newEventDto, initiator, category);
        Event e1 = eventRepository.save(e);
        return EventMapper.toEventDto(e1);
    }

    @Override
    public EventDto updateByAdmin(long eventId, UpdateAdminEventDto eventDto) {
        Event event = findEventById(eventId);
        LocalDateTime eventDate = eventDto.getEventDate() == null ? event.getEventDate() : eventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new DateValidationException("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.");
        }
        if (event.getState() == EventState.PUBLISHED && eventDto.getStateAction() == EventAdminStateAction.REJECT_EVENT) {
            throw new ConditionsNotMetException("Опубликованное событие нельзя отклонить.");
        }
        if (event.getState() != EventState.PENDING && eventDto.getStateAction() == EventAdminStateAction.PUBLISH_EVENT) {
            throw new ConditionsNotMetException("Опубликовать можно только событие в состоянии ожидания.");
        }
        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(() -> new NotFoundException("Категория с id=" + eventDto.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        event.setAnnotation(eventDto.getAnnotation() == null ? event.getAnnotation() : eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription() == null ? event.getDescription() : eventDto.getDescription());
        event.setEventDate(eventDate);
        event.setPaid(eventDto.getPaid() == null ? event.getPaid() : eventDto.getPaid());
        event.setParticipantLimit(eventDto.getParticipantLimit() == null ? event.getParticipantLimit() : eventDto.getParticipantLimit());
        event.setRequestModeration(eventDto.getRequestModeration() == null ? event.getRequestModeration() : eventDto.getRequestModeration());
        event.setState(eventDto.getStateAction() == null ? event.getState() :
                eventDto.getStateAction() == EventAdminStateAction.PUBLISH_EVENT ? EventState.PUBLISHED : EventState.CANCELED);
        event.setTitle(eventDto.getTitle() == null ? event.getTitle() : eventDto.getTitle());
        event.setLocation(eventDto.getLocation() == null ? event.getLocation() : eventDto.getLocation());

        return EventMapper.toEventDto(eventRepository.save(event));
    }

    @Override
    public EventDto updateByUser(Long userId, Long eventId, UpdateEventDto eventDto) {
        userService.findUserById(userId);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Событие может редактировать только его создатель");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Нельзя редактировать опубликованное событие");
        }

        LocalDateTime eventDate = eventDto.getEventDate() == null ? event.getEventDate() : eventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new DateValidationException("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.");
        }

        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(() -> new NotFoundException("Категория с id=" + eventDto.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        if (eventDto.getStateAction() == EventUserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }
        if (eventDto.getStateAction() == EventUserStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        event.setAnnotation(eventDto.getAnnotation() == null ? event.getAnnotation() : eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription() == null ? event.getDescription() : eventDto.getDescription());
        event.setEventDate(eventDate);
        event.setPaid(eventDto.getPaid() == null ? event.getPaid() : eventDto.getPaid());
        event.setParticipantLimit(eventDto.getParticipantLimit() == null ? event.getParticipantLimit() : eventDto.getParticipantLimit());
        event.setRequestModeration(eventDto.getRequestModeration() == null ? event.getRequestModeration() : eventDto.getRequestModeration());
        event.setTitle(eventDto.getTitle() == null ? event.getTitle() : eventDto.getTitle());
        event.setLocation(eventDto.getLocation() == null ? event.getLocation() : eventDto.getLocation());

        return EventMapper.toEventDto(eventRepository.save(event));
    }
}