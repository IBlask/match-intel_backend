package com.match_intel.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MatchDto {
    private UUID id;
    private String initialServer;
    private String startDate;
    private String startTime;
    private String finalScore;
    private String visibility;
    private boolean finished;
    private Integer player1Efficiency;
    private Integer player2Efficiency;
    private int numberOfLikes;
    private int numberOfComments;
    private PlayerInfoDto player1;
    private PlayerInfoDto player2;
    private PlayerInfoDto referee;
    private String clubName;
    private boolean likedByUser;
}
