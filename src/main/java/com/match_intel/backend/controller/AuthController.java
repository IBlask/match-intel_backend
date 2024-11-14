package com.match_intel.backend.controller;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.service.AuthService;
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


    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest registerUserRequest) {
        String sessionToken = authService.register(registerUserRequest);
        Map<String, String> response = new HashMap<>();
        response.put("sessionToken", sessionToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/email_verification_resend", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> emailVerificationResend(@RequestParam String email) {
        authService.emailVerificationResend(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification email successfully sent!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping(value = "/change_email_by_session_token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeEmailBySessionToken(@RequestParam String sessionToken, @RequestParam String newEmail) {
        authService.changeEmailBySessionToken(sessionToken, newEmail);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(value = "/confirm_email", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> confirmEmail(@RequestParam String token) {
        authService.confirmEmail(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
