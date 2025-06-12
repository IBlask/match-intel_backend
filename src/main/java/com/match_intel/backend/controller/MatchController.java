package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.CreateMatchResponse;
import com.match_intel.backend.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/matches")
@Tag(name = "Matches", description = "Managing tennis matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Operation(summary = "Creating new tennis match")
    @PostMapping("/create")
    @ApiResponse(responseCode = "201",
            description = "New match created successfully")
    public ResponseEntity<CreateMatchResponse> createMatch(
            @RequestParam String player1,
            @RequestParam String player2,
            @RequestParam String initialServer
    ) {
        CreateMatchResponse responseDto = matchService.createMatch(player1, player2, initialServer);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Adding a point")
    @PostMapping("/add_point")
    @ApiResponse(responseCode = "201",
            description = "Point added successfully")
    public ResponseEntity<Void> addPoint(
            @RequestParam String matchId,
            @RequestParam String scoringPlayerUsername
    ) {
        UUID matchUUID = UUID.fromString(matchId);
        matchService.addPoint(matchUUID, scoringPlayerUsername);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
