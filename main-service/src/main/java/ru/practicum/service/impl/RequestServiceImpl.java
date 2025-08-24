package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.EventState;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.dto.ParticipationRequestDto;
import ru.practicum.model.entity.Event;
import ru.practicum.model.entity.ParticipationRequest;
import ru.practicum.model.entity.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        checkUserExists(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        checkRequestNotDuplicate(userId, eventId);
        checkUserNotInitiator(userId, event.getInitiator().getId());
        checkEventPublished(event);
        checkParticipantLimitNotReached(event);

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(event.getRequestModeration() && event.getParticipantLimit() > 0 ?
                        RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        ParticipationRequest savedRequest = requestRepository.save(request);
        return requestMapper.toDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUserExists(userId);
        ParticipationRequest request = getRequestById(requestId);

        checkUserOwnsRequest(userId, request.getRequester().getId());

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updatedRequest = requestRepository.save(request);
        return requestMapper.toDto(updatedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        checkUserExists(userId);
        Event event = getEventById(eventId);
        checkUserIsInitiator(userId, event.getInitiator().getId());

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        checkUserExists(userId);
        Event event = getEventById(eventId);
        checkUserIsInitiator(userId, event.getInitiator().getId());

        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(updateRequest.getRequestIds());
        checkRequestsPending(requests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(new ArrayList<>());
        result.setRejectedRequests(new ArrayList<>());

        if ("CONFIRMED".equals(updateRequest.getStatus())) {
            confirmRequests(event, requests, result);
        } else if ("REJECTED".equals(updateRequest.getStatus())) {
            rejectRequests(requests, result);
        }

        return result;
    }

    private void confirmRequests(Event event, List<ParticipationRequest> requests,
                                 EventRequestStatusUpdateResult result) {
        long confirmedCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        int availableSlots = event.getParticipantLimit() - (int) confirmedCount;

        if (availableSlots <= 0) {
            throw new ConflictException("The participant limit has been reached");
        }

        for (ParticipationRequest request : requests) {
            if (availableSlots > 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                result.getConfirmedRequests().add(requestMapper.toDto(request));
                availableSlots--;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(requestMapper.toDto(request));
            }
        }

        requestRepository.saveAll(requests);

        // Если лимит исчерпан, отклоняем все оставшиеся заявки
        if (availableSlots == 0) {
            rejectPendingRequests(event.getId());
        }
    }

    private void rejectRequests(List<ParticipationRequest> requests, EventRequestStatusUpdateResult result) {
        for (ParticipationRequest request : requests) {
            request.setStatus(RequestStatus.REJECTED);
            result.getRejectedRequests().add(requestMapper.toDto(request));
        }
        requestRepository.saveAll(requests);
    }

    private void rejectPendingRequests(Long eventId) {
        List<ParticipationRequest> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
        for (ParticipationRequest request : pendingRequests) {
            request.setStatus(RequestStatus.REJECTED);
        }
        requestRepository.saveAll(pendingRequests);
    }

    private void checkRequestNotDuplicate(Long userId, Long eventId) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exists");
        }
    }

    private void checkUserNotInitiator(Long userId, Long initiatorId) {
        if (userId.equals(initiatorId)) {
            throw new ConflictException("Initiator cannot participate in their own event");
        }
    }

    private void checkEventPublished(Event event) {
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }
    }

    private void checkParticipantLimitNotReached(Event event) {
        if (event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }
    }

    private void checkUserOwnsRequest(Long userId, Long requesterId) {
        if (!userId.equals(requesterId)) {
            throw new NotFoundException("Request not found");
        }
    }

    private void checkUserIsInitiator(Long userId, Long initiatorId) {
        if (!userId.equals(initiatorId)) {
            throw new NotFoundException("User is not event initiator");
        }
    }

    private void checkRequestsPending(List<ParticipationRequest> requests) {
        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private ParticipationRequest getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }
}
