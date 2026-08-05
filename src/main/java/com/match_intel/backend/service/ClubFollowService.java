package com.match_intel.backend.service;

import com.match_intel.backend.entity.Club;
import com.match_intel.backend.entity.ClubFollow;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.ClubFollowRepository;
import com.match_intel.backend.repository.ClubRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClubFollowService {

    @Autowired
    private ClubFollowRepository clubFollowRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private UserRepository userRepository;

    public void followClub(String username, UUID clubId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Club not found"));

        if (clubFollowRepository.existsByClubAndUser(club, user)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Already following this club");
        }

        ClubFollow follow = new ClubFollow();
        follow.setClub(club);
        follow.setUser(user);
        clubFollowRepository.save(follow);
    }

    public void unfollowClub(String username, UUID clubId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Club not found"));

        ClubFollow follow = clubFollowRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Not following this club"));

        clubFollowRepository.delete(follow);
    }

    public boolean isFollowing(String username, UUID clubId) {
        if (username == null) {
            return false;
        }
        User user = userRepository.findByUsername(username)
                .orElse(null);
        Club club = clubRepository.findById(clubId)
                .orElse(null);
        if (user == null || club == null) {
            return false;
        }
        return clubFollowRepository.existsByClubAndUser(club, user);
    }

    public long getFollowersCount(UUID clubId) {
        return clubRepository.findById(clubId)
                .map(clubFollowRepository::countByClub)
                .orElse(0L);
    }

    public List<Club> getFollowedClubs(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        return clubFollowRepository.findByUserOrderByTimestampDesc(user).stream()
                .map(ClubFollow::getClub)
                .toList();
    }
}
