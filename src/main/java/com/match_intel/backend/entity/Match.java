package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
public class Match {
    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player1_id", referencedColumnName = "id")
    @Setter
    private User player1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player2_id", referencedColumnName = "id")
    @Setter
    private User player2;

    @Column(nullable = false)
    @Setter
    private String initialServer;

    @Column(nullable = false)
    @Setter
    private LocalDateTime startTime;

    @Column(nullable = false)
    @Setter
    private boolean isFinished = false;

    @Column
    @Setter
    private String finalScore = null;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private MatchVisibility visibility = MatchVisibility.PRIVATE;
}
