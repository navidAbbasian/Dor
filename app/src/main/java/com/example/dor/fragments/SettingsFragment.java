package com.example.dor.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dor.R;
import com.example.dor.utils.SoundManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Fragment for app settings
 */
public class SettingsFragment extends Fragment {

    private SwitchMaterial soundEffectsSwitch;
    private SwitchMaterial backgroundMusicSwitch;
    private SwitchMaterial vibrationSwitch;
    private SharedPreferences prefs;

    private static final String PREFS_NAME = "game_settings";
    private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_BACKGROUND_MUSIC = "background_music";
    private static final String KEY_VIBRATION = "vibration";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize views
        soundEffectsSwitch = view.findViewById(R.id.soundEffectsSwitch);
        backgroundMusicSwitch = view.findViewById(R.id.backgroundMusicSwitch);
        vibrationSwitch = view.findViewById(R.id.vibrationSwitch);

        // Load saved settings
        loadSettings();

        // Setup listeners
        setupListeners();
    }

    private void loadSettings() {
        soundEffectsSwitch.setChecked(prefs.getBoolean(KEY_SOUND_EFFECTS, true));
        backgroundMusicSwitch.setChecked(prefs.getBoolean(KEY_BACKGROUND_MUSIC, true));
        vibrationSwitch.setChecked(prefs.getBoolean(KEY_VIBRATION, true));
    }

    private void setupListeners() {
        soundEffectsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_SOUND_EFFECTS, isChecked).apply();
            SoundManager.getInstance().setSoundEffectsEnabled(isChecked);
        });

        backgroundMusicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_BACKGROUND_MUSIC, isChecked).apply();
            SoundManager soundManager = SoundManager.getInstance();
            if (isChecked) {
                soundManager.startBackgroundMusic();
            } else {
                soundManager.stopBackgroundMusic();
            }
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_VIBRATION, isChecked).apply();
            SoundManager.getInstance().setVibrationEnabled(isChecked);
        });
    }
}

