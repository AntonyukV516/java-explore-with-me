package ru.practicum.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResponseCompilationDto {
    private Long id;
    private List<ResponseEventShortDto> events;
    private Boolean pinned;
    private String title;
}
