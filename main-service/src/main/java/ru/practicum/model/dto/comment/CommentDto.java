package ru.practicum.model.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.model.dto.event.EventDto;
import ru.practicum.model.dto.user.UserDto;

import java.time.LocalDateTime;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private UserDto user;

    private EventDto event;

    private String message;

    private LocalDateTime created;
}