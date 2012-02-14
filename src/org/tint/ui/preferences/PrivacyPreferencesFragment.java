package org.tint.ui.preferences;

import org.tint.R;
import org.tint.utils.Constants;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class PrivacyPreferencesFragment extends PreferenceFragment {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_privacy_settings);
        
        PreferenceScreen websiteSettings = (PreferenceScreen) findPreference(Constants.PREFERENCE_WEBSITES_SETTINGS);
        websiteSettings.setFragment(WebsitesSettingsFragment.class.getName());
	}

}
