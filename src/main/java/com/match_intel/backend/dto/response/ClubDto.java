package com.match_intel.backend.dto.response;

import com.match_intel.backend.entity.ReservationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClubDto(
        UUID id,
        String name,
        String address,
        String email,
        String phone,
        String description,
        String logoUrl,
        Double latitude,
        Double longitude,
        ReservationType reservationType,
        Double averageRating,
        int numberOfReviews,
        int followersCount,
        boolean isFollowing,
        boolean isAdmin,
        LocalDateTime createdAt
) {}
