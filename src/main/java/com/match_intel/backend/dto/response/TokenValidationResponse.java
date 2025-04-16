package com.match_intel.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@NoArgsConstructor
public class TokenValidationResponse {

    @Schema(description = "Represents token's validation", example = "false")
    private boolean isValid = false;

    @Schema(description = "When the provided token is invalid, represents a reason why the token is invalid.",
            example = "Token is expired!")
    private String message = "";


    public void setValid() {
        this.isValid = true;
    }

    public void setNotValid(String message) {
        this.message = message;
    }
}
