package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.PostCommentDto;
import com.match_intel.backend.dto.response.PostDto;
import com.match_intel.backend.entity.MatchVisibility;
import com.match_intel.backend.entity.PostComment;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.service.PostService;
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
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Managing posts in the newsfeed")
public class PostController {

    @Autowired
    private PostService postService;

    @Operation(summary = "Creating a new post")
    @PostMapping("/create")
    public ResponseEntity<PostDto> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String content,
            @RequestParam(defaultValue = "PUBLIC") String visibility,
            @RequestParam(required = false) String imageUrl
    ) {
        MatchVisibility matchVisibility;
        try {
            matchVisibility = MatchVisibility.valueOf(visibility.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid visibility value.");
        }

        PostDto postDto = postService.createPost(
                userDetails.getUsername(),
                content,
                imageUrl,
                matchVisibility
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(postDto);
    }

    @GetMapping("/of/{username}")
    public ResponseEntity<List<PostDto>> getPostsOfUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username
    ) {
        List<PostDto> posts = postService.getVisiblePosts(userDetails.getUsername(), username);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/followed")
    public ResponseEntity<List<PostDto>> getFollowedPosts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<PostDto> posts = postService.getFollowedPosts(userDetails.getUsername());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId
    ) {
        PostDto postDto = postService.getPost(userDetails.getUsername(), UUID.fromString(postId));
        return ResponseEntity.ok(postDto);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId
    ) {
        postService.deletePost(userDetails.getUsername(), UUID.fromString(postId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/like")
    public ResponseEntity<Map<String, Boolean>> likePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String postId
    ) {
        boolean liked = postService.toggleLike(userDetails.getUsername(), UUID.fromString(postId));

        Map<String, Boolean> returnMap = new HashMap<>();
        returnMap.put("liked", liked);
        return ResponseEntity.ok(returnMap);
    }

    @GetMapping("/likes_count")
    public ResponseEntity<Long> getLikesCount(
            @RequestParam String postId
    ) {
        long likesCount = postService.getLikesCount(UUID.fromString(postId));
        return ResponseEntity.ok(likesCount);
    }

    @GetMapping("/likes_list")
    public ResponseEntity<List<User>> getLikesList(
            @RequestParam String postId
    ) {
        List<User> likesList = postService.getLikesList(UUID.fromString(postId));
        return ResponseEntity.ok(likesList);
    }

    @PostMapping("/comment/{postId}")
    public ResponseEntity<PostCommentDto> commentPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId,
            @RequestParam String comment
    ) {
        PostComment newComment = postService.addComment(
                userDetails.getUsername(),
                UUID.fromString(postId),
                comment
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(PostCommentDto.fromEntity(newComment));
    }

    @GetMapping("/comments_list/{postId}")
    public ResponseEntity<List<PostCommentDto>> getComments(
            @PathVariable String postId
    ) {
        List<PostComment> comments = postService.getComments(UUID.fromString(postId));
        return ResponseEntity.ok(comments.stream().map(PostCommentDto::fromEntity).toList());
    }

    @GetMapping("/comments_count/{postId}")
    public ResponseEntity<Long> getCommentsCount(
            @PathVariable String postId
    ) {
        long commentsCount = postService.getCommentsCount(UUID.fromString(postId));
        return ResponseEntity.ok(commentsCount);
    }
}
