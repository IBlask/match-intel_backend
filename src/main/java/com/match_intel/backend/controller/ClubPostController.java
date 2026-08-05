package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.ClubPostCommentDto;
import com.match_intel.backend.dto.response.ClubPostDto;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.service.ClubPostService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/clubs")
@Tag(name = "Club Posts", description = "Managing club posts")
public class ClubPostController {

    @Autowired
    private ClubPostService clubPostService;

    @Operation(summary = "Getting posts of a club")
    @GetMapping("/{clubId}/posts")
    public ResponseEntity<List<ClubPostDto>> getClubPosts(
            @PathVariable String clubId
    ) {
        List<ClubPostDto> posts = clubPostService.getClubPosts(UUID.fromString(clubId));
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Creating a club post (club admins only)")
    @PostMapping("/{clubId}/posts")
    public ResponseEntity<ClubPostDto> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String clubId,
            @RequestParam String content,
            @RequestParam(required = false) String imageUrl
    ) {
        ClubPostDto post = clubPostService.createPost(
                userDetails.getUsername(),
                UUID.fromString(clubId),
                content,
                imageUrl
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @Operation(summary = "Getting posts of followed clubs")
    @GetMapping("/posts/followed")
    public ResponseEntity<List<ClubPostDto>> getFollowedClubPosts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ClubPostDto> posts = clubPostService.getFollowedClubPosts(userDetails.getUsername());
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Getting a single club post")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ClubPostDto> getPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId
    ) {
        ClubPostDto post = clubPostService.getPost(userDetails.getUsername(), UUID.fromString(postId));
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "Deleting a club post (club admins only)")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId
    ) {
        clubPostService.deletePost(userDetails.getUsername(), UUID.fromString(postId));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Liking or unliking a club post")
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Map<String, Boolean>> likePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId
    ) {
        boolean liked = clubPostService.toggleLike(userDetails.getUsername(), UUID.fromString(postId));

        Map<String, Boolean> returnMap = new HashMap<>();
        returnMap.put("liked", liked);
        return ResponseEntity.ok(returnMap);
    }

    @Operation(summary = "Getting likes list of a club post")
    @GetMapping("/posts/{postId}/likes_list")
    public ResponseEntity<List<User>> getLikesList(
            @PathVariable String postId
    ) {
        List<User> likesList = clubPostService.getLikesList(UUID.fromString(postId));
        return ResponseEntity.ok(likesList);
    }

    @Operation(summary = "Commenting on a club post")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ClubPostCommentDto> commentPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId,
            @RequestParam String comment
    ) {
        ClubPostCommentDto newComment = ClubPostService.toCommentDto(
                clubPostService.addComment(userDetails.getUsername(), UUID.fromString(postId), comment)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @Operation(summary = "Getting comments of a club post")
    @GetMapping("/posts/{postId}/comments_list")
    public ResponseEntity<List<ClubPostCommentDto>> getComments(
            @PathVariable String postId
    ) {
        List<ClubPostCommentDto> comments = clubPostService.getComments(UUID.fromString(postId)).stream()
                .map(ClubPostService::toCommentDto)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Getting comments count of a club post")
    @GetMapping("/posts/{postId}/comments_count")
    public ResponseEntity<Long> getCommentsCount(
            @PathVariable String postId
    ) {
        long commentsCount = clubPostService.getCommentsCount(UUID.fromString(postId));
        return ResponseEntity.ok(commentsCount);
    }
}
