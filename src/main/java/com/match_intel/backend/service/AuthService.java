package com.match_intel.backend.service;

import com.match_intel.backend.auth.token.EmailConfirmationToken;
import com.match_intel.backend.auth.token.EmailConfirmationTokenService;
import com.match_intel.backend.auth.token.RegistrationSessionToken;
import com.match_intel.backend.auth.token.RegistrationSessionTokenService;
import com.match_intel.backend.auth.utils.EmailValidator;
import com.match_intel.backend.dto.request.LoginUserRequest;
import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.exception.ServerErrorException;
import com.match_intel.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private EmailValidator emailValidator;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailConfirmationTokenService emailTokenService;
    @Autowired
    private RegistrationSessionTokenService sessionTokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;


    public String register(RegisterUserRequest request) {
        boolean isValidEmail = emailValidator.validate(request.getEmail());
        if (!isValidEmail) {
            throw new ClientErrorException(HttpStatus.valueOf(400), "Entered email is not valid!");
        }

        User newUser = userService.registerUser(request);

        EmailConfirmationToken emailToken = emailTokenService.createToken(newUser.getId());
        emailTokenService.saveToken(emailToken);

        emailService.sendEmailConfirmationAsync(newUser, emailToken);

        RegistrationSessionToken sessionToken = sessionTokenService.createToken(newUser.getId());
        sessionTokenService.saveToken(sessionToken);
        return sessionToken.getToken();
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

        EmailConfirmationToken newToken = emailTokenService.createToken(userOptional.get().getId());
        emailTokenService.saveToken(newToken);

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


    public void changeEmailBySessionToken(String sessionToken, String newEmail) {
        boolean isEmailValid = emailValidator.validate(newEmail);
        if (!isEmailValid) {
            throw new ClientErrorException(
                    HttpStatus.valueOf(400),
                    "Entered email address is not valid!"
            );
        }

        Optional<RegistrationSessionToken> tokenOptional = sessionTokenService.getToken(sessionToken);
        if (tokenOptional.isEmpty()) {
            throw new ClientErrorException(
                    HttpStatus.valueOf(400),
                    "Session token is not valid! Please try again or contact our support team."
            );
        }

        RegistrationSessionToken token = tokenOptional.get();
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ClientErrorException(
                    HttpStatus.valueOf(400),
                    "The time to change your email address has already expired and you can no longer change it."
                    + " Please register again with a new username and correct email address."
            );
        }

        Optional<User> userOptional = userService.getUserById(token.getUserId());
        if (userOptional.isEmpty()) {
            throw new ServerErrorException(
                    HttpStatus.valueOf(500),
                    "Couldn't change your email address due to an internal error. Please register again or contact our support team."
            );
        }

        User user = userOptional.get();
        user.setEmail(newEmail);
        userService.saveUser(user);

        EmailConfirmationToken emailToken = emailTokenService.createToken(user.getId());
        emailTokenService.saveToken(emailToken);

        emailService.sendEmailConfirmationAsync(user, emailToken);
    }


    public void confirmEmail(String confirmationToken) {
        Optional<EmailConfirmationToken> tokenOptional = emailTokenService.getToken(confirmationToken);
        if (tokenOptional.isEmpty()) {
            throw new ClientErrorException(
                    HttpStatus.valueOf(400),
                    "Confirmation token is not valid. Please try again or request new confirmation token."
            );
        }

        EmailConfirmationToken token = tokenOptional.get();
        Optional<User> userOptional = userService.getUserById(token.getUserId());
        if (userOptional.isEmpty()) {
            throw new ServerErrorException(
                    HttpStatus.valueOf(500),
                    "Couldn't confirm your email due to an internal error. Please log in to your account and request a new confirmation token."
            );
        }

        User user = userOptional.get();
        if (user.isEnabled()) {
            throw new ClientErrorException(
                    HttpStatus.valueOf(400),
                    "Email address has already been confirmed. You can log in to your account."
            );
        }

        Optional<EmailConfirmationToken> lastTokenOptional = emailTokenService.getLastTokenByUserId(user.getId());
        if (lastTokenOptional.isEmpty()) {
            throw new ServerErrorException(
                    HttpStatus.valueOf(500),
                    "Couldn't confirm your email due to an internal error. Please log in to your account and request a new confirmation token."
            );
        }
        if (!confirmationToken.equals(lastTokenOptional.get().getToken())) {
            throw new ClientErrorException(
                    HttpStatus.valueOf(400),
                    "Please use the last issued token or request a new confirmation token by logging into your account."
            );
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ClientErrorException(
                    HttpStatus.valueOf(400),
                    "Token is expired. Please request a new confirmation token by logging into your account."
            );
        }

        emailTokenService.setConfirmedAt(token);
        userService.enableUser(user);
    }


    public User authenticate(LoginUserRequest requestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getUsername(),
                        requestDto.getPassword()
                )
        );

        return userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow();
    }
}