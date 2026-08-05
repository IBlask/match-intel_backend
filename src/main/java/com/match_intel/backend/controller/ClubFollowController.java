package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.ClubDto;
import com.match_intel.backend.service.ClubFollowService;
import com.match_intel.backend.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clubs")
@Tag(name = "Club Follows", description = "Following tennis clubs")
public class ClubFollowController {

    @Autowired
    private ClubFollowService clubFollowService;
    @Autowired
    private ClubService clubService;

    @Operation(summary = "Following a club")
    @PostMapping("/{clubId}/follow")
    public ResponseEntity<Void> followClub(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId
    ) {
        clubFollowService.followClub(userDetails.getUsername(), UUID.fromString(clubId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unfollowing a club")
    @PostMapping("/{clubId}/unfollow")
    public ResponseEntity<Void> unfollowClub(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId
    ) {
        clubFollowService.unfollowClub(userDetails.getUsername(), UUID.fromString(clubId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Getting followed clubs")
    @GetMapping("/followed")
    public ResponseEntity<List<ClubDto>> getFollowedClubs(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ClubDto> clubs = clubFollowService.getFollowedClubs(userDetails.getUsername()).stream()
                .map(club -> clubService.toDto(club, userDetails.getUsername()))
                .toList();
        return ResponseEntity.ok(clubs);
    }
}
