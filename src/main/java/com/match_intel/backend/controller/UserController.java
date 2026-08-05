package com.match_intel.backend.controller;

import com.match_intel.backend.dto.response.UserDto;
import com.match_intel.backend.entity.ProfileVisibility;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@Tag(name = "Users", description = "Managing user(s)")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(summary = "Search for users by name or username")
    @GetMapping("/search")
    @ApiResponse(responseCode = "200",
            description = "Users retrieved successfully")
    public ResponseEntity<List<Map<String, String>>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        List<Map<String, String>> result = users.stream().map(user -> Map.of(
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "username", user.getUsername()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getUserByUsername(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable String username
    ) {
        String currentUsername = currentUser.getUsername();
        UserDto userDto = userService.getUserByUsername(currentUsername, username);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Update visibility of the authenticated user's profile")
    @PatchMapping("/visibility")
    public ResponseEntity<Void> updateProfileVisibility(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestBody Map<String, String> request
    ) {
        String visibilityValue = request.get("visibility");
        if (visibilityValue == null || visibilityValue.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Please provide a visibility value.");
        }

        ProfileVisibility visibility;
        try {
            visibility = ProfileVisibility.valueOf(visibilityValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid visibility value.");
        }

        userService.updateProfileVisibility(currentUser.getUsername(), visibility);
        return ResponseEntity.ok().build();
    }
}
