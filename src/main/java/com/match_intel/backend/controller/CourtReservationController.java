package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.CourtReservationDto;
import com.match_intel.backend.dto.response.OccupiedSlotDto;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.service.CourtReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clubs")
@Tag(name = "Court Reservations", description = "Managing court reservations")
public class CourtReservationController {

    @Autowired
    private CourtReservationService courtReservationService;

    @Operation(summary = "Creating a court reservation")
    @PostMapping("/{clubId}/reservations")
    public ResponseEntity<CourtReservationDto> createReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId,
            @RequestParam String courtId,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        CourtReservationDto reservation = courtReservationService.createReservation(
                userDetails.getUsername(),
                parseUuid(clubId, "Invalid club id"),
                parseUuid(courtId, "Invalid court id"),
                parseDate(date),
                parseTime(startTime),
                parseTime(endTime)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @Operation(summary = "Getting reservations of a club for a date")
    @GetMapping("/{clubId}/reservations")
    public ResponseEntity<List<CourtReservationDto>> getClubReservations(
            @PathVariable String clubId,
            @RequestParam String date
    ) {
        List<CourtReservationDto> reservations = courtReservationService.getClubReservations(
                parseUuid(clubId, "Invalid club id"),
                parseDate(date)
        );
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Getting occupied slots for a court on a date")
    @GetMapping("/{clubId}/reservations/occupied-slots")
    public ResponseEntity<List<OccupiedSlotDto>> getOccupiedSlots(
            @PathVariable String clubId,
            @RequestParam String courtId,
            @RequestParam String date
    ) {
        List<OccupiedSlotDto> slots = courtReservationService.getOccupiedSlots(
                parseUuid(clubId, "Invalid club id"),
                parseUuid(courtId, "Invalid court id"),
                parseDate(date)
        );
        return ResponseEntity.ok(slots);
    }

    @Operation(summary = "Confirming a pending reservation (club admin only)")
    @PostMapping("/reservations/confirm")
    public ResponseEntity<Void> confirmReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String reservationId
    ) {
        courtReservationService.confirmReservation(
                userDetails.getUsername(),
                parseUuid(reservationId, "Invalid reservation id")
        );
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cancelling a reservation (creator or club admin)")
    @PostMapping("/reservations/cancel")
    public ResponseEntity<Void> cancelReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String reservationId
    ) {
        courtReservationService.cancelReservation(
                userDetails.getUsername(),
                parseUuid(reservationId, "Invalid reservation id")
        );
        return ResponseEntity.ok().build();
    }

    private static UUID parseUuid(String value, String errorMessage) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid date format. Use yyyy-MM-dd");
        }
    }

    private static LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid time format. Use HH:mm");
        }
    }
}
