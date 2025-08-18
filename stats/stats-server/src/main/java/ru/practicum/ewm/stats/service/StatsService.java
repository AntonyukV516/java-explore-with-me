package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.ewm.stats.model.EndpointHitMapper;
import ru.practicum.ewm.stats.model.Stats;
import ru.practicum.ewm.stats.model.StatsMapper;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    @Autowired
    private final StatsRepository statsRepository;

    public EndpointHitDto hit(EndpointHitDto endpointHitDto) {
        return EndpointHitMapper.toEndpointHitDto(
                statsRepository.save(EndpointHitMapper.toEndpointHit(endpointHitDto))
        );
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        validateDates(start, end);
        return unique.equals(Boolean.TRUE)
                ? getUniqueStats(start, end, uris)
                : getStats(start, end, uris);
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            throw new IllegalArgumentException("Дата начала не может быть пустой");
        }

        if (end == null) {
            throw new IllegalArgumentException("Дата конца не может быть пустой");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты конца");
        }
    }

    private List<StatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return statsMapping(statsRepository.findUniqueStats(start, end, uris));
    }

    private List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return statsMapping(statsRepository.findStats(start, end, uris));
    }

    private List<StatsDto> statsMapping(List<Stats> stats) {
        return stats.stream()
                .map(StatsMapper::toStatsDto)
                .toList();
    }
}
