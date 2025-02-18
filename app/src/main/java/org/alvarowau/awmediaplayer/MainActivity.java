package org.alvarowau.awmediaplayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUrl;
    private TextView tvGreeting;
    private ActivityResultLauncher<Intent> settingsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.activity_settings, false);

        editTextUrl = findViewById(R.id.editTextUrl);
        tvGreeting = findViewById(R.id.tvGreeting);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnSettings = findViewById(R.id.btnSettings);

        // Llamada para programar notificaciones diarias
        scheduleDailyNotification();

        // Aplicación del modo oscuro basado en la preferencia
        applyDarkModePreference();

        // Actualizar saludo basado en preferencias
        updateGreeting();

        btnPlay.setOnClickListener(v -> handlePlayButtonClick());
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });

        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    applyDarkModePreference();
                    updateGreeting();
                }
        );

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        if ("enable_greeting".equals(key) || "enabled_features".equals(key) || "greeting_strength".equals(key)) {
            updateGreeting();
        }
        if ("enable_notifications".equals(key)) {
            boolean enableNotifications = sharedPreferences.getBoolean(key, true);
            if (enableNotifications) {
                // Aquí se puede agregar la lógica para activar las notificaciones si es necesario
                scheduleDailyNotification();
            } else {
                // Aquí se puede agregar la lógica para desactivar las notificaciones
                cancelDailyNotification();
            }
        }
    };

    public void updateGreeting() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableGreetingGeneral = preferences.getBoolean("enable_greeting", true);
        Set<String> enabledFeatures = preferences.getStringSet("enabled_features", new HashSet<>());
        boolean enableGreetingFeature = enabledFeatures.contains("saludo");

        String userName = preferences.getString("user_name", "").trim();

        if (enableGreetingGeneral && enableGreetingFeature) {
            String greetingText = userName.isEmpty() ? "¡Hola!" : "¡Hola, " + userName + "!";
            String greetingStrength = preferences.getString("greeting_strength", "normal");
            int greetingColor = getResources().getColor(R.color.greeting_normal);

            switch (greetingStrength) {
                case "normal":
                    greetingColor = getResources().getColor(R.color.greeting_normal);
                    break;
                case "strong":
                    greetingText = "¡HOLA, " + userName.toUpperCase() + "!";
                    greetingColor = getResources().getColor(R.color.greeting_strong);
                    break;
                case "weak":
                    greetingText = "hola, " + userName.toLowerCase();
                    greetingColor = getResources().getColor(R.color.greeting_weak);
                    break;
            }

            tvGreeting.setText(greetingText);
            tvGreeting.setTextColor(greetingColor);
            tvGreeting.setVisibility(View.VISIBLE);
        } else {
            tvGreeting.setVisibility(View.GONE);
        }
    }

    private void handlePlayButtonClick() {
        String url = editTextUrl.getText().toString().trim();

        if (url.isEmpty()) {
            showToast("Por favor, ingresa una URL.");
            return;
        }

        checkIfUrlIsVideo(url);
    }

    private void applyDarkModePreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        int mode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void checkIfUrlIsVideo(String url) {
        new CheckUrlTask(isVideo -> {
            if (isVideo) {
                Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                intent.putExtra("URL", url);
                startActivity(intent);
            } else {
                showToast("La URL no es un video válido.");
            }
        }).execute(url);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void scheduleDailyNotification() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableNotifications = preferences.getBoolean("enable_notifications", true);

        if (!enableNotifications) {
            return; // Si las notificaciones están desactivadas, no programar nada.
        }

        // Obtener el tiempo actual
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12); // Establecer la hora a las 12:00 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Si ya pasó la hora de hoy, establecer para el próximo día
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        // Crear un intent para el BroadcastReceiver
        Intent intent = new Intent(this, DailyNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Usar AlarmManager para programar la notificación
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void cancelDailyNotification() {
        // Cancelar la notificación si se desactivan las notificaciones
        Intent intent = new Intent(this, DailyNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
