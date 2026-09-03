package com.match_intel.backend.dto.response;

import java.util.List;

public record TournamentRoundDto(
        int round,
        String roundName,
        List<TournamentMatchDto> matches
) {}