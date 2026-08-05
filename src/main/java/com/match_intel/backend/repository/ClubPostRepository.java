package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Club;
import com.match_intel.backend.entity.ClubPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClubPostRepository extends JpaRepository<ClubPost, UUID> {

    List<ClubPost> findByClubOrderByCreatedAtDesc(Club club);

    @Query("""
        SELECT cp FROM ClubPost cp
        JOIN ClubFollow cf ON cp.club.id = cf.club.id
        WHERE cf.user.id = :userId
        ORDER BY cp.createdAt DESC
    """)
    List<ClubPost> findVisiblePostsOfFollowedClubs(@Param("userId") UUID userId);
}
