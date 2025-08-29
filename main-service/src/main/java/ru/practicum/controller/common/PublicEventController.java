package ru.practicum.controller.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.SimpleDateTimeFormatter;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.model.dto.event.EventDto;
import ru.practicum.model.dto.event.EventSearchCommon;
import ru.practicum.model.dto.event.EventSearchOrder;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/events")
@Validated
public class PublicEventController {

    private final EventService service;
    private final StatsClient statsClient;

    @GetMapping
    public ResponseEntity<List<EventDto>> findAll(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size,
                                                  HttpServletRequest request) {

        sendStats(request);

        EventSearchCommon eventSearchCommon = EventSearchCommon.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(EventSearchOrder.valueOf(sort))
                .from(from)
                .size(size)
                .build();
        log.info("Получен запрос GET /events с параметрами {}", eventSearchCommon);
        return ResponseEntity.ok(service.searchCommon(eventSearchCommon));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> findById(@PathVariable long eventId, HttpServletRequest request) {
        sendStats(request);

        log.info("Получен запрос GET /events/{}", eventId);
        return ResponseEntity.ok(service.findById(eventId));
    }

    private void sendStats(HttpServletRequest request) {
        try {
            String appName = "main-service";
            statsClient.save(EndpointHitDto.builder()
                    .app(appName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке статистики: {}", e.getMessage());
        }
    }
}