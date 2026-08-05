package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Club;
import com.match_intel.backend.entity.ClubReview;
import com.match_intel.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubReviewRepository extends JpaRepository<ClubReview, UUID> {
    List<ClubReview> findByClubOrderByCreatedAtDesc(Club club);
    Optional<ClubReview> findByClubAndUser(Club club, User user);
    void deleteByClubAndUser(Club club, User user);
}
