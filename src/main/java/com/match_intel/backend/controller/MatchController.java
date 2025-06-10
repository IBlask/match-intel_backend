package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.CreateMatchResponse;
import com.match_intel.backend.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches")
@Tag(name = "Matches", description = "Managing tennis matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Operation(summary = "Creating new tennis match")
    @PostMapping("/create")
    public ResponseEntity<CreateMatchResponse> createMatch(
            @RequestParam String player1,
            @RequestParam String player2,
            @RequestParam String initialServer) {
        CreateMatchResponse responseDto = matchService.createMatch(player1, player2, initialServer);
        return ResponseEntity.ok(responseDto);
    }
}
