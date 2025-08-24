package ru.practicum.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}

