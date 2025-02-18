package org.alvarowau.awmediaplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;

import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.activity_settings, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        switch (key) {
            case "volume":
                Toast.makeText(getActivity(), "Volumen cambiado", Toast.LENGTH_SHORT).show();
                break;

            case "dark_mode":
                boolean isDarkMode = sharedPreferences.getBoolean(key, false);
                AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case "enable_notifications":
                boolean enableNotifications = sharedPreferences.getBoolean(key, true);
                if (enableNotifications) {
                    Toast.makeText(getActivity(), "Notificaciones: Activadas", Toast.LENGTH_SHORT).show();
                    // Aquí puedes agregar el código para activar las notificaciones
                } else {
                    Toast.makeText(getActivity(), "Notificaciones: Desactivadas", Toast.LENGTH_SHORT).show();
                    // Aquí puedes agregar el código para desactivar las notificaciones si es necesario
                }
                break;

            case "enabled_features":
                Set<String> features = sharedPreferences.getStringSet(key, new HashSet<>());
                boolean enableGreeting = features.contains("saludo");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("enable_greeting", enableGreeting);
                editor.apply();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateGreeting();
                }
                break;

            case "greeting_strength":
                String greetingStrength = sharedPreferences.getString(key, "normal");

                SharedPreferences.Editor editorStrength = sharedPreferences.edit();
                editorStrength.putString("greeting_strength", greetingStrength);
                editorStrength.apply();

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateGreeting();
                }
                break;
        }
    };
}
