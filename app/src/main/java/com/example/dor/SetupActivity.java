package com.example.dor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dor.models.GameMode;
import com.example.dor.utils.GameManager;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    private int playerCount;
    private FrameLayout tableContainer;
    private List<EditText> playerInputs;
    private GameManager gameManager;

    // Team colors
    private static final String[] TEAM_COLORS = {
            "#E53935", // Red
            "#1E88E5", // Blue
            "#43A047", // Green
            "#FDD835", // Yellow
            "#8E24AA"  // Purple
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        playerCount = getIntent().getIntExtra("playerCount", 4);
        tableContainer = findViewById(R.id.tableContainer);
        playerInputs = new ArrayList<>();
        gameManager = GameManager.getInstance();

        // Initialize game with default mode (will be set later)
        gameManager.initializeGame(playerCount, GameMode.QUICK);

        // Wait for layout to be ready before adding player inputs
        tableContainer.post(this::setupPlayerInputs);

        findViewById(R.id.nextButton).setOnClickListener(v -> onNextClicked());
    }

    private void setupPlayerInputs() {
        int containerWidth = tableContainer.getWidth();
        int containerHeight = tableContainer.getHeight();
        int centerX = containerWidth / 2;
        int centerY = containerHeight / 2;

        // Calculate radius for player positions (slightly less than half container)
        int radius = (int) (Math.min(containerWidth, containerHeight) * 0.38);

        // Input field dimensions
        int inputWidth = 120;
        int inputHeight = 50;

        // Convert dp to pixels
        float density = getResources().getDisplayMetrics().density;
        int inputWidthPx = (int) (inputWidth * density);
        int inputHeightPx = (int) (inputHeight * density);

        int teamCount = playerCount / 2;

        // Create ordered list of player positions
        // Order: T1P1, T2P1, T3P1, T1P2, T2P2, T3P2 (alternating teams, sitting in circle)
        for (int i = 0; i < playerCount; i++) {
            // Calculate angle for this player position
            // Start from top (-90 degrees) and go clockwise
            double angle = Math.toRadians(-90 + (360.0 / playerCount) * i);

            int x = centerX + (int) (radius * Math.cos(angle)) - inputWidthPx / 2;
            int y = centerY + (int) (radius * Math.sin(angle)) - inputHeightPx / 2;

            // Determine team for this player (alternating pattern)
            int teamIndex = i % teamCount;

            EditText editText = createPlayerInput(i, teamIndex, inputWidthPx, inputHeightPx);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(inputWidthPx, inputHeightPx);
            params.leftMargin = x;
            params.topMargin = y;

            tableContainer.addView(editText, params);
            playerInputs.add(editText);
        }
    }

    private EditText createPlayerInput(int playerIndex, int teamIndex, int width, int height) {
        EditText editText = new EditText(this);
        editText.setHint(getString(R.string.player_hint) + " " + (playerIndex + 1));
        editText.setHintTextColor(Color.parseColor("#80FFFFFF"));
        editText.setTextColor(Color.WHITE);
        editText.setTextSize(12);
        editText.setGravity(Gravity.CENTER);
        editText.setBackground(getDrawable(R.drawable.player_input_background));
        editText.setPadding(8, 8, 8, 8);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editText.setSingleLine(true);

        // Set border color based on team
        editText.getBackground().setTint(Color.parseColor(TEAM_COLORS[teamIndex]));

        return editText;
    }

    private void onNextClicked() {
        List<String> playerNames = new ArrayList<>();

        for (int i = 0; i < playerInputs.size(); i++) {
            String name = playerInputs.get(i).getText().toString().trim();
            if (name.isEmpty()) {
                name = "بازیکن " + (i + 1);
            }
            playerNames.add(name);
        }

        // Set player names in game manager
        gameManager.setPlayerNames(playerNames);

        // Go to category selection
        Intent intent = new Intent(this, CategoryActivity.class);
        startActivity(intent);
    }
}
