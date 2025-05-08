package com.match_intel.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PasswordResetRequest {

    @Schema(description = "Password reset token (sent by an email)",
            example = "849587")
    private String token;

    @Schema(description = "New password",
            example = "pass123")
    private String newPassword;
}
