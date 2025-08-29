package ru.practicum.model.dto.participationRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;
    private ParticipationRequestStatus status;
}
