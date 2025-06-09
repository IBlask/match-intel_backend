package com.match_intel.backend.dto.request;

import lombok.Getter;

@Getter
public class LoginUserRequest {
    private String username;
    private String password;
}
