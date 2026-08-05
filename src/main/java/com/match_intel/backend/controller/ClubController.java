package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.ClubAdminDto;
import com.match_intel.backend.dto.response.ClubCourtDto;
import com.match_intel.backend.dto.response.ClubDetailsDto;
import com.match_intel.backend.dto.response.ClubDto;
import com.match_intel.backend.entity.ReservationType;
import com.match_intel.backend.entity.SurfaceType;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/clubs")
@Tag(name = "Clubs", description = "Managing tennis clubs")
public class ClubController {

    @Autowired
    private ClubService clubService;

    @Operation(summary = "Registering a new club (creator becomes owner)")
    @PostMapping("/register")
    public ResponseEntity<ClubDto> registerClub(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String logoUrl,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String reservationType
    ) {
        ClubDto clubDto = clubService.registerClub(
                userDetails.getUsername(),
                name,
                address,
                email,
                phone,
                description,
                logoUrl,
                latitude,
                longitude,
                parseReservationType(reservationType)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(clubDto);
    }

    @Operation(summary = "Getting all clubs ordered by name")
    @GetMapping
    public ResponseEntity<List<ClubDto>> getAllClubs(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ClubDto> clubs = clubService.getAllClubs(userDetails.getUsername());
        return ResponseEntity.ok(clubs);
    }

    @Operation(summary = "Getting club details with courts and admins")
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubDetailsDto> getClubDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId
    ) {
        ClubDetailsDto details = clubService.getClubDetails(userDetails.getUsername(), parseUuid(clubId));
        return ResponseEntity.ok(details);
    }

    @Operation(summary = "Partially updating a club (admins only)")
    @PatchMapping("/{clubId}")
    public ResponseEntity<ClubDto> updateClub(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId,
            @RequestBody Map<String, String> fields
    ) {
        ClubDto clubDto = clubService.updateClub(userDetails.getUsername(), parseUuid(clubId), fields);
        return ResponseEntity.ok(clubDto);
    }

    @Operation(summary = "Getting club admins")
    @GetMapping("/{clubId}/admins")
    public ResponseEntity<List<ClubAdminDto>> getAdmins(
            @PathVariable String clubId
    ) {
        List<ClubAdminDto> admins = clubService.getAdmins(parseUuid(clubId));
        return ResponseEntity.ok(admins);
    }

    @Operation(summary = "Adding a club admin (owner only)")
    @PostMapping("/{clubId}/admins")
    public ResponseEntity<Void> addAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId,
            @RequestParam String username
    ) {
        clubService.addAdmin(userDetails.getUsername(), parseUuid(clubId), username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Removing a club admin (owner only)")
    @DeleteMapping("/{clubId}/admins")
    public ResponseEntity<Void> removeAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId,
            @RequestParam String username
    ) {
        clubService.removeAdmin(userDetails.getUsername(), parseUuid(clubId), username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Getting club courts")
    @GetMapping("/{clubId}/courts")
    public ResponseEntity<List<ClubCourtDto>> getCourts(
            @PathVariable String clubId
    ) {
        List<ClubCourtDto> courts = clubService.getCourts(parseUuid(clubId));
        return ResponseEntity.ok(courts);
    }

    @Operation(summary = "Adding a court to the club (admins only)")
    @PostMapping("/{clubId}/courts")
    public ResponseEntity<ClubCourtDto> addCourt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId,
            @RequestParam String name,
            @RequestParam String surfaceType,
            @RequestParam BigDecimal pricePerHour
    ) {
        SurfaceType surface;
        try {
            surface = SurfaceType.valueOf(surfaceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid surface type");
        }

        ClubCourtDto courtDto = clubService.addCourt(
                userDetails.getUsername(),
                parseUuid(clubId),
                name,
                surface,
                pricePerHour
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(courtDto);
    }

    @Operation(summary = "Removing a court from the club (admins only)")
    @DeleteMapping("/{clubId}/courts/{courtId}")
    public ResponseEntity<Void> removeCourt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId,
            @PathVariable String courtId
    ) {
        clubService.removeCourt(userDetails.getUsername(), parseUuid(clubId), parseUuid(courtId));
        return ResponseEntity.ok().build();
    }

    private static ReservationType parseReservationType(String reservationType) {
        if (reservationType == null || reservationType.isBlank()) {
            return null;
        }
        try {
            return ReservationType.valueOf(reservationType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid reservation type");
        }
    }

    private static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }
}
