package com.example.dor.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.example.dor.R;

/**
 * Manages sound effects and music for the game
 *
 * Required audio files in res/raw:
 * - background_music.mp3 - موسیقی متن صفحه اصلی
 * - tick_normal.mp3 - صدای تیک عادی تایمر
 * - tick_fast.mp3 - صدای تیک تند (۱۰ ثانیه آخر)
 * - explosion.mp3 - صدای انفجار بمب
 * - next_turn.mp3 - صدای رد شدن کلمه/نوبت بعدی
 * - word_correct.mp3 - صدای درست گفتن کلمه
 * - game_over.mp3 - صدای پایان بازی
 * - countdown_beep.mp3 - صدای بیپ شمارش معکوس
 */
public class SoundManager {
    private static SoundManager instance;
    private Context context;
    private SoundPool soundPool;
    private MediaPlayer backgroundMusic;
    private MediaPlayer bombTickPlayer; // For fast ticking in last 10 seconds
    private Vibrator vibrator;

    // Sound IDs
    private int tickNormalId = 0;
    private int tickFastId = 0;
    private int explosionId = 0;
    private int nextTurnId = 0;
    private int wordCorrectId = 0;
    private int gameOverId = 0;
    private int countdownBeepId = 0;

    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private boolean vibrationEnabled = true;
    private boolean initialized = false;
    private boolean isIntenseMode = false; // For last 10 seconds

    private SoundManager() {
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (initialized) return;

        this.context = context.getApplicationContext();
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize SoundPool for sound effects
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load sound effects
        loadSounds();

        initialized = true;
    }

    private void loadSounds() {
        try {
            // Load tick sounds
            tickNormalId = soundPool.load(context, R.raw.tick_normal, 1);
        } catch (Exception e) {
            // Sound file not found
        }

        try {
            tickFastId = soundPool.load(context, R.raw.tick_fast, 1);
        } catch (Exception e) {
            // Sound file not found
        }

        try {
            explosionId = soundPool.load(context, R.raw.explosion, 1);
        } catch (Exception e) {
            // Sound file not found
        }

        try {
            nextTurnId = soundPool.load(context, R.raw.next_turn, 1);
        } catch (Exception e) {
            // Sound file not found
        }

        try {
            wordCorrectId = soundPool.load(context, R.raw.word_correct, 1);
        } catch (Exception e) {
            // Sound file not found
        }

        try {
            gameOverId = soundPool.load(context, R.raw.game_over, 1);
        } catch (Exception e) {
            // Sound file not found
        }

        try {
            countdownBeepId = soundPool.load(context, R.raw.countdown_beep, 1);
        } catch (Exception e) {
            // Sound file not found
        }
    }

    /**
     * Play normal tick sound
     */
    public void playTick() {
        if (!soundEnabled || soundPool == null) return;

        if (isIntenseMode && tickFastId != 0) {
            soundPool.play(tickFastId, 1.0f, 1.0f, 1, 0, 1.0f);
        } else if (tickNormalId != 0) {
            soundPool.play(tickNormalId, 0.7f, 0.7f, 1, 0, 1.0f);
        }
    }

    /**
     * Set intense mode for last 10 seconds (faster ticking)
     */
    public void setIntenseMode(boolean intense) {
        this.isIntenseMode = intense;
        if (intense) {
            startIntenseTickLoop();
        } else {
            stopIntenseTickLoop();
        }
    }

    private void startIntenseTickLoop() {
        if (!soundEnabled || context == null) return;

        try {
            if (bombTickPlayer == null) {
                bombTickPlayer = MediaPlayer.create(context, R.raw.bomb_tension);
                if (bombTickPlayer != null) {
                    bombTickPlayer.setLooping(true);
                    bombTickPlayer.setVolume(0.8f, 0.8f);
                    bombTickPlayer.start();
                }
            }
        } catch (Exception e) {
            // Sound file not found
        }
    }

    private void stopIntenseTickLoop() {
        if (bombTickPlayer != null) {
            try {
                bombTickPlayer.stop();
                bombTickPlayer.release();
            } catch (Exception e) {
                // Ignore
            }
            bombTickPlayer = null;
        }
    }

    /**
     * Play explosion sound
     */
    public void playExplosion() {
        if (!soundEnabled || soundPool == null) return;

        stopIntenseTickLoop();

        if (explosionId != 0) {
            soundPool.play(explosionId, 1.0f, 1.0f, 2, 0, 1.0f);
        }
    }

    /**
     * Play next turn sound (when passing to next player)
     */
    public void playNextTurn() {
        if (!soundEnabled || soundPool == null) return;

        if (nextTurnId != 0) {
            soundPool.play(nextTurnId, 0.8f, 0.8f, 1, 0, 1.0f);
        }
    }

    /**
     * Play word correct sound
     */
    public void playWordCorrect() {
        if (!soundEnabled || soundPool == null) return;

        if (wordCorrectId != 0) {
            soundPool.play(wordCorrectId, 0.8f, 0.8f, 1, 0, 1.0f);
        }
    }

    /**
     * Play game over/winner sound
     */
    public void playGameOver() {
        if (!soundEnabled || soundPool == null) return;

        if (gameOverId != 0) {
            soundPool.play(gameOverId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    /**
     * Play countdown beep
     */
    public void playCountdownBeep() {
        if (!soundEnabled || soundPool == null) return;

        if (countdownBeepId != 0) {
            soundPool.play(countdownBeepId, 0.6f, 0.6f, 1, 0, 1.0f);
        }
    }

    /**
     * Start background music
     */
    public void startBackgroundMusic() {
        if (!musicEnabled || context == null) return;

        try {
            if (backgroundMusic == null) {
                backgroundMusic = MediaPlayer.create(context, R.raw.background_music);
                if (backgroundMusic != null) {
                    backgroundMusic.setLooping(true);
                    backgroundMusic.setVolume(0.4f, 0.4f);
                }
            }

            if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
                backgroundMusic.start();
            }
        } catch (Exception e) {
            // Sound file not found
        }
    }

    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            try {
                if (backgroundMusic.isPlaying()) {
                    backgroundMusic.pause();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Pause background music (use when game is active)
     */
    public void pauseBackgroundMusic() {
        stopBackgroundMusic();
    }

    /**
     * Resume background music
     */
    public void resumeBackgroundMusic() {
        startBackgroundMusic();
    }

    public void vibrate() {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(500);
        }
    }

    public void vibratePattern() {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return;

        long[] pattern = {0, 100, 100, 100, 100, 300};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

    /**
     * Short vibration for feedback
     */
    public void vibrateShort() {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(50);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setSoundEffectsEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        }
    }

    public void setVibrationEnabled(boolean enabled) {
        this.vibrationEnabled = enabled;
    }

    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isIntenseMode() {
        return isIntenseMode;
    }

    public void release() {
        stopIntenseTickLoop();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
        initialized = false;
    }
}
