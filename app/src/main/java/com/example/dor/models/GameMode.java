package com.example.dor.models;

/**
 * Enum representing game modes
 */
public enum GameMode {
    QUICK(90000, 25000, 15000, 8000),      // 1:30 total, 25s bomb, 15s penalty, 8s skip cooldown
    PROFESSIONAL(165000, 40000, 15000, 8000); // 2:45 total, 40s bomb, 15s penalty, 8s skip cooldown

    private final long teamTimeMillis;
    private final long bombTimeMillis;
    private final long penaltyMillis;
    private final long skipCooldownMillis;

    GameMode(long teamTimeMillis, long bombTimeMillis, long penaltyMillis, long skipCooldownMillis) {
        this.teamTimeMillis = teamTimeMillis;
        this.bombTimeMillis = bombTimeMillis;
        this.penaltyMillis = penaltyMillis;
        this.skipCooldownMillis = skipCooldownMillis;
    }

    public long getTeamTimeMillis() {
        return teamTimeMillis;
    }

    public long getBombTimeMillis() {
        return bombTimeMillis;
    }

    public long getPenaltyMillis() {
        return penaltyMillis;
    }

    public long getSkipCooldownMillis() {
        return skipCooldownMillis;
    }
}
