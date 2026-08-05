package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Club;
import com.match_intel.backend.entity.ClubFollow;
import com.match_intel.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubFollowRepository extends JpaRepository<ClubFollow, UUID> {
    Optional<ClubFollow> findByClubAndUser(Club club, User user);
    boolean existsByClubAndUser(Club club, User user);
    long countByClub(Club club);
    List<ClubFollow> findByUserOrderByTimestampDesc(User user);
}
