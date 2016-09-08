package ru.komaric.birthdayreminder.activity;

import android.os.Bundle;

import ru.komaric.birthdayreminder.R;

public class PreferenceFragment extends android.preference.PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }
}
