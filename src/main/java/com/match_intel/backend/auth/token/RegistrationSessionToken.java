package com.match_intel.backend.auth.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
public class RegistrationSessionToken {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;
    @Column(nullable = false, unique = true)
    private UUID userId;
    @Column(nullable = false)
    private String token;
    @Column(nullable = false)
    private LocalDateTime expiresAt;


    public RegistrationSessionToken(UUID userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
