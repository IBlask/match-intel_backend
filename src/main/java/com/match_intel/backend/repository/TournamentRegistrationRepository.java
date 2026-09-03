package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Tournament;
import com.match_intel.backend.entity.TournamentRegistration;
import com.match_intel.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, UUID> {
    List<TournamentRegistration> findByTournamentOrderByRegisteredAtAsc(Tournament tournament);
    Optional<TournamentRegistration> findByTournamentAndUser(Tournament tournament, User user);
    boolean existsByTournamentAndUser(Tournament tournament, User user);
    long countByTournament(Tournament tournament);
}
