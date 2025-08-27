package ru.practicum.service;

import ru.practicum.model.dto.ParticipationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.model.dto.ParticipationRequest.EventRequestStatusUpdateResult;
import ru.practicum.model.dto.ParticipationRequest.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getAllByUser(Long userId);

    List<ParticipationRequestDto> getAllByEventAndInitiator(Long userId, Long eventId);

    ParticipationRequestDto create(Long userId, Long eventId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest requestDto);
}
