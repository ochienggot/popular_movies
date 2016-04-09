package com.nicholasgot.project1;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class SettingsActivity extends PreferenceActivity {
    public static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsActivityFragment())
                .commit();
    }
}
