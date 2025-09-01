package ru.practicum.model.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Location {
    @Positive(message = "Широта должна быть положительной")
    @NotNull(message = "Широта не может быть пустой")
    private Double lat;

    @Positive(message = "Долгота должна быть положительной")
    @NotNull(message = "Долгота не может быть пустой")
    private Double lon;
}
