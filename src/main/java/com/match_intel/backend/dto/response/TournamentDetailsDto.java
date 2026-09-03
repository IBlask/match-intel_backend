package com.match_intel.backend.dto.response;

import java.util.List;

public record TournamentDetailsDto(
        TournamentDto tournament,
        List<TournamentRegistrationDto> registrations
) {}
