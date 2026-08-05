package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Club;
import com.match_intel.backend.entity.ClubMember;
import com.match_intel.backend.entity.ClubRole;
import com.match_intel.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubMemberRepository extends JpaRepository<ClubMember, UUID> {
    List<ClubMember> findByClub(Club club);
    Optional<ClubMember> findByClubAndUser(Club club, User user);
    boolean existsByClubAndUserAndRole(Club club, User user, ClubRole role);
}
