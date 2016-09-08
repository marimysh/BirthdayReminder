package ru.komaric.birthdayreminder.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import ru.komaric.birthdayreminder.R;
import ru.komaric.birthdayreminder.sql.Preference;
import ru.komaric.birthdayreminder.sql.VK;

public class PreferenceActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final int REQUEST_PERMISSIONS_CONTACTS = 0;

    Toolbar toolbar;
    PreferenceFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_activity);

        fragment = (PreferenceFragment) getFragmentManager().findFragmentById(R.id.fragment);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.preference);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Preference.CONTACTS:
                if (sharedPreferences.getBoolean(key, false)) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                            PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_PERMISSIONS_CONTACTS);
                    }
                }
                break;
            case Preference.VK:
                if (sharedPreferences.getBoolean(key, false)) {
                    VKSdk.login(this);
                } else {
                    VKSdk.logout();
                    VK vk = new VK(this);
                    vk.open();
                    vk.clearDbAndImages();
                    vk.close();
                }
                break;
        }
        setResult(RESULT_OK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
            }

            @Override
            public void onError(VKError error) {
                PreferenceManager.getDefaultSharedPreferences(PreferenceActivity.this).edit().
                        putBoolean(Preference.VK, false).
                        apply();
                ((SwitchPreference) fragment.findPreference(Preference.VK)).setChecked(false);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    PreferenceManager.getDefaultSharedPreferences(this).edit().
                            putBoolean(Preference.CONTACTS, false).
                            apply();
                    ((SwitchPreference) fragment.findPreference(Preference.CONTACTS)).setChecked(false);
                }
        }
    }
}
