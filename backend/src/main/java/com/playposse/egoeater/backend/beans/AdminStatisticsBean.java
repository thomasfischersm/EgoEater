package com.playposse.egoeater.backend.beans;

/**
 * A transport bean that contains statistics about the users. It is only for the eyes of admins.
 */
public class AdminStatisticsBean {

    private int totalUserCount;
    private int activeUserCount;
    private int conversationCount;
    private int matchesCount;
    private int ratingsCount;
    private long reportDuration;

    public AdminStatisticsBean() {
    }

    public AdminStatisticsBean(
            int totalUserCount,
            int activeUserCount,
            int conversationCount,
            int matchesCount,
            int ratingsCount,
            long reportDuration) {

        this.totalUserCount = totalUserCount;
        this.activeUserCount = activeUserCount;
        this.conversationCount = conversationCount;
        this.matchesCount = matchesCount;
        this.ratingsCount = ratingsCount;
        this.reportDuration = reportDuration;
    }

    public int getTotalUserCount() {
        return totalUserCount;
    }

    public int getActiveUserCount() {
        return activeUserCount;
    }

    public int getConversationCount() {
        return conversationCount;
    }

    public int getMatchesCount() {
        return matchesCount;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public long getReportDuration() {
        return reportDuration;
    }
}
