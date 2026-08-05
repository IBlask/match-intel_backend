package com.match_intel.backend.dto.response;

import com.match_intel.backend.entity.MatchVisibility;

import java.util.UUID;

public record PostDto(
        UUID id,
        UserDto author,
        String content,
        String imageUrl,
        String createdAt,
        MatchVisibility visibility,
        int numberOfLikes,
        int numberOfComments,
        boolean likedByUser
) {}
