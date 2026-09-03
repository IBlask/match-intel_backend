package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Tournament;
import com.match_intel.backend.entity.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TournamentRepository extends JpaRepository<Tournament, UUID> {
    List<Tournament> findByClubIdOrderByCreatedAtDesc(UUID clubId);
    List<Tournament> findByStatusIn(List<TournamentStatus> statuses);
}
