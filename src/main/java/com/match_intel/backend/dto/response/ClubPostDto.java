package com.match_intel.backend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClubPostDto(
        UUID id,
        UUID clubId,
        String clubName,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        int numberOfLikes,
        int numberOfComments,
        boolean likedByUser
) {}
