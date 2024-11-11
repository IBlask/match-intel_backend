package com.match_intel.backend.service;

import com.match_intel.backend.auth.token.EmailConfirmationToken;
import com.match_intel.backend.auth.token.EmailConfirmationTokenService;
import com.match_intel.backend.auth.utils.EmailValidator;
import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class AuthService {

    @Autowired
    private EmailValidator emailValidator;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailConfirmationTokenService tokenService;
    @Autowired
    private UserService userService;


    public void register(RegisterUserRequest request) {
        boolean isValidEmail = emailValidator.validate(request.getEmail());
        if (!isValidEmail) {
            throw new ClientErrorException(HttpStatus.valueOf(400), "Entered email is not valid!");
        }

        User newUser = userService.registerUser(request);

        EmailConfirmationToken token = tokenService.createToken(newUser.getId());
        tokenService.saveToken(token);


        try {
            emailService.sendEmailConfirmation(newUser, token.getToken());
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }
}