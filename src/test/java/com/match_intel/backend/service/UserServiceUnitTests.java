package com.match_intel.backend.service;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User(
                "jDoe1",
                "John",
                "Doe",
                "john.doe@example.com",
                "pass123"
        );
    }


    @Test
    @DisplayName("getUserById - existing user")
    void getUserById_shouldReturnUserWhenExists() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertNull(result.get().getId());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("getUserById - non existing user")
    void getUserById_shouldReturnEmptyWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }



    @Test
    @DisplayName("getUserByEmail - existing user")
    void getUserByEmail_shouldReturnUserWhenExists() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserByEmail - non existing user")
    void getUserByEmail_shouldReturnEmptyWhenUserDoesNotExist() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByEmail(email);

        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }



    @Test
    @DisplayName("saveUser - successfully saved")
    void saveUser_shouldSaveUser() {
        userService.saveUser(user);

        verify(userRepository).save(user);
    }



    @Test
    @DisplayName("enableUser - successfully enabled")
    void enableUser_shouldSetEnabledAndSaveUser() {
        userService.enableUser(user);

        assertTrue(user.isEnabled());
        verify(userRepository).save(user);
    }



    @Test
    @DisplayName("registerUser - successfully registered")
    void registerUser_shouldRegisterNewUserWhenEmailAndUsernameAreAvailable() {
        RegisterUserRequest request = new RegisterUserRequest("jDoe1", "test@example.com", "John", "Doe", "pass123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        User result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals("jDoe1", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser - email already exists")
    void registerUser_shouldThrowExceptionWhenEmailIsInUse() {
        RegisterUserRequest request = new RegisterUserRequest("jDoe1", "test@example.com", "John", "Doe", "pass123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                userService.registerUser(request));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        assertEquals("Entered email is already in use!", exception.getMessage());
        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser - username already exists")
    void registerUser_shouldThrowExceptionWhenUsernameIsTaken() {
        RegisterUserRequest request = new RegisterUserRequest("jDoe1", "test@example.com", "John", "Doe", "pass123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new User()));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                userService.registerUser(request));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        assertEquals("Entered username is taken! Try another.", exception.getMessage());
        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }
}
