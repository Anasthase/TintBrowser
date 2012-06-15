/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tint.ui.preferences;

import org.tint.R;
import org.tint.utils.Constants;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class AccessibilityPreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	private Preference mInvertedContrast;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_accessibility_settings);
        
        mInvertedContrast = findPreference(Constants.PREFERENCE_INVERTED_DISPLAY_CONTRAST);
        
        updateInvertedContrastEnabledState();
        
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onDestroy() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (Constants.PREFERENCE_INVERTED_DISPLAY.equals(key)) {
			updateInvertedContrastEnabledState();
		}
	}
	
	private void updateInvertedContrastEnabledState() {
		boolean useInvertedDisplay = getPreferenceManager().getSharedPreferences().getBoolean(Constants.PREFERENCE_INVERTED_DISPLAY, false);
        mInvertedContrast.setEnabled(useInvertedDisplay);
	}
}
