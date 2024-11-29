package com.match_intel.backend.controller;

import com.match_intel.backend.dto.request.ChangeEmailBySessionRequest;
import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.service.AuthService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;


    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(ref = "#components/schemas/RegisterResponse")))
    public ResponseEntity<Map<String, String>> register (@RequestBody RegisterUserRequest registerUserRequest) {
        String sessionToken = authService.register(registerUserRequest);
        Map<String, String> response = new HashMap<>();
        response.put("sessionToken", sessionToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping(value = "/email_verification_resend",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200",
            description = "Verification email successfully sent",
            content = @Content(schema = @Schema(ref = "#components/schemas/MessageResponse"),
                    examples = @ExampleObject("{\"message\": \"Verification email successfully sent!\"}")))
    public ResponseEntity<Map<String, String>> emailVerificationResend (
            @RequestBody @Schema(ref = "#components/schemas/EmailRequest") Map<String, String> requestDto
    ) {
        authService.emailVerificationResend(requestDto.get("email"));
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification email successfully sent!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PatchMapping(value = "/change_email_by_session_token",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "204",
            description = "Email address changed successfully")
    public ResponseEntity<Void> changeEmailBySessionToken (@RequestBody ChangeEmailBySessionRequest requestDto) {
        authService.changeEmailBySessionToken(requestDto.getSessionToken(), requestDto.getNewEmail());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping(value = "/confirm_email")
    @ApiResponse(responseCode = "204",
            description = "Email confirmed")
    public ResponseEntity<Void> confirmEmail (@RequestParam String token) {
        authService.confirmEmail(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
