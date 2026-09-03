package com.match_intel.backend.dto.response;

import java.util.UUID;

public record TournamentRefereeDto(
        UUID id,
        String username,
        String firstName,
        String lastName
) {}