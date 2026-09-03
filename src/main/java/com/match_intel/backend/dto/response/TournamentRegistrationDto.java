package com.match_intel.backend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TournamentRegistrationDto(
        UUID id,
        String username,
        String firstName,
        String lastName,
        int seed,
        LocalDateTime registeredAt
) {}
