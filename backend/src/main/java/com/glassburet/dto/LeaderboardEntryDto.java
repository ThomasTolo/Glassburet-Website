package com.glassburet.dto;

public class LeaderboardEntryDto {

    private String memberName;
    private long score;

    public LeaderboardEntryDto(String memberName, long score) {
        this.memberName = memberName;
        this.score = score;
    }

    public String getMemberName() { return memberName; }
    public long getScore() { return score; }
}
