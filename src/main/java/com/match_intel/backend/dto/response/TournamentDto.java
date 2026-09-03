package com.match_intel.backend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TournamentDto(
        UUID id,
        String name,
        UUID clubId,
        String clubName,
        String createdByUsername,
        String startDate,
        String registrationDeadline,
        String status,
        int maxPlayers,
        int numberOfPlayers,
        boolean isRegistered,
        LocalDateTime createdAt
) {}
