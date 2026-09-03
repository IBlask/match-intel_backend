package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
public class Tournament {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "club_id", referencedColumnName = "id")
    private Club club;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
    private User createdBy;

    @Column(name = "start_date", nullable = false)
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.OPEN;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers;

    @Column(name = "registration_deadline", nullable = false)
    private String registrationDeadline;

    @Column(name = "number_of_players", nullable = false)
    private int numberOfPlayers = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
