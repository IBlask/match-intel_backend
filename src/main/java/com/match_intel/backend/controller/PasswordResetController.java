package com.match_intel.backend.controller;

import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.service.PasswordResetService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password_reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;


    @GetMapping("/request_token/{email}")
    @ApiResponse(responseCode = "200",
            description = "Password reset email sent successfully")
    public ResponseEntity<Void> request_token (@PathVariable String email) {
        if (email == null || email.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Please provide an email!");
        }

        passwordResetService.request_token(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
