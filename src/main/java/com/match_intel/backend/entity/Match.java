package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.UUID;



@Entity
@Table(name = "matches")
@Getter
public class Match {
    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "player1_id", referencedColumnName = "id")
    @Setter
    private User player1;

    @ManyToOne
    @JoinColumn(name = "player2_id", referencedColumnName = "id")
    @Setter
    private User player2;

    @ManyToOne
    @JoinColumn(name = "referee_id", referencedColumnName = "id")
    @Setter
    private User referee;

    @ManyToOne
    @JoinColumn(name = "club_id", referencedColumnName = "id")
    @Setter
    private Club club;

    @Column(nullable = false)
    @Setter
    private String initialServer;

    @Column(nullable = false)
    @Setter
    private String startDate;

    @Column(nullable = false)
    @Setter
    private String startTime;

    @Column(nullable = false)
    @Setter
    private boolean isFinished = false;

    @Column
    @Setter
    private String finalScore = "0 : 0";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private MatchVisibility visibility = MatchVisibility.PRIVATE;

    @Column
    @Setter
    private Integer player1Efficiency;

    @Column
    @Setter
    private Integer player2Efficiency;

    @Column(nullable = false)
    @Setter
    private int numberOfLikes = 0;

    @Column(nullable = false)
    @Setter
    private int numberOfComments = 0;

    @Column
    @Setter
    private String set1Score = null;

    @Column
    @Setter
    private String set2Score = null;

    @Column
    @Setter
    private String set3Score = null;

    @Transient
    @Setter
    private MatchStats matchStats;

    @Transient
    @Setter
    private boolean likedByUser;

    @ManyToOne
    @JoinColumn(name = "tournament_id", referencedColumnName = "id")
    @Setter
    private Tournament tournament;

    @Column
    @Setter
    private Integer round;

    @Column(name = "bracket_position")
    @Setter
    private Integer bracketPosition;

    @Column(name = "is_bye")
    @Setter
    private boolean isBye = false;

    @Column
    @Setter
    private Integer livePlayer1Games;

    @Column
    @Setter
    private Integer livePlayer2Games;

    @Column
    @Setter
    private String livePlayer1Points;

    @Column
    @Setter
    private String livePlayer2Points;
}
