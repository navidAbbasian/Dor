package com.example.dor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.dor.models.GameMode;
import com.example.dor.utils.GameManager;
import com.example.dor.utils.SoundManager;

public class ModeActivity extends AppCompatActivity {

    private CardView quickModeCard;
    private CardView proModeCard;
    private GameManager gameManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        gameManager = GameManager.getInstance();

        quickModeCard = findViewById(R.id.quickModeCard);
        proModeCard = findViewById(R.id.proModeCard);

        quickModeCard.setOnClickListener(v -> selectMode(GameMode.QUICK));
        proModeCard.setOnClickListener(v -> selectMode(GameMode.PROFESSIONAL));
    }

    private void selectMode(GameMode mode) {
        gameManager.setGameMode(mode);

        // Stop background music before starting game
        SoundManager.getInstance().stopBackgroundMusic();

        // Start game
        Intent intent = new Intent(this, GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
