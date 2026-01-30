package com.example.dor.utils;

import com.example.dor.data.WordRepository;
import com.example.dor.models.GameMode;
import com.example.dor.models.Player;
import com.example.dor.models.Team;
import com.example.dor.models.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the game state and logic
 */
public class GameManager {
    private static GameManager instance;

    private List<Team> teams;
    private GameMode gameMode;
    private int currentTeamIndex;
    private int currentPlayerIndexInTeam; // 0 or 1 (which player of each team is playing)
    private Word currentWord;
    private WordRepository wordRepository;

    // Team colors - Dark & Visible on white background
    private static final String[] TEAM_COLORS = {
            "#B71C1C", // Deep Red
            "#0D47A1", // Deep Blue
            "#1B5E20", // Deep Green
            "#E65100", // Deep Orange
            "#4A148C"  // Deep Purple
    };

    private GameManager() {
        teams = new ArrayList<>();
        wordRepository = WordRepository.getInstance();
    }

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void initializeGame(int playerCount, GameMode mode) {
        this.gameMode = mode;
        teams.clear();

        int teamCount = playerCount / 2;
        for (int i = 0; i < teamCount; i++) {
            Team team = new Team(i, TEAM_COLORS[i], mode.getTeamTimeMillis());
            teams.add(team);
        }

        currentTeamIndex = 0;
        currentPlayerIndexInTeam = 0;
    }

    public void setPlayerNames(List<String> playerNames) {
        // Players are arranged: Team1-P1, Team2-P1, Team3-P1, Team1-P2, Team2-P2, Team3-P2
        // So we need to distribute them correctly
        int teamCount = teams.size();

        // Clear existing players from all teams first
        for (Team team : teams) {
            team.getPlayers().clear();
        }

        for (int i = 0; i < playerNames.size(); i++) {
            int teamIndex = i % teamCount;
            Player player = new Player(playerNames.get(i), teamIndex);
            teams.get(teamIndex).addPlayer(player);
        }

        android.util.Log.d("GameManager", "Set " + playerNames.size() + " player names across " + teamCount + " teams");
    }

    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
        // Update team times
        for (Team team : teams) {
            team.setRemainingTimeMillis(mode.getTeamTimeMillis());
        }
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Team getCurrentTeam() {
        if (currentTeamIndex >= 0 && currentTeamIndex < teams.size()) {
            return teams.get(currentTeamIndex);
        }
        return null;
    }

    public Player getCurrentPlayer() {
        Team currentTeam = getCurrentTeam();
        if (currentTeam != null) {
            return currentTeam.getPlayer(currentPlayerIndexInTeam);
        }
        return null;
    }

    public String getCurrentPlayerName() {
        Player player = getCurrentPlayer();
        return player != null ? player.getName() : "";
    }

    /**
     * Get the name of the next player in turn order
     */
    public String getNextPlayerName() {
        // Find next non-eliminated team following the circular order
        int nextTeamIndex = currentTeamIndex;
        int nextPlayerIndex = currentPlayerIndexInTeam;
        int count = 0;
        int maxIterations = teams.size() * 2; // Maximum possible iterations

        do {
            nextTeamIndex = (nextTeamIndex + 1) % teams.size();

            // If we've gone through all teams, move to next player index
            if (nextTeamIndex == 0) {
                nextPlayerIndex = (nextPlayerIndex + 1) % 2;
            }
            count++;
        } while (teams.get(nextTeamIndex).isEliminated() && count < maxIterations);

        if (!teams.get(nextTeamIndex).isEliminated()) {
            Team nextTeam = teams.get(nextTeamIndex);
            Player nextPlayer = nextTeam.getPlayer(nextPlayerIndex);
            return nextPlayer != null ? nextPlayer.getName() : "";
        }
        return "";
    }

    public Word nextWord() {
        currentWord = wordRepository.getNextWord();
        return currentWord;
    }

    public Word skipWord() {
        currentWord = wordRepository.skipWord();
        return currentWord;
    }

    public Word getCurrentWord() {
        return currentWord;
    }

    public void moveToNextTeam() {
        // Find next non-eliminated team
        int startIndex = currentTeamIndex;
        int startPlayerIndex = currentPlayerIndexInTeam;

        do {
            currentTeamIndex = (currentTeamIndex + 1) % teams.size();

            // If we've gone through all teams, move to next player index
            if (currentTeamIndex == 0) {
                currentPlayerIndexInTeam = (currentPlayerIndexInTeam + 1) % 2;
            }
        } while (teams.get(currentTeamIndex).isEliminated() &&
                 !(currentTeamIndex == startIndex && currentPlayerIndexInTeam == startPlayerIndex));
    }

    public void onBombExploded() {
        Team currentTeam = getCurrentTeam();
        if (currentTeam != null) {
            currentTeam.applyPenalty(gameMode.getPenaltyMillis());

            // Check if team is eliminated
            if (currentTeam.isEliminated()) {
                currentTeam.setEliminated(true);
            }
        }
        // Don't move to next team - same player continues after explosion
    }

    public void onWordGuessed() {
        // Move to next team
        moveToNextTeam();
    }

    public void updateTeamTime(long elapsedMillis) {
        Team currentTeam = getCurrentTeam();
        if (currentTeam != null) {
            currentTeam.subtractTime(elapsedMillis);

            if (currentTeam.getRemainingTimeMillis() <= 0) {
                currentTeam.setEliminated(true);
            }
        }
    }

    public boolean isGameOver() {
        int activeTeams = 0;
        for (Team team : teams) {
            if (!team.isEliminated()) {
                activeTeams++;
            }
        }
        return activeTeams <= 1;
    }

    public Team getWinner() {
        for (Team team : teams) {
            if (!team.isEliminated()) {
                return team;
            }
        }
        return null;
    }

    public int getActiveTeamCount() {
        int count = 0;
        for (Team team : teams) {
            if (!team.isEliminated()) {
                count++;
            }
        }
        return count;
    }

    public void startNewRound() {
        currentPlayerIndexInTeam = (currentPlayerIndexInTeam + 1) % 2;
    }

    public void reset() {
        teams.clear();
        currentTeamIndex = 0;
        currentPlayerIndexInTeam = 0;
        currentWord = null;
        wordRepository.reset();
    }

    /**
     * Get ordered list of player names for circular seating arrangement
     * Order: T1P1, T2P1, T3P1, T1P2, T2P2, T3P2 (for 3 teams)
     */
    public List<String> getCircularPlayerOrder() {
        List<String> orderedPlayers = new ArrayList<>();
        int teamCount = teams.size();

        // First player of each team
        for (int t = 0; t < teamCount; t++) {
            Player p = teams.get(t).getPlayer(0);
            if (p != null) {
                orderedPlayers.add(p.getName());
            }
        }

        // Second player of each team
        for (int t = 0; t < teamCount; t++) {
            Player p = teams.get(t).getPlayer(1);
            if (p != null) {
                orderedPlayers.add(p.getName());
            }
        }

        return orderedPlayers;
    }

    /**
     * Get team color by player name
     */
    public String getTeamColorByPlayerName(String playerName) {
        for (Team team : teams) {
            for (Player player : team.getPlayers()) {
                if (player.getName().equals(playerName)) {
                    return team.getColor();
                }
            }
        }
        return TEAM_COLORS[0];
    }

    public int getCurrentTeamIndex() {
        return currentTeamIndex;
    }

    public void setCurrentTeamIndex(int index) {
        this.currentTeamIndex = index;
    }
}
