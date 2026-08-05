package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, UUID> {
    Optional<Club> findByName(String name);
    List<Club> findAllByOrderByNameAsc();
}
