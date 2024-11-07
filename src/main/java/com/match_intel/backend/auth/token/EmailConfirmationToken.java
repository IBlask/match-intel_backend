package com.match_intel.backend.auth.token;

import com.match_intel.backend.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class EmailConfirmationToken {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;
    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false)
    private String token;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column
    private LocalDateTime confirmedAt;


    public EmailConfirmationToken(UUID userId, String token, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.confirmedAt = null;
    }


    public String getToken() {
        return token;
    }
}
