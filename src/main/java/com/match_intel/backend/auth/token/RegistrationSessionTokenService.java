package com.match_intel.backend.auth.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationSessionTokenService {

    @Autowired
    RegistrationSessionTokenRepository tokenRepository;


    public RegistrationSessionToken createToken(UUID userId) {
        return new RegistrationSessionToken(
                userId,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusMinutes(10)
        );
    }

    public void saveToken(RegistrationSessionToken token) {
        tokenRepository.save(token);
    }

    public Optional<RegistrationSessionToken> getToken(String token) {
        return tokenRepository.findByToken(token);
    }
}
