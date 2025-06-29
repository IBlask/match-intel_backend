package com.match_intel.backend.auth.token;

import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.exception.GeneralUnhandledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;


    public String createPasswordResetToken(User user) {
        PasswordResetToken token = new PasswordResetToken(user.getId());
        passwordResetTokenRepository.save(token);

        return token.getToken();
    }


    public Optional<String> validateToken(String requestedToken) {
        Optional<PasswordResetToken> token = passwordResetTokenRepository.findByToken(requestedToken);
        if (token.isEmpty()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }

        if (token.get().isUsed()) {
            return Optional.of("Token has already been used!");
        }
        if (token.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.of("Token has expired!");
        }

        return Optional.empty();
    }


    public UUID getUserIdByToken(String requestToken) throws RuntimeException {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(requestToken);

        if (tokenOpt.isEmpty()) {
            throw new GeneralUnhandledException("Token doesn't exist in the database!");
        }

        return tokenOpt.get().getUserId();
    }


    public PasswordResetToken getPasswordResetTokenByTokenAsString(String token) {
        Optional<PasswordResetToken> passwordResetTokenOpt = passwordResetTokenRepository.findByToken(token);
        if (passwordResetTokenOpt.isEmpty()) {
            throw new GeneralUnhandledException("Token doesn't exist in the database!");
        }

        return passwordResetTokenOpt.get();
    }


    public void markTokenAsUsed(PasswordResetToken token) {
        token.markAsUsed();
        passwordResetTokenRepository.save(token);
    }
}
