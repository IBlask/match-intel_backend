package com.match_intel.backend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClubReviewDto(
        UUID id,
        UUID clubId,
        String username,
        String firstName,
        String lastName,
        int rating,
        String comment,
        LocalDateTime createdAt
) {}
