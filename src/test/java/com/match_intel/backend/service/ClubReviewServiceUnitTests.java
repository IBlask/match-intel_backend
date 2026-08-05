package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.ClubReviewDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.ClubMemberRepository;
import com.match_intel.backend.repository.ClubRepository;
import com.match_intel.backend.repository.ClubReviewRepository;
import com.match_intel.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

class ClubReviewServiceUnitTests {

    @Mock
    private ClubReviewRepository clubReviewRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClubMemberRepository clubMemberRepository;
    @InjectMocks
    private ClubReviewService clubReviewService;

    private User user;
    private User owner;
    private Club club;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User("user", "User", "One", "user@example.com", "pass123");
        owner = new User("owner", "Owner", "One", "owner@example.com", "pass123");

        club = new Club();
        club.setId(UUID.randomUUID());
        club.setName("Test Club");
    }

    @Test
    @DisplayName("addOrUpdateReview - adds a new review and recomputes average")
    void addReview_shouldAddAndRecomputeAverage() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubMemberRepository.findByClubAndUser(club, user)).thenReturn(Optional.empty());
        when(clubReviewRepository.findByClubAndUser(club, user)).thenReturn(Optional.empty());
        when(clubReviewRepository.findByClubOrderByCreatedAtDesc(club))
                .thenReturn(List.of(review(5)));
        when(clubReviewRepository.save(any(ClubReview.class))).thenAnswer(invocation -> {
            ClubReview r = invocation.getArgument(0);
            if (r.getId() == null) {
                r.setId(UUID.randomUUID());
            }
            return r;
        });

        ClubReviewDto dto = clubReviewService.addOrUpdateReview(
                "user", club.getId(), 5, "Great!"
        );

        assertNotNull(dto);
        assertEquals(5, dto.rating());
        assertEquals(5.0, club.getAverageRating());
        assertEquals(1, club.getNumberOfReviews());
        verify(clubRepository).save(club);
    }

    @Test
    @DisplayName("addOrUpdateReview - overwrites existing review instead of creating a second")
    void addReview_shouldOverwriteExisting() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubMemberRepository.findByClubAndUser(club, user)).thenReturn(Optional.empty());
        ClubReview existing = new ClubReview();
        existing.setId(UUID.randomUUID());
        existing.setClub(club);
        existing.setUser(user);
        existing.setRating(3);
        when(clubReviewRepository.findByClubAndUser(club, user)).thenReturn(Optional.of(existing));
        when(clubReviewRepository.findByClubOrderByCreatedAtDesc(club))
                .thenReturn(List.of(review(4)));

        clubReviewService.addOrUpdateReview("user", club.getId(), 4, "Better");

        ArgumentCaptor<ClubReview> captor = ArgumentCaptor.forClass(ClubReview.class);
        verify(clubReviewRepository).save(captor.capture());
        assertEquals(4, captor.getValue().getRating());
        assertEquals(existing.getId(), captor.getValue().getId());
        assertEquals(4.0, club.getAverageRating());
    }

    @Test
    @DisplayName("addOrUpdateReview - owner cannot review own club")
    void addReview_shouldRejectOwner() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        ClubMember ownerMember = new ClubMember();
        ownerMember.setClub(club);
        ownerMember.setUser(owner);
        ownerMember.setRole(ClubRole.OWNER);
        when(clubMemberRepository.findByClubAndUser(club, owner)).thenReturn(Optional.of(ownerMember));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubReviewService.addOrUpdateReview("owner", club.getId(), 5, null));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
        verify(clubReviewRepository, never()).save(any(ClubReview.class));
    }

    @Test
    @DisplayName("addOrUpdateReview - rating out of range throws 400")
    void addReview_shouldRejectInvalidRating() {
        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubReviewService.addOrUpdateReview("user", club.getId(), 0, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());

        exception = assertThrows(ClientErrorException.class, () ->
                clubReviewService.addOrUpdateReview("user", club.getId(), 6, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("deleteOwnReview - recomputes average to null when last review deleted")
    void deleteReview_shouldSetAverageNullWhenEmpty() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        ClubReview existing = new ClubReview();
        existing.setId(UUID.randomUUID());
        existing.setClub(club);
        existing.setUser(user);
        existing.setRating(5);
        when(clubReviewRepository.findByClubAndUser(club, user)).thenReturn(Optional.of(existing));
        when(clubReviewRepository.findByClubOrderByCreatedAtDesc(club)).thenReturn(List.of());

        clubReviewService.deleteOwnReview("user", club.getId());

        verify(clubReviewRepository).delete(existing);
        assertNull(club.getAverageRating());
        assertEquals(0, club.getNumberOfReviews());
    }

    @Test
    @DisplayName("deleteOwnReview - review not found throws 404")
    void deleteReview_shouldRejectWhenNoReview() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubReviewRepository.findByClubAndUser(club, user)).thenReturn(Optional.empty());

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubReviewService.deleteOwnReview("user", club.getId()));

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode());
    }

    private ClubReview review(int rating) {
        ClubReview review = new ClubReview();
        review.setId(UUID.randomUUID());
        review.setClub(club);
        review.setUser(user);
        review.setRating(rating);
        return review;
    }
}
