package com.match_intel.backend.service;

import com.match_intel.backend.auth.token.EmailConfirmationToken;
import com.match_intel.backend.auth.token.EmailConfirmationTokenService;
import com.match_intel.backend.auth.token.RegistrationSessionToken;
import com.match_intel.backend.auth.token.RegistrationSessionTokenService;
import com.match_intel.backend.auth.utils.EmailValidator;
import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceUnitTests {

    @InjectMocks
    private AuthService authService;
    @Mock
    private EmailValidator emailValidator;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailConfirmationTokenService emailTokenService;
    @Mock
    private RegistrationSessionTokenService sessionTokenService;
    @Mock
    private RegistrationSessionToken sessionToken;
    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("register - success")
    void register_shouldRegisterUserAndSendConfirmationEmail() {
        RegisterUserRequest request = new RegisterUserRequest("jDoe1", "test@example.com", "John", "Doe", "pass123");

        User user = new User();
        user.setEmail("test@example.com");

        EmailConfirmationToken emailToken = new EmailConfirmationToken(
                UUID.randomUUID(), UUID.randomUUID().toString(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(15)
        );

        RegistrationSessionToken mockSessionToken = mock(RegistrationSessionToken.class);
        when(mockSessionToken.getToken()).thenReturn("mockToken");

        when(emailValidator.validate(request.getEmail())).thenReturn(true);
        when(userService.registerUser(request)).thenReturn(user);
        when(emailTokenService.createToken(user.getId())).thenReturn(emailToken);
        when(sessionTokenService.createToken(user.getId())).thenReturn(mockSessionToken);

        String token = authService.register(request);

        assertNotNull(token);
        assertEquals("mockToken", token);

        verify(emailValidator).validate(request.getEmail());
        verify(userService).registerUser(request);
        verify(emailService).sendEmailConfirmationAsync(user, emailToken);
        verify(sessionTokenService).createToken(user.getId());
        verify(sessionTokenService).saveToken(mockSessionToken);
    }

    @Test
    @DisplayName("register - invalid email")
    void register_shouldThrowExceptionForInvalidEmailOnRegister() {
        RegisterUserRequest request = new RegisterUserRequest("jDoe1", "invalid-email", "John", "Doe", "pass123");

        when(emailValidator.validate(request.getEmail())).thenReturn(false);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> authService.register(request));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());

        verify(emailValidator).validate(request.getEmail());
        verifyNoInteractions(userService, emailService);
    }



    @Test
    @DisplayName("emailVerificationResend - success")
    void emailVerificationResend_shouldResendEmailVerification() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        EmailConfirmationToken emailToken = new EmailConfirmationToken(UUID.randomUUID(), "token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(15));

        when(emailValidator.validate(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));
        when(emailTokenService.createToken(user.getId())).thenReturn(emailToken);

        authService.emailVerificationResend(email);

        verify(emailValidator).validate(email);
        try {
            verify(emailService).sendEmailConfirmationSafely(user, emailToken);
        } catch (MessagingException e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("emailVerificationResend - email already verified")
    void emailVerificationResend_shouldThrowExceptionIfEmailAlreadyVerified() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);

        when(emailValidator.validate(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> authService.emailVerificationResend(email));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        assertEquals("Email is already verified! You can log in.", exception.getMessage());

        verify(emailValidator).validate(email);
        verify(userService).getUserByEmail(email);
        verifyNoInteractions(emailService);
    }



    @Test
    @DisplayName("changeEmailBySessionToken - success")
    void changeEmailBySessionToken_shouldChangeEmail() {
        String sessionToken = "sessionToken";
        String newEmail = "newemail@example.com";

        RegistrationSessionToken session = new RegistrationSessionToken(UUID.randomUUID(), sessionToken, LocalDateTime.now().plusMinutes(15));
        User user = new User();
        user.setEmail("oldemail@example.com");

        when(sessionTokenService.getToken(sessionToken)).thenReturn(Optional.of(session));
        when(userService.getUserById(session.getUserId())).thenReturn(Optional.of(user));
        when(emailValidator.validate(any(String.class))).thenReturn(true);

        authService.changeEmailBySessionToken(sessionToken, newEmail);

        assertEquals(newEmail, user.getEmail());

        verify(sessionTokenService).getToken(sessionToken);
        verify(userService).getUserById(session.getUserId());
        verify(userService).saveUser(user);
        verify(emailTokenService).createToken(user.getId());
        verify(emailTokenService).saveToken(null);
        verify(emailService).sendEmailConfirmationAsync(any(User.class), isNull(EmailConfirmationToken.class));
    }

    @Test
    @DisplayName("changeEmailBySessionToken - invalid session token")
    void changeEmailBySessionToken_shouldThrowExceptionForInvalidSessionToken() {
        String sessionToken = "invalidSessionToken";
        String newEmail = "newemail@example.com";

        when(emailValidator.validate(newEmail)).thenReturn(true);
        when(sessionTokenService.getToken(sessionToken)).thenReturn(Optional.empty());

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> authService.changeEmailBySessionToken(sessionToken, newEmail));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        assertEquals("Session token is not valid! Please try again or contact our support team.", exception.getMessage());
    }

    @Test
    @DisplayName("changeEmailBySessionToken - expired session token")
    void changeEmailBySessionToken_shouldThrowExceptionForExpiredSessionToken() {
        String sessionToken = "expiredSessionToken";
        String newEmail = "newemail@example.com";
        RegistrationSessionToken expiredSession = new RegistrationSessionToken(UUID.randomUUID(), UUID.randomUUID().toString(), LocalDateTime.now().minusDays(1));

        when(emailValidator.validate(newEmail)).thenReturn(true);
        when(sessionTokenService.getToken(sessionToken)).thenReturn(Optional.of(expiredSession));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> authService.changeEmailBySessionToken(sessionToken, newEmail));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        assertEquals("The time to change your email address has already expired and you can no longer change it. Please register again with a new username and correct email address.", exception.getMessage());
    }



    @Test
    @DisplayName("confirmEmail - success")
    void confirmEmail_shouldConfirmEmailSuccessfully() {
        String confirmationToken = "validToken";
        User user = new User();
        user.setEmail("test@example.com");
        user.setEnabled(false);

        EmailConfirmationToken token = new EmailConfirmationToken(UUID.randomUUID(), confirmationToken, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15));

        when(emailTokenService.getToken(confirmationToken)).thenReturn(Optional.of(token));
        when(emailTokenService.getLastTokenByUserId(null)).thenReturn(Optional.of(token));
        when(userService.getUserById(token.getUserId())).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authService.confirmEmail(confirmationToken));

        verify(emailTokenService).getToken(confirmationToken);
        verify(userService).getUserById(token.getUserId());
        verify(userService).enableUser(user);
    }

    @Test
    @DisplayName("confirmEmail - invalid confirmation token")
    void confirmEmail_shouldThrowExceptionForInvalidConfirmationToken() {
        String confirmationToken = "invalidToken";

        when(emailTokenService.getToken(confirmationToken)).thenReturn(Optional.empty());

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> authService.confirmEmail(confirmationToken));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        assertEquals("Confirmation token is not valid. Please try again or request new confirmation token.", exception.getMessage());
    }

    @Test
    @DisplayName("confirmEmail - email already confirmed")
    void confirmEmail_shouldThrowExceptionIfEmailAlreadyConfirmed() {
        String confirmationToken = "validToken";
        User user = new User();
        user.setEmail("test@example.com");
        user.setEnabled(true);

        EmailConfirmationToken token = new EmailConfirmationToken(UUID.randomUUID(), confirmationToken, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15));

        when(emailTokenService.getToken(confirmationToken)).thenReturn(Optional.of(token));
        when(userService.getUserById(token.getUserId())).thenReturn(Optional.of(user));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> authService.confirmEmail(confirmationToken));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        assertEquals("Email address has already been confirmed. You can log in to your account.", exception.getMessage());
    }

    @Test
    @DisplayName("confirmEmail - expired confirmation token")
    void confirmEmail_shouldThrowExceptionForExpiredConfirmationToken() {
        String confirmationToken = "validToken";
        User user = new User();
        user.setEmail("test@example.com");

        EmailConfirmationToken token = new EmailConfirmationToken(UUID.randomUUID(), confirmationToken, LocalDateTime.now().minusMinutes(15), LocalDateTime.now().minusMinutes(5));

        when(emailTokenService.getLastTokenByUserId(null)).thenReturn(Optional.of(token));
        when(emailTokenService.getToken(confirmationToken)).thenReturn(Optional.of(token));
        when(userService.getUserById(token.getUserId())).thenReturn(Optional.of(user));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () -> authService.confirmEmail(confirmationToken));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        assertEquals("Token is expired. Please request a new confirmation token by logging into your account.", exception.getMessage());
    }
}
