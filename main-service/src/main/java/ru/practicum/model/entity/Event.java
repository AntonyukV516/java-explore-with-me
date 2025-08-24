package ru.practicum.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 120)
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    @Column(name = "annotation", nullable = false)
    private String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    @Column(name = "description")
    private String description;

    @NotNull
    @Future
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @NotNull
    @Embedded
    private Location location;

    @Column(name = "paid", nullable = false)
    private Boolean paid = false;

    @Column(name = "participant_limit")
    private Integer participantLimit = 0;

    @Column(name = "request_moderation")
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState state = EventState.PENDING;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "views")
    private Long views;

    @Column(name = "confirmed_requests")
    private Long confirmedRequests;

    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
    }
}