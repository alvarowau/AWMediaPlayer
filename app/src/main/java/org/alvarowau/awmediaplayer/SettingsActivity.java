package org.alvarowau.awmediaplayer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Establecer las preferencias predeterminadas si aún no están establecidas
        PreferenceManager.setDefaultValues(this, R.xml.activity_settings, false);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment_container, new SettingsFragment())
                .commit();
    }
}

