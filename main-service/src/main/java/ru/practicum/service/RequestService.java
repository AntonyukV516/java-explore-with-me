package ru.practicum.service;

import ru.practicum.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);
    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);
}
