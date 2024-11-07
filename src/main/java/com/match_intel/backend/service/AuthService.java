package com.match_intel.backend.service;

import com.match_intel.backend.auth.token.EmailConfirmationToken;
import com.match_intel.backend.auth.token.EmailConfirmationTokenService;
import com.match_intel.backend.auth.utils.EmailValidator;
import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

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


    public Optional<String> register(RegisterUserRequest request) {
        boolean isValidEmail = emailValidator.validate(request.getEmail());
        if (!isValidEmail) {
            return Optional.of("Entered email is not valid!");
        }

        Pair<Optional<User>, Optional<String>> newUserAndExPair = userService.registerUser(request);
        if (newUserAndExPair.getFirst().isEmpty()) {
            return newUserAndExPair.getSecond();
        }

        EmailConfirmationToken token = tokenService.createToken(newUserAndExPair.getFirst().get().getId());
        tokenService.saveToken(token);


        new Thread(() -> {
            try {
                emailService.sendEmailConfirmation(newUserAndExPair.getFirst().get(), token.getToken());
            } catch (MessagingException | UnsupportedEncodingException e) {
                System.out.println(e.getMessage());
            }
        }).start();

        return Optional.empty();
    }
}