package com.example.dor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dor.utils.GameManager;
import com.example.dor.utils.SoundManager;
import com.example.dor.data.WordRepository;

public class MainActivity extends AppCompatActivity {

    private GameManager gameManager;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize managers
        gameManager = GameManager.getInstance();
        soundManager = SoundManager.getInstance();
        soundManager.init(this);

        // Load word categories
        WordRepository.getInstance().loadCategories(this);

        // Start background music
        soundManager.startBackgroundMusic();

        // Setup button click listeners
        findViewById(R.id.btn4Players).setOnClickListener(v -> startSetup(4));
        findViewById(R.id.btn6Players).setOnClickListener(v -> startSetup(6));
        findViewById(R.id.btn8Players).setOnClickListener(v -> startSetup(8));
        findViewById(R.id.btn10Players).setOnClickListener(v -> startSetup(10));
    }

    private void startSetup(int playerCount) {
        Intent intent = new Intent(this, SetupActivity.class);
        intent.putExtra("playerCount", playerCount);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        soundManager.startBackgroundMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        soundManager.stopBackgroundMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
    }
}
