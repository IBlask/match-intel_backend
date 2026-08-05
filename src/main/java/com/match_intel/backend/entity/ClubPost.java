package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "club_posts")
@Getter
@Setter
public class ClubPost {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "club_id", referencedColumnName = "id")
    private Club club;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "number_of_likes", nullable = false)
    private int numberOfLikes = 0;

    @Column(name = "number_of_comments", nullable = false)
    private int numberOfComments = 0;
}
