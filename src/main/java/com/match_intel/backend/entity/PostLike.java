package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "post_likes")
@Getter
@Setter
public class PostLike {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_username", referencedColumnName = "username")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;
}
