package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.CourtReservationDto;
import com.match_intel.backend.dto.response.OccupiedSlotDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.ClubCourtRepository;
import com.match_intel.backend.repository.ClubRepository;
import com.match_intel.backend.repository.CourtReservationRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class CourtReservationService {

    @Autowired
    private CourtReservationRepository courtReservationRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private ClubCourtRepository clubCourtRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubService clubService;

    public CourtReservationDto createReservation(
            String username,
            UUID clubId,
            UUID courtId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Club not found"));
        ClubCourt court = clubCourtRepository.findById(courtId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Court not found"));
        if (!court.getClub().getId().equals(club.getId())) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Court does not belong to this club");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Reservation date cannot be in the past");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        List<CourtReservation> existing = courtReservationRepository
                .findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                        courtId, date, ReservationStatus.CANCELLED);
        for (CourtReservation reservation : existing) {
            if (reservation.getStartTime().isBefore(endTime)
                    && reservation.getEndTime().isAfter(startTime)) {
                throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Time slot already reserved");
            }
        }

        CourtReservation reservation = new CourtReservation();
        reservation.setClub(club);
        reservation.setCourt(court);
        reservation.setUser(user);
        reservation.setReservationDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(
                club.getReservationType() == ReservationType.INSTANT
                        ? ReservationStatus.CONFIRMED
                        : ReservationStatus.PENDING
        );
        courtReservationRepository.save(reservation);
        return toDto(reservation);
    }

    public void confirmReservation(String username, UUID reservationId) {
        CourtReservation reservation = getReservation(reservationId);
        if (!clubService.isClubAdmin(username, reservation.getClub().getId())) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can confirm reservations");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Only pending reservations can be confirmed");
        }
        reservation.setStatus(ReservationStatus.CONFIRMED);
        courtReservationRepository.save(reservation);
    }

    public void cancelReservation(String username, UUID reservationId) {
        CourtReservation reservation = getReservation(reservationId);
        boolean isCreator = reservation.getUser().getUsername().equals(username);
        boolean isAdmin = clubService.isClubAdmin(username, reservation.getClub().getId());
        if (!isCreator && !isAdmin) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only the creator or a club admin can cancel this reservation");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Reservation already cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        courtReservationRepository.save(reservation);
    }

    public List<OccupiedSlotDto> getOccupiedSlots(UUID clubId, UUID courtId, LocalDate date) {
        ClubCourt court = clubCourtRepository.findById(courtId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Court not found"));
        if (!court.getClub().getId().equals(clubId)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Court does not belong to this club");
        }
        return courtReservationRepository
                .findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
                        courtId, date, ReservationStatus.CANCELLED)
                .stream()
                .map(r -> new OccupiedSlotDto(r.getStartTime(), r.getEndTime()))
                .toList();
    }

    public List<CourtReservationDto> getClubReservations(UUID clubId, LocalDate date) {
        return courtReservationRepository
                .findByClub_IdAndReservationDateOrderByStartTimeAsc(clubId, date)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private CourtReservation getReservation(UUID reservationId) {
        return courtReservationRepository.findById(reservationId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    private CourtReservationDto toDto(CourtReservation reservation) {
        return new CourtReservationDto(
                reservation.getId(),
                reservation.getClub().getId(),
                reservation.getClub().getName(),
                reservation.getCourt().getId(),
                reservation.getCourt().getName(),
                reservation.getUser().getUsername(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus()
        );
    }
}
