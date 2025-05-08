package com.match_intel.backend.auth.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Entity
@NoArgsConstructor
public class PasswordResetToken {

    private static final int EXPIRATION_PERIOD = 60;    // 60 min

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;
    @Getter
    @Column(nullable = false)
    private UUID userId;
    @Getter
    @Column(nullable = false)
    private String token;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Getter
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Getter
    @Column(nullable = false)
    private boolean isUsed;


    public PasswordResetToken(UUID userId) {
        this.userId = userId;
        this.token = String.format("%06d", new Random().nextInt(999999));
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_PERIOD);
        this.isUsed = false;
    }


    public void markAsUsed() {
        this.isUsed = true;
    }
}
