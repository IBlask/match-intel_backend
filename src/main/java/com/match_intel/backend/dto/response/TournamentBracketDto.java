package com.match_intel.backend.dto.response;

import java.util.List;
import java.util.UUID;

public record TournamentBracketDto(
        UUID tournamentId,
        String status,
        int totalRounds,
        List<TournamentRoundDto> rounds
) {}