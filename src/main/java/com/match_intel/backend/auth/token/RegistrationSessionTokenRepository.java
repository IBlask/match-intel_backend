package com.match_intel.backend.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegistrationSessionTokenRepository extends JpaRepository<RegistrationSessionToken, UUID> {
    Optional<RegistrationSessionToken> findByToken(String token);
}
