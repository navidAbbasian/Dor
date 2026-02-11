package com.example.dor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.dor.models.GameEvent;
import com.example.dor.models.GameMode;
import com.example.dor.models.Team;
import com.example.dor.models.Word;
import com.example.dor.utils.GameManager;
import com.example.dor.utils.SoundManager;
import com.example.dor.views.CircularPlayerLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private GameManager gameManager;
    private SoundManager soundManager;

    // UI Elements
    private LinearLayout teamTimersContainer;
    private TextView bombTimerText;
    private CardView centerTableCard;
    private TextView playerNameText;
    private TextView tapToStartText;
    private TextView wordText;
    private TextView nextTurnText;
    private View explosionOverlay;
    private com.google.android.material.button.MaterialButton skipButton;
    private CircularPlayerLayout circularGameLayout;
    private ImageButton pauseButton;

    // Team timer TextViews
    private List<TextView> teamTimerViews;

    // Player indicator views around the circle
    private List<View> playerIndicatorViews;
    private List<String> circularPlayerOrder;

    // Timers
    private CountDownTimer bombTimer;
    private CountDownTimer teamTimer;
    private CountDownTimer skipCooldownTimer;

    // State
    private boolean isPlaying = false;
    private boolean canSkip = false;
    private boolean isPaused = false;
    private long bombTimeRemaining;
    private long lastTeamTimerUpdate;
    private AlertDialog pauseDialog;
    private AlertDialog eliminatedDialog;

    // Tracking for game events
    private long wordStartTime;
    private String currentWordText;
    private TextView penaltyText;

    // Handler for tick sound
    private Handler tickHandler;
    private Runnable tickRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameManager = GameManager.getInstance();
        soundManager = SoundManager.getInstance();

        initViews();
        setupTeamTimers();
        setupPlayerIndicators();

        showCurrentPlayerTurn();
    }

    private void initViews() {
        teamTimersContainer = findViewById(R.id.teamTimersContainer);
        bombTimerText = findViewById(R.id.bombTimerText);
        centerTableCard = findViewById(R.id.centerTableCard);
        playerNameText = findViewById(R.id.playerNameText);
        tapToStartText = findViewById(R.id.tapToStartText);
        wordText = findViewById(R.id.wordText);
        nextTurnText = findViewById(R.id.nextTurnText);
        explosionOverlay = findViewById(R.id.explosionOverlay);
        penaltyText = findViewById(R.id.penaltyText);
        skipButton = findViewById(R.id.skipButton);
        circularGameLayout = findViewById(R.id.circularGameLayout);
        pauseButton = findViewById(R.id.pauseButton);

        teamTimerViews = new ArrayList<>();
        playerIndicatorViews = new ArrayList<>();
        circularPlayerOrder = gameManager.getCircularPlayerOrder();

        // Center table click - start turn or next word
        centerTableCard.setOnClickListener(v -> onCenterTableClicked());

        // Skip button
        skipButton.setOnClickListener(v -> onSkipClicked());

        // Pause button
        pauseButton.setOnClickListener(v -> showPauseDialog());

        tickHandler = new Handler(Looper.getMainLooper());

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                stopTimers();
                stopTickSound();
                gameManager.reset();
                finish();
            }
        });
    }

    private void setupTeamTimers() {
        teamTimersContainer.removeAllViews();
        teamTimerViews.clear();

        List<Team> teams = gameManager.getTeams();
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);

            TextView timerView = new TextView(this);
            timerView.setTextSize(18);
            timerView.setTextColor(Color.WHITE);
            timerView.setPadding((int)(16 * density), (int)(8 * density),
                                  (int)(16 * density), (int)(8 * density));
            timerView.setGravity(Gravity.CENTER);

            // Set background with team color
            timerView.setBackgroundColor(Color.parseColor(team.getColor()));
            timerView.setText(formatTime(team.getRemainingTimeMillis()));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins((int)(4 * density), 0, (int)(4 * density), 0);
            timerView.setLayoutParams(params);

            teamTimersContainer.addView(timerView);
            teamTimerViews.add(timerView);
        }

        updateTeamTimerHighlight();
    }

    private void updateTeamTimerHighlight() {
        List<Team> teams = gameManager.getTeams();
        for (int i = 0; i < teamTimerViews.size(); i++) {
            TextView timerView = teamTimerViews.get(i);
            Team team = teams.get(i);

            if (team.isEliminated()) {
                timerView.setAlpha(0.3f);
                timerView.setText("حذف");
            } else {
                timerView.setText(formatTime(team.getRemainingTimeMillis()));

                if (i == gameManager.getCurrentTeamIndex()) {
                    timerView.setAlpha(1f);
                    timerView.setScaleX(1.2f);
                    timerView.setScaleY(1.2f);
                } else {
                    timerView.setAlpha(0.7f);
                    timerView.setScaleX(1f);
                    timerView.setScaleY(1f);
                }
            }
        }
    }

    private void setupPlayerIndicators() {
        // Remove all except the first child (center card)
        while (circularGameLayout.getChildCount() > 1) {
            circularGameLayout.removeViewAt(1);
        }
        playerIndicatorViews.clear();

        // Refresh the circular player order from game manager
        circularPlayerOrder = gameManager.getCircularPlayerOrder();

        float density = getResources().getDisplayMetrics().density;
        int playerCount = circularPlayerOrder.size();

        android.util.Log.d("GameActivity", "Setting up " + playerCount + " player indicators");

        if (playerCount == 0) {
            android.util.Log.e("GameActivity", "No players found in circularPlayerOrder!");
            return;
        }

        // Calculate indicator size based on player count - 40% larger than original
        int indicatorSize;
        int dotSize;
        float textSize;
        if (playerCount <= 4) {
            indicatorSize = (int) (100 * density);  // 70 * 1.44
            dotSize = (int) (26 * density);         // 18 * 1.44
            textSize = 16;                           // 11 * 1.44
        } else if (playerCount <= 6) {
            indicatorSize = (int) (94 * density);   // 65 * 1.44
            dotSize = (int) (23 * density);         // 16 * 1.44
            textSize = 14;                           // 10 * 1.44
        } else {
            indicatorSize = (int) (79 * density);   // 55 * 1.44
            dotSize = (int) (20 * density);         // 14 * 1.44
            textSize = 13;                           // 9 * 1.44
        }

        for (int i = 0; i < playerCount; i++) {
            String playerName = circularPlayerOrder.get(i);
            String teamColor = gameManager.getTeamColorByPlayerName(playerName);

            // Create a container for dot + name
            LinearLayout playerContainer = new LinearLayout(this);
            playerContainer.setOrientation(LinearLayout.VERTICAL);
            playerContainer.setGravity(Gravity.CENTER);

            // Create dot/circle indicator
            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
            dotParams.gravity = Gravity.CENTER;
            dot.setLayoutParams(dotParams);
            dot.setBackground(getDrawable(R.drawable.player_dot));
            dot.getBackground().setTint(Color.parseColor(teamColor));

            // Create name text
            TextView nameText = new TextView(this);
            nameText.setText(truncateName(playerName));
            nameText.setTextSize(textSize);
            nameText.setTextColor(Color.parseColor(teamColor));
            nameText.setGravity(Gravity.CENTER);
            nameText.setMaxLines(1);

            playerContainer.addView(dot);
            playerContainer.addView(nameText);

            // Set layout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(indicatorSize, indicatorSize);
            playerContainer.setLayoutParams(params);

            circularGameLayout.addView(playerContainer);
            playerIndicatorViews.add(playerContainer);
        }

        // Initial rotation without animation - put first player at bottom
        String currentPlayerName = gameManager.getCurrentPlayerName();
        for (int i = 0; i < circularPlayerOrder.size(); i++) {
            if (circularPlayerOrder.get(i).equals(currentPlayerName)) {
                circularGameLayout.rotateToPlayer(i, false);
                break;
            }
        }

        updateCurrentPlayerHighlight();
        android.util.Log.d("GameActivity", "Created " + playerIndicatorViews.size() + " player indicators");
    }

    private String truncateName(String name) {
        if (name.length() > 24) {
            return name.substring(0, 23) + "..";
        }
        return name;
    }

    private void updateCurrentPlayerHighlight() {
        String currentPlayerName = gameManager.getCurrentPlayerName();
        int currentPlayerIndex = -1;

        // First, stop ALL animations and reset ALL players to default state
        for (int i = 0; i < playerIndicatorViews.size(); i++) {
            View container = playerIndicatorViews.get(i);

            // Find current player index
            if (circularPlayerOrder.get(i).equals(currentPlayerName)) {
                currentPlayerIndex = i;
            }

            // Stop any existing animation first
            Object tag = container.getTag(R.id.playerNameText);
            if (tag instanceof ObjectAnimator[]) {
                for (ObjectAnimator anim : (ObjectAnimator[]) tag) {
                    anim.cancel();
                }
                container.setTag(R.id.playerNameText, null);
            }

            // Reset all properties immediately - all players at normal size
            container.setScaleX(1f);
            container.setScaleY(1f);
            container.setAlpha(0.6f);  // Slightly more visible
            container.setBackground(null);
            container.setPadding(0, 0, 0, 0);

            // Reset dot and text
            LinearLayout playerContainer = (LinearLayout) container;
            View dot = playerContainer.getChildAt(0);
            TextView nameText = (TextView) playerContainer.getChildAt(1);
            String playerName = circularPlayerOrder.get(i);
            String teamColor = gameManager.getTeamColorByPlayerName(playerName);

            dot.setBackground(getDrawable(R.drawable.player_dot));
            dot.getBackground().setTint(Color.parseColor(teamColor));
            nameText.setTextColor(Color.parseColor(teamColor));
            nameText.setTypeface(null, android.graphics.Typeface.NORMAL);
            nameText.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        }

        // Rotate the circular layout to put current player at bottom
        if (currentPlayerIndex >= 0) {
            circularGameLayout.rotateToPlayer(currentPlayerIndex, true);
        }

        // Now highlight only the current player
        for (int i = 0; i < playerIndicatorViews.size(); i++) {
            String playerName = circularPlayerOrder.get(i);
            if (playerName.equals(currentPlayerName)) {
                View container = playerIndicatorViews.get(i);
                LinearLayout playerContainer = (LinearLayout) container;
                View dot = playerContainer.getChildAt(0);
                TextView nameText = (TextView) playerContainer.getChildAt(1);
                String teamColor = gameManager.getTeamColorByPlayerName(playerName);

                // Current player: full opacity and 20% larger
                container.setAlpha(1f);
                container.setScaleX(1.2f);
                container.setScaleY(1.2f);

                // Keep normal dot style, just full color
                dot.setBackground(getDrawable(R.drawable.player_dot));
                dot.getBackground().setTint(Color.parseColor(teamColor));

                // Make name text same color but bold
                nameText.setTextColor(Color.parseColor(teamColor));
                nameText.setTypeface(nameText.getTypeface(), android.graphics.Typeface.BOLD);


                break; // Only one current player
            }
        }
    }

    private void showCurrentPlayerTurn() {
        isPlaying = false;
        canSkip = false;
        skipButton.setEnabled(false);

        String playerName = gameManager.getCurrentPlayerName();
        playerNameText.setText(playerName);
        playerNameText.setVisibility(View.VISIBLE);
        tapToStartText.setVisibility(View.VISIBLE);
        wordText.setVisibility(View.GONE);

        // Set card border color to current team color
        Team currentTeam = gameManager.getCurrentTeam();
        if (currentTeam != null) {
            centerTableCard.setCardBackgroundColor(Color.parseColor("#0F3460"));
        }

        // Update player highlight around the circle
        updateCurrentPlayerHighlight();

        // Show next player info
        updateNextTurnText();

        // Reset bomb timer display
        GameMode mode = gameManager.getGameMode();
        bombTimeRemaining = mode.getBombTimeMillis();
        updateBombTimerDisplay();
    }

    private void updateNextTurnText() {
        String nextPlayerName = gameManager.getNextPlayerName();
        if (!nextPlayerName.isEmpty()) {
            nextTurnText.setText("نوبت بعدی: " + nextPlayerName);
        } else {
            nextTurnText.setText("");
        }
    }

    private void onCenterTableClicked() {
        if (!isPlaying) {
            // Start the turn
            startTurn();
        } else {
            // Word guessed correctly - move to next team
            onWordGuessed();
        }
    }

    private void startTurn() {
        isPlaying = true;

        // Hide player name, show word
        playerNameText.setVisibility(View.GONE);
        tapToStartText.setVisibility(View.GONE);
        wordText.setVisibility(View.VISIBLE);

        // Get and show first word
        Word word = gameManager.nextWord();
        if (word != null) {
            wordText.setText(word.getText());
            currentWordText = word.getText();
        } else {
            wordText.setText("کلمه‌ای نیست!");
            currentWordText = "";
        }
        wordStartTime = System.currentTimeMillis();

        // Start bomb timer
        startBombTimer();

        // Start team timer
        startTeamTimer();

        // Start skip cooldown
        startSkipCooldown();

        // Start tick sound
        startTickSound();
    }

    private void startBombTimer() {
        GameMode mode = gameManager.getGameMode();
        bombTimeRemaining = mode.getBombTimeMillis();

        bombTimer = new CountDownTimer(bombTimeRemaining, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                bombTimeRemaining = millisUntilFinished;
                updateBombTimerDisplay();

                // Start intense mode in last 10 seconds
                int seconds = (int) (millisUntilFinished / 1000);
                if (seconds <= 10 && !soundManager.isIntenseMode()) {
                    soundManager.setIntenseMode(true);
                }
            }

            @Override
            public void onFinish() {
                onBombExploded();
            }
        }.start();
    }

    private void startTeamTimer() {
        Team currentTeam = gameManager.getCurrentTeam();
        if (currentTeam == null) return;

        lastTeamTimerUpdate = System.currentTimeMillis();

        teamTimer = new CountDownTimer(currentTeam.getRemainingTimeMillis(), 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Only update team time if actually playing (prevents updates during waiting for player click)
                if (!isPlaying) return;

                Team team = gameManager.getCurrentTeam();
                if (team != null) {
                    long elapsed = System.currentTimeMillis() - lastTeamTimerUpdate;
                    lastTeamTimerUpdate = System.currentTimeMillis();
                    team.subtractTime(elapsed);
                    updateTeamTimerDisplay(gameManager.getCurrentTeamIndex());

                    if (team.getRemainingTimeMillis() <= 0) {
                        cancel();
                        onTeamEliminated();
                    }
                }
            }

            @Override
            public void onFinish() {
                onTeamEliminated();
            }
        }.start();
    }

    private void startSkipCooldown() {
        canSkip = false;
        skipButton.setEnabled(false);
        skipButton.setText("صبر کنید...");

        GameMode mode = gameManager.getGameMode();

        skipCooldownTimer = new CountDownTimer(mode.getSkipCooldownMillis(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                skipButton.setText("رد کردن (" + (millisUntilFinished / 1000) + ")");
            }

            @Override
            public void onFinish() {
                canSkip = true;
                skipButton.setEnabled(true);
                skipButton.setText(R.string.skip_word);
            }
        }.start();
    }

    private void startTickSound() {
        tickRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    soundManager.playTick();
                    tickHandler.postDelayed(this, 1000);
                }
            }
        };
        tickHandler.post(tickRunnable);
    }

    private void stopTickSound() {
        if (tickHandler != null && tickRunnable != null) {
            tickHandler.removeCallbacks(tickRunnable);
        }
        // Stop intense mode
        soundManager.setIntenseMode(false);
    }

    private void updateBombTimerDisplay() {
        int seconds = (int) (bombTimeRemaining / 1000);
        bombTimerText.setText("💣 " + seconds);

        // Change color based on time remaining
        if (seconds <= 5) {
            bombTimerText.setTextColor(Color.parseColor("#F44336")); // Red
        } else if (seconds <= 10) {
            bombTimerText.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else {
            bombTimerText.setTextColor(Color.parseColor("#FDD835")); // Yellow
        }
    }

    private void updateTeamTimerDisplay(int teamIndex) {
        if (teamIndex >= 0 && teamIndex < teamTimerViews.size()) {
            Team team = gameManager.getTeams().get(teamIndex);
            TextView timerView = teamTimerViews.get(teamIndex);
            timerView.setText(formatTime(team.getRemainingTimeMillis()));
        }
    }

    private void onWordGuessed() {
        // Play word correct sound
        soundManager.playWordCorrect();
        soundManager.vibrateShort();

        // Record the word event for current team before moving to next
        Team currentTeam = gameManager.getCurrentTeam();
        if (currentTeam != null && currentWordText != null && !currentWordText.isEmpty()) {
            long timeSpent = System.currentTimeMillis() - wordStartTime;
            GameEvent event = new GameEvent(GameEvent.EventType.WORD_GUESSED, currentWordText, timeSpent, 0);
            currentTeam.addGameEvent(event);
        }

        // Stop only team timer, bomb timer continues!
        if (teamTimer != null) {
            teamTimer.cancel();
        }
        if (skipCooldownTimer != null) {
            skipCooldownTimer.cancel();
        }

        // Play next turn sound
        soundManager.playNextTurn();

        // Move to next team
        gameManager.moveToNextTeam();

        // Check if game is over
        if (gameManager.isGameOver()) {
            stopTimers();
            stopTickSound();
            showWinner();
            return;
        }

        // Update highlights
        updateTeamTimerHighlight();
        updateCurrentPlayerHighlight();

        // Show new word immediately (no need to show player name mid-round)
        Word word = gameManager.nextWord();
        if (word != null) {
            wordText.setText(word.getText());
            currentWordText = word.getText();
        } else {
            wordText.setText("کلمه‌ای نیست!");
            currentWordText = "";
        }
        wordStartTime = System.currentTimeMillis();

        // Start team timer for new team
        startTeamTimer();

        // Restart skip cooldown
        startSkipCooldown();

        // Update next turn text
        updateNextTurnText();
    }

    private void onSkipClicked() {
        if (!canSkip || !isPlaying) return;

        // Get new word
        Word word = gameManager.skipWord();
        if (word != null) {
            wordText.setText(word.getText());
        }

        // Restart skip cooldown
        if (skipCooldownTimer != null) {
            skipCooldownTimer.cancel();
        }
        startSkipCooldown();
    }

    private void onBombExploded() {
        // Get penalty amount for display
        GameMode mode = gameManager.getGameMode();
        long penaltyMillis = mode.getPenaltyMillis();
        int penaltySeconds = (int) (penaltyMillis / 1000);

        // Update penalty text before showing explosion
        if (penaltyText != null) {
            penaltyText.setText("-" + penaltySeconds + " ثانیه");
        }

        // Play explosion effects
        soundManager.playExplosion();
        soundManager.vibrate();
        showExplosionEffect();

        // Record bomb event for current team before applying penalty
        Team currentTeam = gameManager.getCurrentTeam();
        if (currentTeam != null && currentWordText != null && !currentWordText.isEmpty()) {
            long timeSpent = System.currentTimeMillis() - wordStartTime;
            GameEvent event = new GameEvent(GameEvent.EventType.BOMB_EXPLODED, currentWordText, timeSpent, penaltyMillis);
            currentTeam.addGameEvent(event);
        }

        stopTimers();
        stopTickSound();

        // Apply penalty to current team
        gameManager.onBombExploded();

        // Check if current team is eliminated
        currentTeam = gameManager.getCurrentTeam();
        if (currentTeam != null && currentTeam.isEliminated()) {
            // Store eliminated team info before moving to next
            Team eliminatedTeam = currentTeam;

            // Move to next team only if current team is eliminated
            gameManager.moveToNextTeam();

            // Check if game is over
            if (gameManager.isGameOver()) {
                // Only show winner - no elimination sound/dialog needed
                new Handler(Looper.getMainLooper()).postDelayed(this::showWinner, 1500);
                return;
            }

            // Game continues - show eliminated dialog with sound
            // Play team eliminated sound
            soundManager.playTeamEliminated();
            // Update UI
            updateTeamTimerHighlight();
            // Show eliminated dialog
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showTeamEliminatedDialog(eliminatedTeam);
            }, 1500);
            return;
        }
        // If team is not eliminated, same player continues in new round

        // Check if game is over
        if (gameManager.isGameOver()) {
            // Delay to show explosion effect
            new Handler(Looper.getMainLooper()).postDelayed(this::showWinner, 1500);
            return;
        }

        // Update UI
        updateTeamTimerHighlight();

        // Delay before showing next turn (same player starts new round)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showCurrentPlayerTurn();
        }, 1500);
    }

    private void onTeamEliminated() {
        Team currentTeam = gameManager.getCurrentTeam();

        if (currentTeam != null) {
            currentTeam.setEliminated(true);
        }

        // Store eliminated team info before moving to next
        Team eliminatedTeam = currentTeam;

        stopTimers();
        stopTickSound();

        // Move to next team
        gameManager.moveToNextTeam();

        // Check if game is over
        if (gameManager.isGameOver()) {
            // Only show winner - no elimination sound/dialog needed
            showWinner();
            return;
        }

        // Game continues - show eliminated dialog with sound
        // Play team eliminated sound
        soundManager.playTeamEliminated();
        // Update UI
        updateTeamTimerHighlight();
        // Show eliminated dialog
        showTeamEliminatedDialog(eliminatedTeam);
    }

    private void showExplosionEffect() {
        explosionOverlay.setVisibility(View.VISIBLE);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(explosionOverlay, "alpha", 0f, 0.8f);
        fadeIn.setDuration(200);
        fadeIn.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(explosionOverlay, "alpha", 0.8f, 0f);
        fadeOut.setDuration(800);
        fadeOut.setStartDelay(400);

        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeOut.start();
            }
        });

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                explosionOverlay.setVisibility(View.GONE);
            }
        });

        fadeIn.start();
    }

    private void stopTimers() {
        if (bombTimer != null) {
            bombTimer.cancel();
        }
        if (teamTimer != null) {
            teamTimer.cancel();
        }
        if (skipCooldownTimer != null) {
            skipCooldownTimer.cancel();
        }
    }

    private void showWinner() {
        Team winner = gameManager.getWinner();
        Intent intent = new Intent(this, WinnerActivity.class);
        if (winner != null) {
            intent.putExtra("winnerColor", winner.getColor());
            intent.putExtra("winnerPlayer1", winner.getPlayer(0) != null ?
                    winner.getPlayer(0).getName() : "");
            intent.putExtra("winnerPlayer2", winner.getPlayer(1) != null ?
                    winner.getPlayer(1).getName() : "");
        }
        startActivity(intent);
        finish();
    }

    private void showTeamEliminatedDialog(Team eliminatedTeam) {
        if (eliminatedTeam == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_team_eliminated, null);

        // Set eliminated team card color
        androidx.cardview.widget.CardView teamCard = dialogView.findViewById(R.id.eliminatedTeamCard);
        if (teamCard != null && eliminatedTeam.getColor() != null) {
            teamCard.setCardBackgroundColor(Color.parseColor(eliminatedTeam.getColor()));
        }

        // Set player names
        TextView player1Name = dialogView.findViewById(R.id.eliminatedPlayer1Name);
        TextView player2Name = dialogView.findViewById(R.id.eliminatedPlayer2Name);

        if (player1Name != null && eliminatedTeam.getPlayer(0) != null) {
            player1Name.setText(eliminatedTeam.getPlayer(0).getName());
        }
        if (player2Name != null && eliminatedTeam.getPlayer(1) != null) {
            player2Name.setText(eliminatedTeam.getPlayer(1).getName());
        }

        // Populate game events list
        LinearLayout eventsContainer = dialogView.findViewById(R.id.gameEventsContainer);
        if (eventsContainer != null) {
            eventsContainer.removeAllViews();
            List<GameEvent> events = eliminatedTeam.getGameEvents();

            if (events.isEmpty()) {
                // No events recorded
                TextView noEventsText = new TextView(this);
                noEventsText.setText("هیچ رویدادی ثبت نشده");
                noEventsText.setTextColor(Color.parseColor("#AAAAAA"));
                noEventsText.setGravity(Gravity.CENTER);
                eventsContainer.addView(noEventsText);
            } else {
                float density = getResources().getDisplayMetrics().density;

                for (int i = 0; i < events.size(); i++) {
                    GameEvent event = events.get(i);

                    LinearLayout eventRow = new LinearLayout(this);
                    eventRow.setOrientation(LinearLayout.HORIZONTAL);
                    eventRow.setPadding(0, (int)(4 * density), 0, (int)(4 * density));
                    eventRow.setGravity(Gravity.CENTER_VERTICAL);

                    // Event icon
                    TextView iconText = new TextView(this);
                    if (event.getType() == GameEvent.EventType.WORD_GUESSED) {
                        iconText.setText("✅");
                    } else {
                        iconText.setText("💥");
                    }
                    iconText.setTextSize(16);
                    iconText.setPadding(0, 0, (int)(8 * density), 0);

                    // Word text
                    TextView wordTextView = new TextView(this);
                    wordTextView.setText(event.getWordText());
                    wordTextView.setTextColor(Color.parseColor("#1A1A2E"));
                    wordTextView.setTextSize(14);
                    LinearLayout.LayoutParams wordParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    wordTextView.setLayoutParams(wordParams);

                    // Time spent
                    TextView timeText = new TextView(this);
                    int timeSeconds = (int) (event.getTimeSpentMillis() / 1000);
                    timeText.setText(timeSeconds + " ث");
                    timeText.setTextColor(Color.parseColor("#AAAAAA"));
                    timeText.setTextSize(12);
                    timeText.setPadding((int)(8 * density), 0, 0, 0);

                    eventRow.addView(iconText);
                    eventRow.addView(wordTextView);
                    eventRow.addView(timeText);

                    // If bomb exploded, add penalty info
                    if (event.getType() == GameEvent.EventType.BOMB_EXPLODED) {
                        TextView penaltyTextView = new TextView(this);
                        int penaltySec = (int) (event.getPenaltyMillis() / 1000);
                        penaltyTextView.setText(" (-" + penaltySec + "ث)");
                        penaltyTextView.setTextColor(Color.parseColor("#F44336"));
                        penaltyTextView.setTextSize(12);
                        eventRow.addView(penaltyTextView);
                    }

                    eventsContainer.addView(eventRow);
                }
            }
        }

        eliminatedDialog = new AlertDialog.Builder(this, R.style.PauseDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.continueButton).setOnClickListener(v -> {
            eliminatedDialog.dismiss();
            // Continue to next player's turn
            showCurrentPlayerTurn();
        });

        eliminatedDialog.show();
    }

    private String formatTime(long millis) {
        if (millis < 0) millis = 0;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void showPauseDialog() {
        // Only pause if playing
        if (isPlaying) {
            isPaused = true;
            stopTimers();
            stopTickSound();
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pause, null);

        pauseDialog = new AlertDialog.Builder(this, R.style.PauseDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.resumeButton).setOnClickListener(v -> {
            pauseDialog.dismiss();
            resumeGame();
        });

        dialogView.findViewById(R.id.endGameButton).setOnClickListener(v -> {
            pauseDialog.dismiss();
            stopTimers();
            stopTickSound();
            gameManager.reset();
            // Start background music and go to MainActivity
            soundManager.startBackgroundMusic();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        pauseDialog.show();
    }

    private void resumeGame() {
        isPaused = false;
        if (isPlaying) {
            // Resume timers
            startBombTimerFromRemaining();
            startTeamTimer();
            startTickSound();
        }
    }

    private void startBombTimerFromRemaining() {
        bombTimer = new CountDownTimer(bombTimeRemaining, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                bombTimeRemaining = millisUntilFinished;
                updateBombTimerDisplay();

                // Start intense mode in last 10 seconds
                int seconds = (int) (millisUntilFinished / 1000);
                if (seconds <= 10 && !soundManager.isIntenseMode()) {
                    soundManager.setIntenseMode(true);
                }
            }

            @Override
            public void onFinish() {
                onBombExploded();
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimers();
        stopTickSound();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimers();
        stopTickSound();
    }
}
