package com.glassburet.dto;

public class StatsDto {

    private long memberCount;
    private long quoteCount;
    private long activitiesThisWeek;

    public StatsDto(long memberCount, long quoteCount, long activitiesThisWeek) {
        this.memberCount = memberCount;
        this.quoteCount = quoteCount;
        this.activitiesThisWeek = activitiesThisWeek;
    }

    public long getMemberCount() { return memberCount; }
    public long getQuoteCount() { return quoteCount; }
    public long getActivitiesThisWeek() { return activitiesThisWeek; }
}
