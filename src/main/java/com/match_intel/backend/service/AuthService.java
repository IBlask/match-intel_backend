package com.match_intel.backend.service;

import com.match_intel.backend.auth.token.EmailConfirmationToken;
import com.match_intel.backend.auth.token.EmailConfirmationTokenService;
import com.match_intel.backend.auth.utils.EmailValidator;
import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.exception.ServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
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

        emailService.sendEmailConfirmationAsync(newUser, token);
    }


    public void emailVerificationResend(String email) {
        boolean isValidEmail = emailValidator.validate(email);
        if (!isValidEmail) {
            throw new ClientErrorException(HttpStatus.valueOf(400), "Entered email is not valid!");
        }

        Optional<User> userOptional = userService.getUserByEmail(email);
        if(userOptional.isEmpty()) {
            throw new ClientErrorException(HttpStatus.valueOf(400), "Entered email does not exist!");
        }

        User user = userOptional.get();
        if(user.isEnabled()) {
            throw new ClientErrorException(HttpStatus.valueOf(400), "Email is already verified! You can log in.");
        }

        EmailConfirmationToken newToken = tokenService.createToken(userOptional.get().getId());
        tokenService.saveToken(newToken);

        try {
            emailService.sendEmailConfirmationSafely(userOptional.get(), newToken);
        } catch (Exception exception) {
            log.error(
                    String.format(
                            "Error while sending email confirmation to user %s with token %s: %s",
                            user.getId() != null ? user.getId() : "UNKNOWN",
                            newToken.getIdAsString() != null ? newToken.getIdAsString() : "UNKNOWN",
                            exception.getMessage()
                    ),
                    exception
            );
            throw new ServerErrorException(
                    HttpStatus.valueOf(500),
                    "Couldn't send a confirmation email due to an internal error. Please try again later or contact our support team."
            );
        }
    }
}