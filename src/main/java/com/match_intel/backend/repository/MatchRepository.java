package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Match;
import com.match_intel.backend.entity.MatchVisibility;
import com.match_intel.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findByPlayer1OrPlayer2(User player1, User player2);

    @Query("""
        SELECT m FROM Match m
        JOIN FollowRequest fr ON (m.player1.id = fr.followee.id OR m.player2.id = fr.followee.id)
        WHERE fr.follower.id = :followerId
          AND fr.status = 'ACCEPTED'
          AND m.visibility IN ('PUBLIC', 'FOLLOWERS')
    """)
    List<Match> findVisibleMatchesOfFollowees(@Param("followerId") UUID followerId);
}
