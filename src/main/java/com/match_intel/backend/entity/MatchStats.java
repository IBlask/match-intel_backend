package com.match_intel.backend.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MatchStats {
    
    private Point point;

    private Integer player1Set1UnforcedErrors = null;
    private Integer player1Set1ForcedErrors = null;
    private Integer player1Set2UnforcedErrors = null;
    private Integer player1Set2ForcedErrors = null;
    private Integer player1Set3UnforcedErrors = null;
    private Integer player1Set3ForcedErrors = null;

    private Integer player2Set1UnforcedErrors = null;
    private Integer player2Set1ForcedErrors = null;
    private Integer player2Set2UnforcedErrors = null;
    private Integer player2Set2ForcedErrors = null;
    private Integer player2Set3UnforcedErrors = null;
    private Integer player2Set3ForcedErrors = null;

    private Integer player1Set1Efficiency = null;
    private Integer player1Set2Efficiency = null;
    private Integer player1Set3Efficiency = null;
    private Integer player2Set1Efficiency = null;
    private Integer player2Set2Efficiency = null;
    private Integer player2Set3Efficiency = null;
}
