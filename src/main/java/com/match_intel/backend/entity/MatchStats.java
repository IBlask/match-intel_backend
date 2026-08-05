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

    // First serve — overall
    private Integer player1FirstServesAttempted = null;
    private Integer player1FirstServesMade = null;
    private Double player1FirstServePercentage = null;
    private Integer player2FirstServesAttempted = null;
    private Integer player2FirstServesMade = null;
    private Double player2FirstServePercentage = null;

    // Second serve — overall
    private Integer player1SecondServesAttempted = null;
    private Integer player1SecondServesMade = null;
    private Double player1SecondServePercentage = null;
    private Integer player2SecondServesAttempted = null;
    private Integer player2SecondServesMade = null;
    private Double player2SecondServePercentage = null;

    // First serve — per set
    private Integer player1Set1FirstServesAttempted = null;
    private Integer player1Set1FirstServesMade = null;
    private Double player1Set1FirstServePercentage = null;
    private Integer player1Set2FirstServesAttempted = null;
    private Integer player1Set2FirstServesMade = null;
    private Double player1Set2FirstServePercentage = null;
    private Integer player1Set3FirstServesAttempted = null;
    private Integer player1Set3FirstServesMade = null;
    private Double player1Set3FirstServePercentage = null;
    private Integer player2Set1FirstServesAttempted = null;
    private Integer player2Set1FirstServesMade = null;
    private Double player2Set1FirstServePercentage = null;
    private Integer player2Set2FirstServesAttempted = null;
    private Integer player2Set2FirstServesMade = null;
    private Double player2Set2FirstServePercentage = null;
    private Integer player2Set3FirstServesAttempted = null;
    private Integer player2Set3FirstServesMade = null;
    private Double player2Set3FirstServePercentage = null;

    // Second serve — per set
    private Integer player1Set1SecondServesAttempted = null;
    private Integer player1Set1SecondServesMade = null;
    private Double player1Set1SecondServePercentage = null;
    private Integer player1Set2SecondServesAttempted = null;
    private Integer player1Set2SecondServesMade = null;
    private Double player1Set2SecondServePercentage = null;
    private Integer player1Set3SecondServesAttempted = null;
    private Integer player1Set3SecondServesMade = null;
    private Double player1Set3SecondServePercentage = null;
    private Integer player2Set1SecondServesAttempted = null;
    private Integer player2Set1SecondServesMade = null;
    private Double player2Set1SecondServePercentage = null;
    private Integer player2Set2SecondServesAttempted = null;
    private Integer player2Set2SecondServesMade = null;
    private Double player2Set2SecondServePercentage = null;
    private Integer player2Set3SecondServesAttempted = null;
    private Integer player2Set3SecondServesMade = null;
    private Double player2Set3SecondServePercentage = null;
}
