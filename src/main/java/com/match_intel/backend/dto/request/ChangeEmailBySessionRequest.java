package com.match_intel.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChangeEmailBySessionRequest {

    @Schema(description = "Registration session token (returned by registration endpoint)",
            example = "af4c19dd-e13f-4d72-84a5-a4328eb7c44c")
    private String sessionToken;

    @Schema(description = "User's new email address",
            example = "exapmle@example.com")
    private String newEmail;


    public String getSessionToken() {
        return sessionToken;
    }

    public String getNewEmail() {
        return newEmail;
    }
}
