
package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.FollowRequestDto;
import com.match_intel.backend.entity.FollowRequest;
import com.match_intel.backend.entity.FollowRequestStatus;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.FollowRequestRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private final FollowRequestRepository followRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public FollowService(
        FollowRequestRepository followRequestRepository,
        UserRepository userRepository
    ) {
        this.followRequestRepository = followRequestRepository;
        this.userRepository = userRepository;
    }

    public void sendFollowRequest(String followerUsername, String followeeUsername) {
        if (followerUsername.equals(followeeUsername)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "User cannot follow themselves!");
        }

        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Follower not found"));
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Followee not found"));

        Optional<FollowRequest> existingRequest = followRequestRepository.findByFollowerAndFollowee(follower, followee);
        if (existingRequest.isPresent()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Follow request already exists.");
        }

        FollowRequest request = new FollowRequest();
        request.setFollower(follower);
        request.setFollowee(followee);
        request.setStatus(FollowRequestStatus.PENDING);
        followRequestRepository.save(request);
    }

    public void acceptFollowRequest(String followerUsername, String followeeUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Follower not found"));
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Followee not found"));

        FollowRequest request = followRequestRepository.findByFollowerAndFollowee(follower, followee)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Follow request not found"));

        if (request.getStatus() != FollowRequestStatus.PENDING) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Follow request is not pending.");
        }

        request.setStatus(FollowRequestStatus.ACCEPTED);
        followRequestRepository.save(request);
    }

    public boolean isFollowing(String followerUsername, String followeeUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Follower not found"));
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Followee not found"));

        return followRequestRepository.findByFollowerAndFollowee(follower, followee)
                .map(req -> req.getStatus() == FollowRequestStatus.ACCEPTED)
                .orElse(false);
    }

    public void removeFollower(String followeeUsername, String username) {
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Followee not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        FollowRequest request = followRequestRepository.findByFollowerAndFollowee(user, followee)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Follow relationship not found"));

        if (request.getStatus() != FollowRequestStatus.ACCEPTED) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "User is not a follower.");
        }

        followRequestRepository.delete(request);
    }

    public List<FollowRequestDto> getIncomingPendingRequests(String followeeUsername) {
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        return followRequestRepository.findByFolloweeAndStatus(followee, FollowRequestStatus.PENDING).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private FollowRequestDto toDto(FollowRequest req) {
        FollowRequestDto dto = new FollowRequestDto();
        dto.setId(req.getId());
        dto.setStatus(req.getStatus() != null ? req.getStatus().name() : null);
        dto.setTimestamp(req.getTimestamp() != null
                ? req.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null);
        if (req.getFollower() != null) {
            dto.setFollowerUsername(req.getFollower().getUsername());
            dto.setFollowerFirstName(req.getFollower().getFirstName());
            dto.setFollowerLastName(req.getFollower().getLastName());
        }
        return dto;
    }
}
