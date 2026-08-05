package com.match_intel.backend.dto.response;

import com.match_intel.backend.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CourtReservationDto(
        UUID id,
        UUID clubId,
        String clubName,
        UUID courtId,
        String courtName,
        String username,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus status
) {
}
