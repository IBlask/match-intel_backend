package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.ClubReviewDto;
import com.match_intel.backend.service.ClubReviewService;
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
@RequestMapping("/clubs/{clubId}/reviews")
@Tag(name = "Club Reviews", description = "Managing club reviews")
public class ClubReviewController {

    @Autowired
    private ClubReviewService clubReviewService;

    @Operation(summary = "Getting reviews of a club")
    @GetMapping
    public ResponseEntity<List<ClubReviewDto>> getReviews(
            @PathVariable String clubId
    ) {
        List<ClubReviewDto> reviews = clubReviewService.getReviews(parseUuid(clubId));
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Adding or updating own review of a club")
    @PostMapping
    public ResponseEntity<ClubReviewDto> addOrUpdateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment
    ) {
        ClubReviewDto review = clubReviewService.addOrUpdateReview(
                userDetails.getUsername(),
                parseUuid(clubId),
                rating,
                comment
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @Operation(summary = "Deleting own review of a club")
    @DeleteMapping
    public ResponseEntity<Void> deleteOwnReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId
    ) {
        clubReviewService.deleteOwnReview(userDetails.getUsername(), parseUuid(clubId));
        return ResponseEntity.ok().build();
    }

    private static UUID parseUuid(String id) {
        return UUID.fromString(id);
    }
}
