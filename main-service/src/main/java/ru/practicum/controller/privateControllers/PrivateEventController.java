package ru.practicum.controller.privateControllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.RequestEventDto;
import ru.practicum.model.dto.ResponseEventFullDto;
import ru.practicum.model.dto.ResponseEventShortDto;
import ru.practicum.model.dto.UpdateEventUserRequest;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<ResponseEventShortDto> getEvents(@PathVariable Long userId,
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getEventsByUser(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEventFullDto addEvent(@PathVariable Long userId,
                                         @Valid @RequestBody RequestEventDto requestEventDto) {
        return eventService.addEvent(userId, requestEventDto);
    }

    @GetMapping("/{eventId}")
    public ResponseEventFullDto getEvent(@PathVariable Long userId,
                                         @PathVariable Long eventId) {
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public ResponseEventFullDto updateEvent(@PathVariable Long userId,
                                            @PathVariable Long eventId,
                                            @RequestBody UpdateEventUserRequest updateRequest) {
        return eventService.updateEventByUser(userId, eventId, updateRequest);
    }
}