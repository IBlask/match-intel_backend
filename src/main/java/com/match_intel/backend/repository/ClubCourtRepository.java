package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Club;
import com.match_intel.backend.entity.ClubCourt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClubCourtRepository extends JpaRepository<ClubCourt, UUID> {
    List<ClubCourt> findByClubOrderByNameAsc(Club club);
}
