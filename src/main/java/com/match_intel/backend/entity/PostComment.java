package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "post_comments")
@Getter
@Setter
public class PostComment {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_username", referencedColumnName = "username")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
