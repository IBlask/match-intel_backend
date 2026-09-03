package com.match_intel.backend.dto.response;

import java.util.UUID;

public record TournamentStatusDto(
        UUID tournamentId,
        String status,
        int totalRounds,
        int currentRound,
        int finishedMatches,
        int totalMatches
) {}