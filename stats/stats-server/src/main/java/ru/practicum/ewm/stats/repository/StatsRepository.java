package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("""
        SELECT new ru.practicum.ewm.stats.model.Stats(h.app, h.uri, COUNT(h.ip))
        FROM EndpointHit as h
        WHERE h.timestamp BETWEEN :start AND :end
        AND (:uris IS NULL OR h.uri IN :uris)
        GROUP BY h.app, h.uri
        ORDER BY COUNT(h.ip) DESC
        """)
    List<Stats> findStats(@Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end,
                          @Param("uris") List<String> uris);

    @Query("""
        SELECT new ru.practicum.ewm.stats.model.Stats(h.app, h.uri, COUNT(DISTINCT h.ip))
        FROM EndpointHit as h
        WHERE h.timestamp BETWEEN :start AND :end
        AND (:uris IS NULL OR h.uri IN :uris)
        GROUP BY h.app, h.uri
        ORDER BY COUNT(DISTINCT h.ip) DESC
        """)
    List<Stats> findUniqueStats(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("uris") List<String> uris);
}
