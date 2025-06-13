package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Match;
import com.match_intel.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findByPlayer1OrPlayer2(User player1, User player2);
}
