package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "club_reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"club_id", "user_id"}))
@Getter
@Setter
public class ClubReview {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "club_id", referencedColumnName = "id")
    private Club club;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
