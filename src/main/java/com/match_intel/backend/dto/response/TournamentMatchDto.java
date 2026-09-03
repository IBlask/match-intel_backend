package com.match_intel.backend.dto.response;

import java.util.UUID;

public record TournamentMatchDto(
        UUID matchId,
        int bracketPosition,
        String player1Name,
        String player1Username,
        String player2Name,
        String player2Username,
        String score,
        boolean isFinished,
        boolean isBye,
        boolean player1Winner,
        boolean player2Winner,
        Integer player1Games,
        Integer player2Games,
        String player1Points,
        String player2Points
) {}