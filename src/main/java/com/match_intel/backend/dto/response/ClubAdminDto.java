package com.match_intel.backend.dto.response;

import com.match_intel.backend.entity.ClubRole;

import java.util.UUID;

public record ClubAdminDto(
        UUID id,
        String username,
        String firstName,
        String lastName,
        ClubRole role
) {}
