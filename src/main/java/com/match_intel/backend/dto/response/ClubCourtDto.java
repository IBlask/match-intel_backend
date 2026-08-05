package com.match_intel.backend.dto.response;

import com.match_intel.backend.entity.SurfaceType;

import java.math.BigDecimal;
import java.util.UUID;

public record ClubCourtDto(
        UUID id,
        String name,
        SurfaceType surfaceType,
        BigDecimal pricePerHour
) {}
