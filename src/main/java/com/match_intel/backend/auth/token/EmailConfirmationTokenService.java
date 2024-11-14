package com.match_intel.backend.auth.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailConfirmationTokenService {

    @Autowired
    private EmailConfirmationTokenRepository tokenRepository;


    public EmailConfirmationToken createToken(UUID userId) {
        return new EmailConfirmationToken(
                userId,
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15)
        );
    }

    public void saveToken(EmailConfirmationToken token) {
        tokenRepository.save(token);
    }

    public Optional<EmailConfirmationToken> getToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public void setConfirmedAt(EmailConfirmationToken token) {
        token.setConfirmedAt();
        tokenRepository.save(token);
    }

    public Optional<EmailConfirmationToken> getLastTokenByUserId(UUID userId) {
        return tokenRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }
}
