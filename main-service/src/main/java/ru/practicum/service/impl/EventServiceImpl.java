package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.EventState;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.dto.*;
import ru.practicum.model.entity.Category;
import ru.practicum.model.entity.Event;
import ru.practicum.model.entity.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public List<ResponseEventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        checkUserExists(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest);
        Map<Long, Long> views = getViewsForEvents(events);

        return events.stream()
                .map(event -> enrichWithStats(event, views.getOrDefault(event.getId(), 0L)))
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEventFullDto addEvent(Long userId, RequestEventDto requestEventDto) {
        User user = getUserById(userId);
        Category category = getCategoryById(requestEventDto.getCategory());

        checkEventDateValid(requestEventDto.getEventDate());

        Event event = eventMapper.toEntity(requestEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);

        Event savedEvent = eventRepository.save(event);

        saveHit("/events/" + savedEvent.getId(), "0.0.0.0");

        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEventFullDto getEventByUser(Long userId, Long eventId) {
        checkUserExists(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Long views = getViewsForEvent(eventId);
        Event enrichedEvent = enrichWithStats(event, views);

        return eventMapper.toFullDto(enrichedEvent);
    }

    @Override
    public ResponseEventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        checkUserExists(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        checkEventStateForUserUpdate(event);

        if (updateRequest.getEventDate() != null) {
            checkEventDateValid(updateRequest.getEventDate());
        }

        if (updateRequest.getCategory() != null) {
            Category category = getCategoryById(updateRequest.getCategory());
            event.setCategory(category);
        }

        eventMapper.updateEntityFromUserRequest(updateRequest, event);

        if ("SEND_TO_REVIEW".equals(updateRequest.getStateAction())) {
            event.setState(EventState.PENDING);
        } else if ("CANCEL_REVIEW".equals(updateRequest.getStateAction())) {
            event.setState(EventState.CANCELED);
        }

        Event updatedEvent = eventRepository.save(event);
        Long views = getViewsForEvent(eventId);
        Event enrichedEvent = enrichWithStats(updatedEvent, views);

        return eventMapper.toFullDto(enrichedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseEventFullDto> getEventsByAdmin(List<Long> users, List<String> states,
                                                       List<Long> categories, String rangeStart,
                                                       String rangeEnd, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        LocalDateTime start = parseDateTime(rangeStart);
        LocalDateTime end = parseDateTime(rangeEnd);

        List<EventState> eventStates = null;
        if (states != null) {
            eventStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        List<Event> events = eventRepository.findEventsByAdminFilters(users, eventStates, categories, start, end, pageRequest);
        Map<Long, Long> views = getViewsForEvents(events);

        return events.stream()
                .map(event -> {
                    ResponseEventFullDto dto = eventMapper.toFullDto(event);
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    dto.setConfirmedRequests(requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = getEventById(eventId);

        checkEventStateForAdminUpdate(event, updateRequest);

        if (updateRequest.getEventDate() != null) {
            checkEventDateForPublish(event, updateRequest.getEventDate());
        }

        if (updateRequest.getCategory() != null) {
            Category category = getCategoryById(updateRequest.getCategory());
            event.setCategory(category);
        }

        eventMapper.updateEntityFromAdminRequest(updateRequest, event);

        if ("PUBLISH_EVENT".equals(updateRequest.getStateAction())) {
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());

            saveHit("/events/" + eventId, "0.0.0.0");
        } else if ("REJECT_EVENT".equals(updateRequest.getStateAction())) {
            event.setState(EventState.CANCELED);
        }

        Event updatedEvent = eventRepository.save(event);
        Long views = getViewsForEvent(eventId);
        Event enrichedEvent = enrichWithStats(updatedEvent, views);

        return eventMapper.toFullDto(enrichedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseEventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                                       String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                                       String sort, Integer from, Integer size) {
        PageRequest pageRequest = createPageRequest(sort, from, size);
        LocalDateTime start = parseDateTime(rangeStart, LocalDateTime.now());
        LocalDateTime end = parseDateTime(rangeEnd);

        List<Event> events = eventRepository.findEventsPublic(text, categories, paid, start,
                end, onlyAvailable, pageRequest);
        Map<Long, Long> views = getViewsForEvents(events);

        return events.stream()
                .map(event -> enrichWithStats(event, views.getOrDefault(event.getId(), 0L)))
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEventFullDto getEventPublic(Long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        saveHit("/events/" + id, "0.0.0.0");

        Long views = getViewsForEvent(id);
        Long confirmedRequests = requestRepository
                .countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        event.setViews(views);
        event.setConfirmedRequests(confirmedRequests);

        eventRepository.save(event);

        return eventMapper.toFullDto(event);
    }

    private Event enrichWithStats(Event event, Long views) {
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        event.setConfirmedRequests(confirmedRequests);
        event.setViews(views);
        return event;
    }

    private Long getViewsForEvent(Long eventId) {
        List<StatsDto> stats = statsClient.getStats(
                LocalDateTime.of(1900, 1, 1, 0, 0).format(formatter),
                LocalDateTime.now().plusYears(100).format(formatter),
                List.of("/events/" + eventId),
                false
        );
        return stats.isEmpty() ? 0L : stats.getFirst().getHits();
    }

    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        List<StatsDto> stats = statsClient.getStats(
                LocalDateTime.of(1900, 1, 1, 0, 0).format(formatter),
                LocalDateTime.now().plusYears(100).format(formatter),
                uris,
                false
        );

        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> Long.parseLong(stat.getUri().split("/")[2]),
                        StatsDto::getHits
                ));
    }

    private void saveHit(String uri, String ip) {
        try {
            statsClient.save(EndpointHitDto.builder()
                    .app("explore-with-me-service")
                    .uri(uri)
                    .ip(ip)
                    .timestamp(LocalDateTime.now().format(formatter))
                    .build());
        } catch (Exception e) {
            System.err.println("Failed to save hit statistics: " + e.getMessage());
        }
    }

    private void checkEventDateValid(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }
    }

    private void checkEventStateForUserUpdate(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
    }

    private void checkEventStateForAdminUpdate(Event event, UpdateEventAdminRequest updateRequest) {
        if ("PUBLISH_EVENT".equals(updateRequest.getStateAction()) &&
                event.getState() != EventState.PENDING) {
            throw new ConflictException("Cannot publish the event because it's not in the right state: PENDING");
        }

        if ("REJECT_EVENT".equals(updateRequest.getStateAction()) &&
                event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot reject the event because it's already published");
        }
    }

    private void checkEventDateForPublish(Event event, LocalDateTime newEventDate) {
        if (event.getPublishedOn() != null &&
                newEventDate.isBefore(event.getPublishedOn().plusHours(1))) {
            throw new ConflictException("Event date must be at least 1 hour after publication");
        }
    }

    private PageRequest createPageRequest(String sort, Integer from, Integer size) {
        Sort sorting = Sort.unsorted();
        if ("EVENT_DATE".equals(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "eventDate");
        } else if ("VIEWS".equals(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "views");
        }
        return PageRequest.of(from / size, size, sorting);
    }

    private LocalDateTime parseDateTime(String dateTime) {
        return dateTime != null ? LocalDateTime.parse(dateTime, formatter) : null;
    }

    private LocalDateTime parseDateTime(String dateTime, LocalDateTime defaultValue) {
        return dateTime != null ? LocalDateTime.parse(dateTime, formatter) : defaultValue;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }
}
