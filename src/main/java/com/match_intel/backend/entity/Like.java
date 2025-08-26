package com.match_intel.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_username", referencedColumnName = "username")
    @Setter
    @Getter
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", referencedColumnName = "id")
    @Setter
    private Match match;
}
