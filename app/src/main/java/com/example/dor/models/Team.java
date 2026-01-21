package com.example.dor.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a team in the game
 */
public class Team {
    private int id;
    private String color;
    private List<Player> players;
    private long remainingTimeMillis;
    private boolean eliminated;

    public Team(int id, String color, long initialTimeMillis) {
        this.id = id;
        this.color = color;
        this.players = new ArrayList<>();
        this.remainingTimeMillis = initialTimeMillis;
        this.eliminated = false;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public long getRemainingTimeMillis() {
        return remainingTimeMillis;
    }

    public void setRemainingTimeMillis(long remainingTimeMillis) {
        this.remainingTimeMillis = remainingTimeMillis;
    }

    public void subtractTime(long millis) {
        this.remainingTimeMillis -= millis;
        if (this.remainingTimeMillis < 0) {
            this.remainingTimeMillis = 0;
        }
    }

    public void applyPenalty(long penaltyMillis) {
        subtractTime(penaltyMillis);
    }

    public boolean isEliminated() {
        return eliminated || remainingTimeMillis <= 0;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public Player getPlayer(int index) {
        if (index >= 0 && index < players.size()) {
            return players.get(index);
        }
        return null;
    }
}
