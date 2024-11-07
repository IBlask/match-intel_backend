package com.match_intel.backend.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, UUID> {
    Optional<EmailConfirmationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE EmailConfirmationToken c SET c.confirmedAt = ?2 WHERE c.token = ?1")
    int updateConfirmedAt(String token, LocalDateTime localDateTime);
}
