package com.example.dor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dor.models.GameMode;
import com.example.dor.utils.GameManager;
import com.example.dor.views.CircularPlayerLayout;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    private int playerCount;
    private CircularPlayerLayout circularPlayerLayout;
    private List<EditText> playerInputs;
    private GameManager gameManager;

    // Team colors - Bright & Fun
    private static final String[] TEAM_COLORS = {
            "#FF6B6B", // Red
            "#4ECDC4", // Teal
            "#7ED957", // Green
            "#FFD93D", // Yellow
            "#C792EA"  // Purple
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        playerCount = getIntent().getIntExtra("playerCount", 4);
        circularPlayerLayout = findViewById(R.id.circularPlayerLayout);
        playerInputs = new ArrayList<>();
        gameManager = GameManager.getInstance();

        // Initialize game with default mode (will be set later)
        gameManager.initializeGame(playerCount, GameMode.QUICK);

        // Add player input cards
        setupPlayerInputs();

        findViewById(R.id.nextButton).setOnClickListener(v -> onNextClicked());
    }

    private void setupPlayerInputs() {
        // The first child (center table) is already in the XML
        // Just add player input cards for each player

        float density = getResources().getDisplayMetrics().density;
        int teamCount = playerCount / 2;

        // Card dimensions based on player count
        int cardWidth, cardHeight;
        if (playerCount <= 4) {
            cardWidth = (int) (95 * density);
            cardHeight = (int) (48 * density);
        } else if (playerCount <= 6) {
            cardWidth = (int) (85 * density);
            cardHeight = (int) (44 * density);
        } else {
            cardWidth = (int) (75 * density);
            cardHeight = (int) (40 * density);
        }

        // Add player cards
        for (int i = 0; i < playerCount; i++) {
            int teamIndex = i % teamCount;
            EditText editText = createPlayerInput(i, teamIndex);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            editText.setLayoutParams(params);

            circularPlayerLayout.addView(editText);
            playerInputs.add(editText);
        }

        android.util.Log.d("SetupActivity", "Added " + playerInputs.size() + " player inputs for " + playerCount + " players");
    }

    private EditText createPlayerInput(int playerIndex, int teamIndex) {
        EditText editText = new EditText(this);
        editText.setHint(getString(R.string.player_hint) + " " + (playerIndex + 1));
        editText.setHintTextColor(Color.parseColor("#80FFFFFF"));
        editText.setTextColor(Color.WHITE);
        editText.setTextSize(11);
        editText.setGravity(Gravity.CENTER);
        editText.setBackground(getDrawable(R.drawable.player_card_background));
        editText.setPadding(8, 8, 8, 8);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editText.setSingleLine(true);

        // Set background color based on team
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
