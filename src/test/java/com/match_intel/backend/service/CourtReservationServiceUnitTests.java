package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.CourtReservationDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.ClubCourtRepository;
import com.match_intel.backend.repository.ClubRepository;
import com.match_intel.backend.repository.CourtReservationRepository;
import com.match_intel.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CourtReservationServiceUnitTests {

    @Mock
    private CourtReservationRepository courtReservationRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubCourtRepository clubCourtRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClubService clubService;
    @InjectMocks
    private CourtReservationService courtReservationService;

    private User user;
    private Club instantClub;
    private Club approvalClub;
    private ClubCourt court;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User("user", "User", "One", "user@example.com", "pass123");

        instantClub = new Club();
        instantClub.setId(UUID.randomUUID());
        instantClub.setName("Instant Club");
        instantClub.setReservationType(ReservationType.INSTANT);

        approvalClub = new Club();
        approvalClub.setId(UUID.randomUUID());
        approvalClub.setName("Approval Club");
        approvalClub.setReservationType(ReservationType.APPROVAL);

        court = new ClubCourt();
        court.setId(UUID.randomUUID());
        court.setName("Court 1");
        court.setClub(instantClub);
    }

    private CourtReservation reservation(LocalTime start, LocalTime end, ReservationStatus status, Club club) {
        CourtReservation reservation = new CourtReservation();
        reservation.setId(UUID.randomUUID());
        reservation.setClub(club);
        reservation.setCourt(court);
        reservation.setUser(user);
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(status);
        return reservation;
    }

    @Test
    @DisplayName("createReservation - INSTANT club auto-confirms")
    void createReservation_shouldAutoConfirmForInstantClub() {
        when(clubRepository.findById(instantClub.getId())).thenReturn(Optional.of(instantClub));
        when(clubCourtRepository.findById(court.getId())).thenReturn(Optional.of(court));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(courtReservationRepository
                .findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                        court.getId(), LocalDate.now().plusDays(1), ReservationStatus.CANCELLED))
                .thenReturn(List.of());
        when(courtReservationRepository.save(any(CourtReservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourtReservationDto dto = courtReservationService.createReservation(
                "user", instantClub.getId(), court.getId(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)
        );

        assertEquals(ReservationStatus.CONFIRMED, dto.status());
    }

    @Test
    @DisplayName("createReservation - APPROVAL club starts pending")
    void createReservation_shouldBePendingForApprovalClub() {
        ClubCourt approvalCourt = new ClubCourt();
        approvalCourt.setId(UUID.randomUUID());
        approvalCourt.setName("Clay A");
        approvalCourt.setClub(approvalClub);

        when(clubRepository.findById(approvalClub.getId())).thenReturn(Optional.of(approvalClub));
        when(clubCourtRepository.findById(approvalCourt.getId())).thenReturn(Optional.of(approvalCourt));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(courtReservationRepository
                .findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                        approvalCourt.getId(), LocalDate.now().plusDays(1), ReservationStatus.CANCELLED))
                .thenReturn(List.of());
        when(courtReservationRepository.save(any(CourtReservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourtReservationDto dto = courtReservationService.createReservation(
                "user", approvalClub.getId(), approvalCourt.getId(),
                LocalDate.now().plusDays(1), LocalTime.of(15, 0), LocalTime.of(16, 0)
        );

        assertEquals(ReservationStatus.PENDING, dto.status());
    }

    @Test
    @DisplayName("createReservation - overlapping CONFIRMED reservation throws 400")
    void createReservation_shouldRejectOverlapWithConfirmed() {
        when(clubRepository.findById(instantClub.getId())).thenReturn(Optional.of(instantClub));
        when(clubCourtRepository.findById(court.getId())).thenReturn(Optional.of(court));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(courtReservationRepository
                .findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                        court.getId(), LocalDate.now().plusDays(1), ReservationStatus.CANCELLED))
                .thenReturn(List.of(
                        reservation(LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.CONFIRMED, instantClub)
                ));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.createReservation(
                        "user", instantClub.getId(), court.getId(),
                        LocalDate.now().plusDays(1), LocalTime.of(10, 30), LocalTime.of(11, 30)
                ));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(courtReservationRepository, never()).save(any(CourtReservation.class));
    }

    @Test
    @DisplayName("createReservation - overlapping PENDING reservation throws 400")
    void createReservation_shouldRejectOverlapWithPending() {
        when(clubRepository.findById(instantClub.getId())).thenReturn(Optional.of(instantClub));
        when(clubCourtRepository.findById(court.getId())).thenReturn(Optional.of(court));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(courtReservationRepository
                .findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                        court.getId(), LocalDate.now().plusDays(1), ReservationStatus.CANCELLED))
                .thenReturn(List.of(
                        reservation(LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.PENDING, instantClub)
                ));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.createReservation(
                        "user", instantClub.getId(), court.getId(),
                        LocalDate.now().plusDays(1), LocalTime.of(10, 30), LocalTime.of(11, 30)
                ));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("createReservation - CANCELLED reservation does not conflict")
    void createReservation_shouldIgnoreCancelledReservations() {
        when(clubRepository.findById(instantClub.getId())).thenReturn(Optional.of(instantClub));
        when(clubCourtRepository.findById(court.getId())).thenReturn(Optional.of(court));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(courtReservationRepository
                .findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                        court.getId(), LocalDate.now().plusDays(1), ReservationStatus.CANCELLED))
                .thenReturn(List.of());
        when(courtReservationRepository.save(any(CourtReservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourtReservationDto dto = courtReservationService.createReservation(
                "user", instantClub.getId(), court.getId(),
                LocalDate.now().plusDays(1), LocalTime.of(10, 30), LocalTime.of(11, 30)
        );

        assertNotNull(dto);
        verify(courtReservationRepository).findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                court.getId(), LocalDate.now().plusDays(1), ReservationStatus.CANCELLED
        );
        verify(courtReservationRepository).save(any(CourtReservation.class));
    }

    @Test
    @DisplayName("createReservation - court of another club throws 400")
    void createReservation_shouldRejectForeignCourt() {
        ClubCourt foreignCourt = new ClubCourt();
        foreignCourt.setId(UUID.randomUUID());
        foreignCourt.setClub(approvalClub);

        when(clubRepository.findById(instantClub.getId())).thenReturn(Optional.of(instantClub));
        when(clubCourtRepository.findById(foreignCourt.getId())).thenReturn(Optional.of(foreignCourt));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.createReservation(
                        "user", instantClub.getId(), foreignCourt.getId(),
                        LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)
                ));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("createReservation - end before start throws 400")
    void createReservation_shouldRejectEndBeforeStart() {
        when(clubRepository.findById(instantClub.getId())).thenReturn(Optional.of(instantClub));
        when(clubCourtRepository.findById(court.getId())).thenReturn(Optional.of(court));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.createReservation(
                        "user", instantClub.getId(), court.getId(),
                        LocalDate.now().plusDays(1), LocalTime.of(11, 0), LocalTime.of(10, 0)
                ));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("createReservation - past date throws 400")
    void createReservation_shouldRejectPastDate() {
        when(clubRepository.findById(instantClub.getId())).thenReturn(Optional.of(instantClub));
        when(clubCourtRepository.findById(court.getId())).thenReturn(Optional.of(court));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.createReservation(
                        "user", instantClub.getId(), court.getId(),
                        LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)
                ));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("confirmReservation - admin confirms only PENDING")
    void confirmReservation_shouldConfirmPendingForAdmin() {
        CourtReservation pending = reservation(
                LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.PENDING, approvalClub
        );
        when(courtReservationRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(clubService.isClubAdmin("admin", approvalClub.getId())).thenReturn(true);

        courtReservationService.confirmReservation("admin", pending.getId());

        assertEquals(ReservationStatus.CONFIRMED, pending.getStatus());
        verify(courtReservationRepository).save(pending);
    }

    @Test
    @DisplayName("confirmReservation - non-admin gets 403")
    void confirmReservation_shouldRejectNonAdmin() {
        CourtReservation pending = reservation(
                LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.PENDING, approvalClub
        );
        when(courtReservationRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(clubService.isClubAdmin("user", approvalClub.getId())).thenReturn(false);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.confirmReservation("user", pending.getId()));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("confirmReservation - non-PENDING throws 400")
    void confirmReservation_shouldRejectNonPending() {
        CourtReservation confirmed = reservation(
                LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.CONFIRMED, instantClub
        );
        when(courtReservationRepository.findById(confirmed.getId())).thenReturn(Optional.of(confirmed));
        when(clubService.isClubAdmin("admin", instantClub.getId())).thenReturn(true);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.confirmReservation("admin", confirmed.getId()));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("cancelReservation - creator can cancel")
    void cancelReservation_shouldAllowCreator() {
        CourtReservation confirmed = reservation(
                LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.CONFIRMED, instantClub
        );
        when(courtReservationRepository.findById(confirmed.getId())).thenReturn(Optional.of(confirmed));
        when(clubService.isClubAdmin("user", instantClub.getId())).thenReturn(false);

        courtReservationService.cancelReservation("user", confirmed.getId());

        assertEquals(ReservationStatus.CANCELLED, confirmed.getStatus());
        verify(courtReservationRepository).save(confirmed);
    }

    @Test
    @DisplayName("cancelReservation - admin can cancel any reservation")
    void cancelReservation_shouldAllowAdmin() {
        CourtReservation confirmed = reservation(
                LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.CONFIRMED, instantClub
        );
        when(courtReservationRepository.findById(confirmed.getId())).thenReturn(Optional.of(confirmed));
        when(clubService.isClubAdmin("admin", instantClub.getId())).thenReturn(true);

        courtReservationService.cancelReservation("admin", confirmed.getId());

        assertEquals(ReservationStatus.CANCELLED, confirmed.getStatus());
    }

    @Test
    @DisplayName("cancelReservation - stranger gets 403")
    void cancelReservation_shouldRejectStranger() {
        CourtReservation confirmed = reservation(
                LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.CONFIRMED, instantClub
        );
        when(courtReservationRepository.findById(confirmed.getId())).thenReturn(Optional.of(confirmed));
        when(clubService.isClubAdmin("stranger", instantClub.getId())).thenReturn(false);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.cancelReservation("stranger", confirmed.getId()));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
        verify(courtReservationRepository, never()).save(any(CourtReservation.class));
    }

    @Test
    @DisplayName("cancelReservation - already cancelled throws 400")
    void cancelReservation_shouldRejectAlreadyCancelled() {
        CourtReservation cancelled = reservation(
                LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.CANCELLED, instantClub
        );
        when(courtReservationRepository.findById(cancelled.getId())).thenReturn(Optional.of(cancelled));
        when(clubService.isClubAdmin("user", instantClub.getId())).thenReturn(false);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                courtReservationService.cancelReservation("user", cancelled.getId()));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("getOccupiedSlots - returns non-cancelled slots")
    void getOccupiedSlots_shouldReturnNonCancelledSlots() {
        when(clubCourtRepository.findById(court.getId())).thenReturn(Optional.of(court));
        when(courtReservationRepository
                .findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                        court.getId(), LocalDate.now().plusDays(1), ReservationStatus.CANCELLED))
                .thenReturn(List.of(
                        reservation(LocalTime.of(10, 0), LocalTime.of(11, 0), ReservationStatus.CONFIRMED, instantClub)
                ));

        var slots = courtReservationService.getOccupiedSlots(
                instantClub.getId(), court.getId(), LocalDate.now().plusDays(1)
        );

        assertEquals(1, slots.size());
        assertEquals(LocalTime.of(10, 0), slots.get(0).startTime());
        assertEquals(LocalTime.of(11, 0), slots.get(0).endTime());
    }
}
