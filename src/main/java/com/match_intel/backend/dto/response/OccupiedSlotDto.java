package com.match_intel.backend.dto.response;

import java.time.LocalTime;

public record OccupiedSlotDto(
        LocalTime startTime,
        LocalTime endTime
) {
}
