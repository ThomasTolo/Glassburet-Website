package com.glassburet.dto;

public class LeaderboardEntryDto {

    private String memberName;
    private long totalScore;

    public LeaderboardEntryDto(String memberName, long totalScore) {
        this.memberName = memberName;
        this.totalScore = totalScore;
    }

    public String getMemberName() { return memberName; }
    public long getTotalScore() { return totalScore; }
}
