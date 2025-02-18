package org.alvarowau.awmediaplayer;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class VideoActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        videoView = findViewById(R.id.videoView);

        String videoUrl = getIntent().getStringExtra("URL");

        if (videoUrl != null && !videoUrl.isEmpty()) {
            videoView.setVideoPath(videoUrl);
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);

            videoView.start();

            applySettings();
        }
    }

    private void applySettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int volume = preferences.getInt("volume", 50);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int volumeLevel = (int) (maxVolume * (volume / 100.0f));

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0);
        }

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
