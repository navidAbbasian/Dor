package com.example.dor.models;

/**
 * Model class representing a game event (word guessed or bomb exploded)
 */
public class GameEvent {

    public enum EventType {
        WORD_GUESSED,
        BOMB_EXPLODED
    }

    private EventType type;
    private String wordText;
    private long timeSpentMillis; // Time spent on this word
    private long penaltyMillis;   // Penalty applied (only for bomb explosion)

    public GameEvent(EventType type, String wordText, long timeSpentMillis, long penaltyMillis) {
        this.type = type;
        this.wordText = wordText;
        this.timeSpentMillis = timeSpentMillis;
        this.penaltyMillis = penaltyMillis;
    }

    public EventType getType() {
        return type;
    }

    public String getWordText() {
        return wordText;
    }

    public long getTimeSpentMillis() {
        return timeSpentMillis;
    }

    public long getPenaltyMillis() {
        return penaltyMillis;
    }
}
