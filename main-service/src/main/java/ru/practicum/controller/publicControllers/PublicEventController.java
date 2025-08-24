package ru.practicum.controller.publicControllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.model.dto.ResponseEventFullDto;
import ru.practicum.model.dto.ResponseEventShortDto;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final StatsClient statsClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public List<ResponseEventShortDto> getEvents(@RequestParam(required = false) String text,
                                                 @RequestParam(required = false) List<Long> categories,
                                                 @RequestParam(required = false) Boolean paid,
                                                 @RequestParam(required = false) String rangeStart,
                                                 @RequestParam(required = false) String rangeEnd,
                                                 @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                 @RequestParam(required = false) String sort,
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 HttpServletRequest request) {

        saveHit(request.getRequestURI(), request.getRemoteAddr());
        return eventService.getEventsPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        saveHit(request.getRequestURI(), request.getRemoteAddr());
        return eventService.getEventPublic(id);
    }

    private void saveHit(String uri, String ip) {
        try {
            statsClient.save(EndpointHitDto.builder()
                    .app("explore-with-me-service")
                    .uri(uri)
                    .ip(ip)
                    .timestamp(LocalDateTime.now().format(formatter))
                    .build());
        } catch (Exception e) {
            System.err.println("Failed to save hit statistics: " + e.getMessage());
        }
    }
}
