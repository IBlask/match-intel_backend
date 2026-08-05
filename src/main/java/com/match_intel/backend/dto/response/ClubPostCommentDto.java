package com.match_intel.backend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClubPostCommentDto(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String comment,
        LocalDateTime createdAt
) {}
