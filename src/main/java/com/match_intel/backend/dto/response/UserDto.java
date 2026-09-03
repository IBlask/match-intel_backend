package com.match_intel.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String username;
    private String profileVisibility;
    private boolean profileRestricted;
    private int following;
    private int followers;
    private List<MatchDto> matches;
    private boolean doesFollow;
    private boolean isFollowRequestSent;
}
