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
    private String player1Points;

    @Column(nullable = false)
    private String player2Points;

    @Column(nullable = false)
    private String playerWhoScored;

    @Column(nullable = false)
    private boolean forced;

    @Column(name = "is_first_serve")
    private Boolean isFirstServe;


    public void setIsFirstServe(Boolean isFirstServe) {
        this.isFirstServe = isFirstServe;
    }


    public Point(UUID matchId, int scoringPlayerNumber, int playerToServe, boolean forced, String playerWhoScored) {
        this.matchId = matchId;
        this.playerToServe = playerToServe;
        this.createdAt = LocalDateTime.now();
        this.forced = forced;
        this.playerWhoScored = playerWhoScored;

        this.player1Sets = 0;
        this.player2Sets = 0;
        this.player1Games = 0;
        this.player2Games = 0;

        if (scoringPlayerNumber == 1) {
            this.player1Points = "15";
            this.player2Points = "0";
        }
        else {
            this.player1Points = "0";
            this.player2Points = "15";
        }
    }

    public Point(Point parentPoint, int scoringPlayerNumber, boolean forced, String playerWhoScored) {
        this.matchId = parentPoint.getMatchId();
        this.parentPoint = parentPoint.getId();
        this.createdAt = LocalDateTime.now();
        this.playerToServe = parentPoint.playerToServe;
        this.forced = forced;
        this.playerWhoScored = playerWhoScored;

        this.player1Sets = parentPoint.player1Sets;
        this.player2Sets = parentPoint.player2Sets;
        this.player1Games = parentPoint.player1Games;
        this.player2Games = parentPoint.player2Games;
        this.player1Points = parentPoint.getPlayer1Points();
        this.player2Points = parentPoint.getPlayer2Points();

        // If not tie-break
        if (!(this.player1Games == 6 && this.player2Games == 6)) {
            // Add point to the scoring player
            if (scoringPlayerNumber == 1) {
                switch (this.player1Points) {
                    case "0" -> this.player1Points = "15";
                    case "15" -> this.player1Points = "30";
                    case "30" -> this.player1Points = "40";
                    case "40" -> this.player1Points = "Ad";
                    case "Ad" -> this.player1Points = "GAME";
                }
            }
            else {
                switch (this.player2Points) {
                    case "0" -> this.player2Points = "15";
                    case "15" -> this.player2Points = "30";
                    case "30" -> this.player2Points = "40";
                    case "40" -> this.player2Points = "Ad";
                    case "Ad" -> this.player2Points = "GAME";
                }
            }

            // check if Game is won
            if (this.player1Points.equals("Ad") && this.player2Points.equals("Ad")) {
                this.player1Points = "40";
                this.player2Points = "40";
            }
            else if (this.player1Points.equals("GAME")
                    || (this.player1Points.equals("Ad") && !this.player2Points.equals("40"))) {
                this.player1Games++;
                this.resetPoints();
                this.changePlayerToServe();
            }
            else if (this.player2Points.equals("GAME")
                    || (this.player2Points.equals("Ad") && !this.player1Points.equals("40"))) {
                this.player2Games++;
                this.resetPoints();
                this.changePlayerToServe();
            }

            // check if Set is won
            if (this.player1Games >= 6 && (this.player2Games + 2) <= this.player1Games) {
                this.player1Sets++;
                this.resetGames();
            }
            else if (this.player2Games >= 6 && (this.player1Games + 2) <= this.player2Games) {
                this.player2Sets++;
                this.resetGames();
            }
        }

        // If tie-break
        else {
            // Add point to the scoring player
            if (scoringPlayerNumber == 1) {
                this.player1Points = String.valueOf(Integer.parseInt(this.player1Points) + 1);
            }
            else {
                this.player2Points = String.valueOf(Integer.parseInt(this.player2Points) + 1);
            }

            // Check if Set is won
            if (Integer.parseInt(this.player1Points) >= 7
                    && (Integer.parseInt(this.player2Points) + 2) <= Integer.parseInt(this.player1Points)) {
                this.player1Sets++;
                this.resetPoints();
                this.resetGames();
            }
            else if (Integer.parseInt(this.player2Points) >= 7
                    && (Integer.parseInt(this.player1Points) + 2) <= Integer.parseInt(this.player2Points)) {
                this.player2Sets++;
                this.resetPoints();
                this.resetGames();
            }

            // Change player to serve on every odd point
            if ((this.player1Points.equals("0") && this.player2Points.equals("0"))
                    || (Integer.parseInt(this.player1Points) + Integer.parseInt(this.player2Points)) % 2 != 0) {
                this.changePlayerToServe();
            }
        }
    }


    private void resetPoints() {
        this.player1Points = "0";
        this.player2Points = "0";
    }

    private void resetGames() {
        this.player1Games = 0;
        this.player2Games = 0;
    }

    private void changePlayerToServe() {
        this.playerToServe = this.playerToServe == 1 ? 2 : 1;
    }
}
