package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EventState;
import ru.practicum.model.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    List<Event> findAllByCategoryId(Long categoryId);

    @Query("SELECT e FROM Event e WHERE " +
            "(:users IS NULL OR e.initiator.id IN :users) AND " +
            "(:states IS NULL OR e.state IN :states) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:start IS NULL OR e.eventDate >= :start) AND " +
            "(:end IS NULL OR e.eventDate <= :end)")
    List<Event> findEventsByAdminFilters(@Param("users") List<Long> users,
                                         @Param("states") List<EventState> states,
                                         @Param("categories") List<Long> categories,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.state = 'PUBLISHED' AND " +
            "(LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:paid IS NULL OR e.paid = :paid) AND " +
            "e.eventDate >= :start AND " +
            "(:end IS NULL OR e.eventDate <= :end) AND " +
            "(:onlyAvailable IS FALSE OR e.participantLimit = 0 OR " +
            "e.participantLimit > (SELECT COUNT(r) FROM ParticipationRequest r" +
            " WHERE r.event = e AND r.status = 'CONFIRMED'))")
    List<Event> findEventsPublic(@Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("onlyAvailable") Boolean onlyAvailable,
                                 Pageable pageable);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.category.id = :categoryId")
    boolean existsByCategoryId(@Param("categoryId") Long categoryId);
}
