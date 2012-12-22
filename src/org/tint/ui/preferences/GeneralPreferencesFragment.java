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
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class GeneralPreferencesFragment extends PreferenceFragment {
	
	private OnSharedPreferenceChangeListener mListener;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_general_settings);
        
        if (ApplicationUtils.isTablet(getActivity())) {
        	PreferenceCategory uiCategory = (PreferenceCategory) findPreference("PREFERENCE_CATEGORY_UI");
        	getPreferenceScreen().removePreference(uiCategory);
        }
        
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Constants.PREFERENCE_PHONE_NEW_UI, true)) {
        	PreferenceCategory uiCategory = (PreferenceCategory) findPreference("PREFERENCE_CATEGORY_UI");
        	
        	uiCategory.removePreference(findPreference(Constants.PREFERENCE_BUBBLE_POSITION));
        	uiCategory.removePreference(findPreference(Constants.PREFERENCE_TOOLBARS_AUTOHIDE_DURATION));
        	uiCategory.removePreference(findPreference(Constants.PREFERENCES_SWITCH_TABS_METHOD));
        } else {
        	
        }
        
        mListener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (Constants.PREFERENCE_PHONE_NEW_UI.equals(key)) {
					askForRestart();
				}				
			}			
		};
		
		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(mListener);
	}

	@Override
	public void onDestroy() {
		PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(mListener);
		super.onDestroy();
	}
	
	private void askForRestart() {
		ApplicationUtils.showYesNoDialog(getActivity(),
				android.R.drawable.ic_dialog_alert,
				R.string.RestartDialogTitle,
				R.string.RestartDialogMessage,
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Activity activity = getActivity();
				
				PendingIntent intent = PendingIntent.getActivity(activity.getBaseContext(), 0, new Intent(activity.getIntent()), activity.getIntent().getFlags());
				AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
				mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
				System.exit(2);
			}
			
		});
	}

}
