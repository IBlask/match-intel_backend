package com.match_intel.backend.controller;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest registerUserRequest) {
        authService.register(registerUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/email_verification_resend")
    public ResponseEntity<?> emailVerificationResend(@RequestParam String email) {
        authService.emailVerificationResend(email);
        return ResponseEntity.status(HttpStatus.OK).body("Verification email successfully sent!");
    }
}
