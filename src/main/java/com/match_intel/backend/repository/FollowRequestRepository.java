
package com.match_intel.backend.repository;

import com.match_intel.backend.entity.FollowRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.entity.FollowRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, UUID> {

    List<FollowRequest> findByFolloweeAndStatus(User followee, FollowRequestStatus status);
    Optional<FollowRequest> findByFollowerAndFollowee(User follower, User followee);
    List<FollowRequest> findByFollowerAndStatus(User follower, FollowRequestStatus status);
    List<FollowRequest> findByFolloweeAndFollowerAndStatus(User followee, User follower, FollowRequestStatus status);
}
