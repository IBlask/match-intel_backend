package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.CreateMatchResponse;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            @RequestParam String initialServer,
            @RequestParam int visibility
    ) {
        MatchVisibility matchVisibility;
        switch (visibility) {
            case 2 -> matchVisibility = MatchVisibility.PUBLIC;
            case 1 -> matchVisibility = MatchVisibility.FOLLOWERS;
            default -> matchVisibility = MatchVisibility.PRIVATE;
        }

        CreateMatchResponse responseDto = matchService.createMatch(player1, player2, initialServer, matchVisibility);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Adding a point")
    @PostMapping("/add_point")
    @ApiResponse(responseCode = "201",
            description = "Point added successfully")
    public ResponseEntity<Point> addPoint(
            @RequestParam String matchId,
            @RequestParam String scoringPlayerUsername,
            @RequestParam Boolean forced
    ) {
        UUID matchUUID = UUID.fromString(matchId);
        Point point = matchService.addPoint(matchUUID, scoringPlayerUsername, forced);
        return ResponseEntity.status(HttpStatus.CREATED).body(point);
    }

    @GetMapping("/of/{username}")
    public ResponseEntity<?> getMatchesOfUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username
    ) {
        String currentUsername = userDetails.getUsername();
        List<Match> visibleMatches = matchService.getVisibleMatches(currentUsername, username);
        return ResponseEntity.ok(visibleMatches);
    }

    @GetMapping("/followed")
    public ResponseEntity<List<Match>> getFollowedVisibleMatches(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        List<Match> matches = matchService.getVisibleMatchesFromFollowedUsers(username);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/like")
    public ResponseEntity<Map<String, Boolean>> likeMatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String matchId
    ) {
        String username = userDetails.getUsername();
        UUID matchUUID = UUID.fromString(matchId);
        boolean liked = matchService.likeMatch(username, matchUUID);

        Map<String, Boolean> returnMap = new HashMap<>();
        returnMap.put("liked", liked);
        return ResponseEntity.ok(returnMap);
    }

    @GetMapping("/likes_count")
    public ResponseEntity<Long> getLikesCount(
            @RequestParam String matchId
    ) {
        UUID matchUUID = UUID.fromString(matchId);
        long likesCount = matchService.getLikesCount(matchUUID);
        return ResponseEntity.ok(likesCount);
    }

    @GetMapping("/likes_list")
    public ResponseEntity<List<User>> getLikesList(
            @RequestParam String matchId
    ) {
        UUID matchUUID = UUID.fromString(matchId);
        List<User> likesList = matchService.getLikesList(matchUUID);
        return ResponseEntity.ok(likesList);
    }

    @PostMapping("/comment/{matchId}")
    public ResponseEntity<Void> commentMatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String matchId,
            @RequestParam String comment
    ) {
        String username = userDetails.getUsername();
        UUID matchUUID = UUID.fromString(matchId);
        matchService.commentMatch(username, matchUUID, comment);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comments_list/{matchId}")
    public ResponseEntity<List<Comment>> getComments(
            @PathVariable String matchId
    ) {
        UUID matchUUID = UUID.fromString(matchId);
        return ResponseEntity.ok(matchService.getComments(matchUUID));
    }

    @GetMapping("/comments_count/{matchId}")
    public ResponseEntity<Long> getCommentsCount(
            @PathVariable String matchId
    ) {
        UUID matchUUID = UUID.fromString(matchId);
        return ResponseEntity.ok(matchService.getCommentsCount(matchUUID));
    }
}
