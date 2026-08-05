package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.UserDto;
import com.match_intel.backend.entity.FollowRequestStatus;
import com.match_intel.backend.entity.ProfileVisibility;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.FollowRequestRepository;
import com.match_intel.backend.repository.MatchRepository;
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

class UserProfileVisibilityUnitTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FollowRequestRepository followRequestRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private FollowService followService;
    @InjectMocks
    private UserService userService;

    private User currentUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        currentUser = new User(
                "jDoe1",
                "John",
                "Doe",
                "john.doe@example.com",
                "pass123"
        );
        targetUser = new User(
                "jSmith2",
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "pass123"
        );
    }

    @Test
    @DisplayName("getUserByUsername - PUBLIC profile is fully accessible")
    void getUserByUsername_shouldReturnFullAccessForPublicProfile() {
        targetUser.setProfileVisibility(ProfileVisibility.PUBLIC);
        when(userRepository.findByUsername("jDoe1")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("jSmith2")).thenReturn(Optional.of(targetUser));

        UserDto result = userService.getUserByUsername("jDoe1", "jSmith2");

        assertNotNull(result);
        assertFalse(result.isProfileRestricted());
        assertEquals("PUBLIC", result.getProfileVisibility());
    }

    @Test
    @DisplayName("getUserByUsername - FOLLOWERS profile is accessible to followers")
    void getUserByUsername_shouldReturnFullAccessForFollower() {
        targetUser.setProfileVisibility(ProfileVisibility.FOLLOWERS);
        when(userRepository.findByUsername("jDoe1")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("jSmith2")).thenReturn(Optional.of(targetUser));
        when(followService.isFollowing("jDoe1", "jSmith2")).thenReturn(true);

        UserDto result = userService.getUserByUsername("jDoe1", "jSmith2");

        assertNotNull(result);
        assertFalse(result.isProfileRestricted());
    }

    @Test
    @DisplayName("getUserByUsername - FOLLOWERS profile is restricted for non-followers")
    void getUserByUsername_shouldRestrictFollowersProfileForNonFollower() {
        targetUser.setProfileVisibility(ProfileVisibility.FOLLOWERS);
        when(userRepository.findByUsername("jDoe1")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("jSmith2")).thenReturn(Optional.of(targetUser));
        when(followService.isFollowing("jDoe1", "jSmith2")).thenReturn(false);

        UserDto result = userService.getUserByUsername("jDoe1", "jSmith2");

        assertNotNull(result);
        assertTrue(result.isProfileRestricted());
        assertNull(result.getMatches());
    }

    @Test
    @DisplayName("getUserByUsername - PRIVATE profile is restricted for everyone else")
    void getUserByUsername_shouldRestrictPrivateProfile() {
        targetUser.setProfileVisibility(ProfileVisibility.PRIVATE);
        when(userRepository.findByUsername("jDoe1")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("jSmith2")).thenReturn(Optional.of(targetUser));

        UserDto result = userService.getUserByUsername("jDoe1", "jSmith2");

        assertNotNull(result);
        assertTrue(result.isProfileRestricted());
        assertNull(result.getMatches());
    }

    @Test
    @DisplayName("getUserByUsername - own profile is always fully accessible")
    void getUserByUsername_shouldReturnFullAccessForOwnProfile() {
        currentUser.setProfileVisibility(ProfileVisibility.PRIVATE);
        when(userRepository.findByUsername("jDoe1")).thenReturn(Optional.of(currentUser));

        UserDto result = userService.getUserByUsername("jDoe1", "jDoe1");

        assertNotNull(result);
        assertFalse(result.isProfileRestricted());
        verify(matchRepository).findByPlayer1OrPlayer2(currentUser, currentUser);
    }

    @Test
    @DisplayName("updateProfileVisibility - updates and saves the user")
    void updateProfileVisibility_shouldUpdateAndSaveUser() {
        when(userRepository.findByUsername("jDoe1")).thenReturn(Optional.of(currentUser));

        userService.updateProfileVisibility("jDoe1", ProfileVisibility.FOLLOWERS);

        assertEquals(ProfileVisibility.FOLLOWERS, currentUser.getProfileVisibility());
        verify(userRepository).save(currentUser);
    }

    @Test
    @DisplayName("updateProfileVisibility - throws when user not found")
    void updateProfileVisibility_shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                userService.updateProfileVisibility("unknown", ProfileVisibility.PUBLIC));

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }
}
