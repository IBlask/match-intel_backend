package com.match_intel.backend.controller;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest registerUserRequest) {
        Optional<String> responseMessage = authService.register(registerUserRequest);

        if (responseMessage.isPresent()) {
            return ResponseEntity.ok(responseMessage.get());
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
