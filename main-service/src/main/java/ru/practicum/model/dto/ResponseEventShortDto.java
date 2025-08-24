package ru.practicum.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseEventShortDto {
    private Long id;
    private String annotation;
    private ResponseCategoryDto category;
    private Long confirmedRequests;
    private LocalDateTime eventDate;
    private ResponseUserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
