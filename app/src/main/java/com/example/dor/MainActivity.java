package com.example.dor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dor.data.WordRepository;
import com.example.dor.fragments.AddWordsFragment;
import com.example.dor.fragments.PlayFragment;
import com.example.dor.fragments.SettingsFragment;
import com.example.dor.utils.GameManager;
import com.example.dor.utils.SoundManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private GameManager gameManager;
    private SoundManager soundManager;
    private BottomNavigationView bottomNavigation;

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

        // Setup bottom navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_add_words) {
                selectedFragment = new AddWordsFragment();
            } else if (itemId == R.id.nav_play) {
                selectedFragment = new PlayFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment (Play - center tab)
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_play);
        }
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
