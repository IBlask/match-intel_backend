package com.match_intel.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_username", referencedColumnName = "username")
    @Getter
    @Setter
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", referencedColumnName = "id")
    @Setter
    private Match match;

    @Column(nullable = false)
    @Getter
    @Setter
    private String comment;

    @Column(nullable = false)
    @Getter
    private LocalDateTime createdAt = LocalDateTime.now();
}
