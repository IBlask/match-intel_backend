package com.match_intel.backend.service;

import com.match_intel.backend.auth.token.PasswordResetTokenService;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    PasswordResetTokenService tokenService;
    @Autowired
    UserService userService;
    @Autowired
    EmailService emailService;


    public void request_token(String email) {
        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isEmpty()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Can't find user with the entered email!");
        }
        User user = userOptional.get();

        String token = tokenService.createPasswordResetToken(user);

        emailService.sendPasswordResetTokenAsync(user, token);
    }
}
