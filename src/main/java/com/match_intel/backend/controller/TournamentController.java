package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.TournamentBracketDto;
import com.match_intel.backend.dto.response.TournamentDetailsDto;
import com.match_intel.backend.dto.response.TournamentDto;
import com.match_intel.backend.dto.response.TournamentRefereeDto;
import com.match_intel.backend.dto.response.TournamentRegistrationDto;
import com.match_intel.backend.dto.response.TournamentStatusDto;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tournaments")
@Tag(name = "Tournaments", description = "Managing tennis tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Operation(summary = "Create a new tournament (club admin only)")
    @PostMapping("/create")
    public ResponseEntity<TournamentDto> createTournament(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam UUID clubId,
            @RequestParam String name,
            @RequestParam String startDate,
            @RequestParam String registrationDeadline,
            @RequestParam int maxPlayers
    ) {
        TournamentDto dto = tournamentService.createTournament(
                userDetails.getUsername(), clubId, name, startDate, registrationDeadline, maxPlayers
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Get all tournaments for a club")
    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<TournamentDto>> getTournamentsByClub(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId
    ) {
        List<TournamentDto> tournaments = tournamentService.getTournamentsByClub(parseUuid(clubId), userDetails.getUsername());
        return ResponseEntity.ok(tournaments);
    }

    @Operation(summary = "Get all active tournaments")
    @GetMapping
    public ResponseEntity<List<TournamentDto>> getAllTournaments(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<TournamentDto> tournaments = tournamentService.getAllTournaments(userDetails.getUsername());
        return ResponseEntity.ok(tournaments);
    }

    @Operation(summary = "Get tournament details with registrations")
    @GetMapping("/{tournamentId}")
    public ResponseEntity<TournamentDetailsDto> getTournamentDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId
    ) {
        TournamentDetailsDto details = tournamentService.getTournamentDetails(parseUuid(tournamentId), userDetails.getUsername());
        return ResponseEntity.ok(details);
    }

    @Operation(summary = "Register for a tournament")
    @PostMapping("/{tournamentId}/register")
    public ResponseEntity<TournamentRegistrationDto> registerForTournament(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId
    ) {
        TournamentRegistrationDto dto = tournamentService.registerForTournament(
                userDetails.getUsername(), parseUuid(tournamentId)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Unregister from a tournament")
    @DeleteMapping("/{tournamentId}/register")
    public ResponseEntity<Void> unregisterFromTournament(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId
    ) {
        tournamentService.unregisterFromTournament(userDetails.getUsername(), parseUuid(tournamentId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cancel/delete a tournament (admin only, OPEN status)")
    @DeleteMapping("/{tournamentId}")
    public ResponseEntity<Void> cancelTournament(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId
    ) {
        tournamentService.cancelTournament(userDetails.getUsername(), parseUuid(tournamentId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Generate bracket for tournament (admin only)")
    @PostMapping("/{tournamentId}/bracket")
    public ResponseEntity<TournamentDto> generateBracket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId
    ) {
        TournamentDto dto = tournamentService.generateBracket(userDetails.getUsername(), parseUuid(tournamentId));
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get tournament bracket")
    @GetMapping("/{tournamentId}/bracket")
    public ResponseEntity<TournamentBracketDto> getBracket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId
    ) {
        TournamentBracketDto bracket = tournamentService.getTournamentBracket(parseUuid(tournamentId));
        return ResponseEntity.ok(bracket);
    }

    @Operation(summary = "Get tournament referees")
    @GetMapping("/{tournamentId}/referees")
    public ResponseEntity<List<TournamentRefereeDto>> getReferees(
            @PathVariable String tournamentId
    ) {
        List<TournamentRefereeDto> referees = tournamentService.getReferees(parseUuid(tournamentId));
        return ResponseEntity.ok(referees);
    }

    @Operation(summary = "Add referee to tournament (admin only)")
    @PostMapping("/{tournamentId}/referees")
    public ResponseEntity<Void> addReferee(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId,
            @RequestParam String username
    ) {
        tournamentService.addReferee(userDetails.getUsername(), parseUuid(tournamentId), username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove referee from tournament (admin only)")
    @DeleteMapping("/{tournamentId}/referees")
    public ResponseEntity<Void> removeReferee(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId,
            @RequestParam String username
    ) {
        tournamentService.removeReferee(userDetails.getUsername(), parseUuid(tournamentId), username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Assign referee to match (admin only)")
    @PostMapping("/{tournamentId}/matches/{matchId}/referee")
    public ResponseEntity<Void> assignRefereeToMatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String tournamentId,
            @PathVariable String matchId,
            @RequestParam String username
    ) {
        tournamentService.assignRefereeToMatch(
                userDetails.getUsername(), parseUuid(tournamentId), parseUuid(matchId), username
        );
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get tournament status summary")
    @GetMapping("/{tournamentId}/status")
    public ResponseEntity<TournamentStatusDto> getTournamentStatus(
            @PathVariable String tournamentId
    ) {
        TournamentStatusDto status = tournamentService.getTournamentStatus(parseUuid(tournamentId));
        return ResponseEntity.ok(status);
    }

    private static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }
}
