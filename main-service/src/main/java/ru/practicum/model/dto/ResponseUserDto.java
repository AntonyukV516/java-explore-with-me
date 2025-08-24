package ru.practicum.model.dto;

import lombok.Data;

@Data
public class ResponseUserDto {
    private Long id;
    private String name;
    private String email;
}
