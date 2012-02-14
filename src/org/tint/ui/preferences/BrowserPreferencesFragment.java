package org.tint.ui.preferences;

import org.tint.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class BrowserPreferencesFragment extends PreferenceFragment {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_browser_settings);
	}

}
