package com.match_intel.backend.service;

import com.match_intel.backend.entity.Club;
import com.match_intel.backend.entity.ClubFollow;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.ClubFollowRepository;
import com.match_intel.backend.repository.ClubRepository;
import com.match_intel.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClubFollowServiceUnitTests {

    @Mock
    private ClubFollowRepository clubFollowRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ClubFollowService clubFollowService;

    private User user;
    private Club club;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User("user", "User", "One", "user@example.com", "pass123");
        club = new Club();
        club.setId(UUID.randomUUID());
        club.setName("Test Club");
    }

    @Test
    @DisplayName("followClub - saves a follow")
    void followClub_shouldSaveFollow() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(clubFollowRepository.existsByClubAndUser(club, user)).thenReturn(false);

        clubFollowService.followClub("user", club.getId());

        verify(clubFollowRepository).save(any(ClubFollow.class));
    }

    @Test
    @DisplayName("followClub - double follow throws 400")
    void followClub_shouldRejectDoubleFollow() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(clubFollowRepository.existsByClubAndUser(club, user)).thenReturn(true);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubFollowService.followClub("user", club.getId()));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(clubFollowRepository, never()).save(any(ClubFollow.class));
    }

    @Test
    @DisplayName("unfollowClub - deletes the follow")
    void unfollowClub_shouldDeleteFollow() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        ClubFollow follow = new ClubFollow();
        follow.setClub(club);
        follow.setUser(user);
        when(clubFollowRepository.findByClubAndUser(club, user)).thenReturn(Optional.of(follow));

        clubFollowService.unfollowClub("user", club.getId());

        verify(clubFollowRepository).delete(follow);
    }

    @Test
    @DisplayName("unfollowClub - not following throws 400")
    void unfollowClub_shouldRejectWhenNotFollowing() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(clubFollowRepository.findByClubAndUser(club, user)).thenReturn(Optional.empty());

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubFollowService.unfollowClub("user", club.getId()));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("getFollowedClubs - returns followed clubs")
    void getFollowedClubs_shouldReturnFollowedClubs() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        ClubFollow follow = new ClubFollow();
        follow.setClub(club);
        follow.setUser(user);
        when(clubFollowRepository.findByUserOrderByTimestampDesc(user)).thenReturn(List.of(follow));

        List<Club> clubs = clubFollowService.getFollowedClubs("user");

        assertEquals(1, clubs.size());
        assertEquals(club.getId(), clubs.get(0).getId());
    }

    @Test
    @DisplayName("getFollowersCount - counts followers")
    void getFollowersCount_shouldCountFollowers() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(clubFollowRepository.countByClub(club)).thenReturn(3L);

        assertEquals(3L, clubFollowService.getFollowersCount(club.getId()));
    }

    @Test
    @DisplayName("getFollowersCount - unknown club returns 0")
    void getFollowersCount_shouldReturnZeroForUnknownClub() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.empty());

        assertEquals(0L, clubFollowService.getFollowersCount(club.getId()));
    }
}
