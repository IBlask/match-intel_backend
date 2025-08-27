package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    boolean existsByUser_UsernameAndMatch_Id(String username, UUID matchId);

    long countByMatch_Id(UUID matchId);

    List<Like> findAllByMatch_Id(UUID matchId);

    Optional<Like> findByMatch_IdAndUser_Username(UUID machId, String username);
}
