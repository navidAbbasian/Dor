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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.dor.models.GameMode;
import com.example.dor.models.Team;
import com.example.dor.models.Word;
import com.example.dor.utils.GameManager;
import com.example.dor.utils.SoundManager;

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

    // Team timer TextViews
    private List<TextView> teamTimerViews;

    // Timers
    private CountDownTimer bombTimer;
    private CountDownTimer teamTimer;
    private CountDownTimer skipCooldownTimer;

    // State
    private boolean isPlaying = false;
    private boolean canSkip = false;
    private long bombTimeRemaining;
    private long lastTeamTimerUpdate;

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
        skipButton = findViewById(R.id.skipButton);

        teamTimerViews = new ArrayList<>();

        // Center table click - start turn or next word
        centerTableCard.setOnClickListener(v -> onCenterTableClicked());

        // Skip button
        skipButton.setOnClickListener(v -> onSkipClicked());

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

        // Show next player info
        updateNextTurnText();

        // Reset bomb timer display
        GameMode mode = gameManager.getGameMode();
        bombTimeRemaining = mode.getBombTimeMillis();
        updateBombTimerDisplay();
    }

    private void updateNextTurnText() {
        // Calculate next team
        int nextTeamIndex = (gameManager.getCurrentTeamIndex() + 1) % gameManager.getTeams().size();
        List<Team> teams = gameManager.getTeams();

        // Find next non-eliminated team
        int count = 0;
        while (teams.get(nextTeamIndex).isEliminated() && count < teams.size()) {
            nextTeamIndex = (nextTeamIndex + 1) % teams.size();
            count++;
        }

        if (!teams.get(nextTeamIndex).isEliminated()) {
            Team nextTeam = teams.get(nextTeamIndex);
            String nextPlayerName = nextTeam.getPlayer(0) != null ?
                    nextTeam.getPlayer(0).getName() : "بازیکن";
            nextTurnText.setText("نوبت بعدی: " + nextPlayerName);
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
        } else {
            wordText.setText("کلمه‌ای نیست!");
        }

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
        stopTimers();
        stopTickSound();

        // Move to next team
        gameManager.moveToNextTeam();

        // Check if game is over
        if (gameManager.isGameOver()) {
            showWinner();
            return;
        }

        // Show next player's turn
        updateTeamTimerHighlight();
        showCurrentPlayerTurn();
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
        // Play explosion effects
        soundManager.playExplosion();
        soundManager.vibrate();
        showExplosionEffect();

        stopTimers();
        stopTickSound();

        // Apply penalty
        gameManager.onBombExploded();

        // Check if current team is eliminated
        Team currentTeam = gameManager.getCurrentTeam();
        if (currentTeam != null && currentTeam.isEliminated()) {
            // Move to next team
            gameManager.moveToNextTeam();
        }

        // Check if game is over
        if (gameManager.isGameOver()) {
            // Delay to show explosion effect
            new Handler(Looper.getMainLooper()).postDelayed(this::showWinner, 1500);
            return;
        }

        // Update UI and continue with same player (or next if eliminated)
        updateTeamTimerHighlight();

        // Delay before showing next turn
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showCurrentPlayerTurn();
        }, 1500);
    }

    private void onTeamEliminated() {
        Team currentTeam = gameManager.getCurrentTeam();
        if (currentTeam != null) {
            currentTeam.setEliminated(true);
        }

        stopTimers();
        stopTickSound();

        // Move to next team
        gameManager.moveToNextTeam();

        // Check if game is over
        if (gameManager.isGameOver()) {
            showWinner();
            return;
        }

        // Update UI
        updateTeamTimerHighlight();
        showCurrentPlayerTurn();
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

    private String formatTime(long millis) {
        if (millis < 0) millis = 0;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
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
