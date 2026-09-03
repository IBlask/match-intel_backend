package com.match_intel.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FollowRequestDto {
    private UUID id;
    private String followerUsername;
    private String followerFirstName;
    private String followerLastName;
    private String status;
    private String timestamp;
}
