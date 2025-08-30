package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointRepository extends JpaRepository<Point, UUID> {
    Optional<Point> findTopByMatchIdOrderByCreatedAtDesc(UUID matchId);
    int countByMatchIdAndPlayerWhoScoredAndForced(UUID matchId, String playerWhoScored, boolean forced);
    List<Point> findAllByMatchId(UUID matchId);
}
