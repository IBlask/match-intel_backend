
package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class FollowRequest {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id", referencedColumnName = "id")
    private User follower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "followee_id", referencedColumnName = "id")
    private User followee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowRequestStatus status = FollowRequestStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
