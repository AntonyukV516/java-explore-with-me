package ru.practicum.model.dto;

import lombok.Data;
import ru.practicum.model.EventState;
import ru.practicum.model.entity.Location;

import java.time.LocalDateTime;

@Data
public class ResponseEventFullDto {
    private Long id;
    private String annotation;
    private ResponseCategoryDto category;
    private Long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
    private ResponseUserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventState state;
    private String title;
    private Long views;
}
