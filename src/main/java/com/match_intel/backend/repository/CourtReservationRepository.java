package com.match_intel.backend.repository;

import com.match_intel.backend.entity.CourtReservation;
import com.match_intel.backend.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CourtReservationRepository extends JpaRepository<CourtReservation, UUID> {

    List<CourtReservation> findByCourt_IdAndReservationDateAndStatusNotOrderByStartTimeAsc(
            UUID courtId, LocalDate date, ReservationStatus status);

    List<CourtReservation> findByClub_IdAndReservationDateOrderByStartTimeAsc(UUID clubId, LocalDate date);
}
