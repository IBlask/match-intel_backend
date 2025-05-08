package com.match_intel.backend.service;

import com.match_intel.backend.auth.token.PasswordResetToken;
import com.match_intel.backend.auth.token.PasswordResetTokenService;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.exception.GeneralUnhandledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    PasswordResetTokenService tokenService;
    @Autowired
    UserService userService;
    @Autowired
    EmailService emailService;


    public void requestToken(String email) {
        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isEmpty()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Can't find user with the entered email!");
        }
        User user = userOptional.get();

        String token = tokenService.createPasswordResetToken(user);

        emailService.sendPasswordResetTokenAsync(user, token);
    }


    public void changePassword(String token, String newPassword) {
        Optional<String> tokenValidity = tokenService.validateToken(token);
        if (tokenValidity.isPresent()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, tokenValidity.get());
        }

        UUID userId;
        try {
            userId = tokenService.getUserIdByToken(token);
        } catch (GeneralUnhandledException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Token doesn't exist in the database!");
        }

        try {
            PasswordResetToken passwordResetToken = tokenService.getPasswordResetTokenByTokenAsString(token);
            userService.changePassword(userId, newPassword);
            tokenService.markTokenAsUsed(passwordResetToken);
        } catch (GeneralUnhandledException e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
