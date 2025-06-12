package com.match_intel.backend.controller;

import com.match_intel.backend.entity.User;
import com.match_intel.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
