package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Point {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @Column(nullable = false)
    private UUID matchId;

    @Column
    private UUID parentPoint;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int playerToServe;

    @Column(nullable = false)
    private int player1Sets;

    @Column(nullable = false)
    private int player2Sets;

    @Column(nullable = false)
    private int player1Games;

    @Column(nullable = false)
    private int player2Games;

    @Column(nullable = false)
    private int player1Points;

    @Column(nullable = false)
    private int player2Points;


    public Point(UUID matchId, int scoringPlayerNumber, int playerToServe) {
        this.matchId = matchId;
        this.playerToServe = playerToServe;
        this.createdAt = LocalDateTime.now();

        this.player1Sets = 0;
        this.player2Sets = 0;
        this.player1Games = 0;
        this.player2Games = 0;

        if (scoringPlayerNumber == 1) {
            this.player1Points = 1;
            this.player2Points = 0;
        }
        else {
            this.player1Points = 0;
            this.player2Points = 1;
        }
    }

    public Point(Point parentPoint, int scoringPlayerNumber) {
        this.matchId = parentPoint.getMatchId();
        this.parentPoint = parentPoint.getId();
        this.createdAt = LocalDateTime.now();
        this.playerToServe = parentPoint.playerToServe;

        this.player1Sets = parentPoint.player1Sets;
        this.player2Sets = parentPoint.player2Sets;
        this.player1Games = parentPoint.player1Games;
        this.player2Games = parentPoint.player2Games;
        this.player1Points = parentPoint.getPlayer1Points();
        this.player2Points = parentPoint.getPlayer2Points();

        if (scoringPlayerNumber == 1) {
            this.player1Points++;
        }
        else {
            this.player2Points++;
        }

        // if not tie-break
        if (!(this.player1Games == 6 && this.player2Games == 6)) {
            if (this.player1Points >= 4 && (this.player2Points + 2) <= this.player1Points) {
                this.player1Games++;
                this.resetPoints();
                this.changePlayerToServe();
            }
            else if (this.player2Points >= 4 && (this.player1Points + 2) <= this.player2Points) {
                this.player2Games++;
                this.resetPoints();
                this.changePlayerToServe();
            }

            if (this.player1Games >= 6 && (this.player2Games + 2) <= this.player1Games) {
                this.player1Sets++;
                this.resetGames();
            }
            else if (this.player2Games >= 6 && (this.player1Games + 2) <= this.player2Games) {
                this.player2Sets++;
                this.resetGames();
            }
        }
        // if tie-break
        else {
            if (this.player1Points >= 7 && (this.player2Points + 2) <= this.player1Points) {
                this.player1Sets++;
                this.resetPoints();
                this.resetGames();
            }
            else if (this.player2Points >= 7 && (this.player1Points + 2) <= this.player2Points) {
                this.player2Sets++;
                this.resetPoints();
                this.resetGames();
            }

            if ((this.player1Points == 0 && this.player2Points == 0)
                    || (this.player1Points + this.player2Points) % 2 != 0
            ) {
                this.changePlayerToServe();
            }
        }
    }


    private void resetPoints() {
        this.player1Points = 0;
        this.player2Points = 0;
    }

    private void resetGames() {
        this.player1Games = 0;
        this.player2Games = 0;
    }

    private void changePlayerToServe() {
        this.playerToServe = this.playerToServe == 1 ? 2 : 1;
    }
}
