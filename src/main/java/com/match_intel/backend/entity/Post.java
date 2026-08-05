package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchVisibility visibility = MatchVisibility.PUBLIC;

    @Column(name = "number_of_likes", nullable = false)
    private int numberOfLikes = 0;

    @Column(name = "number_of_comments", nullable = false)
    private int numberOfComments = 0;
}
