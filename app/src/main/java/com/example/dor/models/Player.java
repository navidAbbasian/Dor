package com.example.dor.models;

/**
 * Model class representing a player in the game
 */
public class Player {
    private String name;
    private int teamId;

    public Player(String name, int teamId) {
        this.name = name;
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
}
