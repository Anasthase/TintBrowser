package org.tint.ui.preferences;

import org.tint.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class GeneralPreferencesFragment extends PreferenceFragment {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_general_settings);
	}

}
