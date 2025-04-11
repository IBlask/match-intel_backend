package com.match_intel.backend.auth.token;

import com.match_intel.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;


    public String createPasswordResetToken(User user) {
        PasswordResetToken token = new PasswordResetToken(user.getId());
        passwordResetTokenRepository.save(token);

        return token.getToken();
    }
}
