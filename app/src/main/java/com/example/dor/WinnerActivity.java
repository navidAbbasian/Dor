package com.example.dor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.dor.utils.GameManager;
import com.example.dor.utils.SoundManager;

public class WinnerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);

        // Get winner info from intent
        String winnerColor = getIntent().getStringExtra("winnerColor");
        String player1 = getIntent().getStringExtra("winnerPlayer1");
        String player2 = getIntent().getStringExtra("winnerPlayer2");

        // Set winner card color
        CardView winnerCard = findViewById(R.id.winnerCard);
        if (winnerColor != null) {
            winnerCard.setCardBackgroundColor(Color.parseColor(winnerColor));
        }

        // Set player names
        TextView player1Name = findViewById(R.id.player1Name);
        TextView player2Name = findViewById(R.id.player2Name);

        if (player1 != null && !player1.isEmpty()) {
            player1Name.setText(player1);
        }
        if (player2 != null && !player2.isEmpty()) {
            player2Name.setText(player2);
        }

        // Play Again button
        findViewById(R.id.playAgainButton).setOnClickListener(v -> {
            // Reset game and go to setup
            GameManager.getInstance().reset();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Main Menu button
        findViewById(R.id.mainMenuButton).setOnClickListener(v -> {
            GameManager.getInstance().reset();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Start background music again
        SoundManager.getInstance().startBackgroundMusic();
    }
}
