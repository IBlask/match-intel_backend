package com.match_intel.backend.controller;

import com.match_intel.backend.auth.token.PasswordResetTokenService;
import com.match_intel.backend.dto.response.TokenValidationResponse;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.service.PasswordResetService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/password_reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private PasswordResetTokenService passwordResetTokenService;


    @PostMapping("/request_token/{email}")
    @ApiResponse(responseCode = "200",
            description = "Password reset email sent successfully")
    public ResponseEntity<Void> requestToken (@PathVariable String email) {
        if (email == null || email.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Please provide an email!");
        }

        passwordResetService.requestToken(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping("/validate_token")
    @ApiResponse(responseCode = "200",
            description = "Password reset token passed the validation. User can enter/send new password")
    public ResponseEntity<TokenValidationResponse> validateToken (@RequestParam String token) {
        if (token == null || token.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Please provide a token!");
        }

        Optional<String> tokenValidity = passwordResetTokenService.validateToken(token);

        TokenValidationResponse returnDto = new TokenValidationResponse();
        if (tokenValidity.isEmpty()) {
            returnDto.setValid();
        }
        else {
            returnDto.setNotValid(tokenValidity.get());
        }

        return ResponseEntity.status(HttpStatus.OK).body(returnDto);
    }
}
