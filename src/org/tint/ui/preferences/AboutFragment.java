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

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.about_fragment, container, false);
		
		TextView versionText = (TextView) v.findViewById(R.id.AboutVersionText);
		versionText.setText(getVersion());
		
		TextView changelogText = (TextView) v.findViewById(R.id.AboutChangelogValue);
		changelogText.setText(ApplicationUtils.getChangelogString(getActivity()));
		
		return v;
	}
	
	/**
	 * Get the current package version.
	 * @return The current version.
	 */
	private String getVersion() {
		String result = "";		
		try {

			PackageManager manager = getActivity().getPackageManager();
			PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);

			result = String.format(getString(R.string.AboutVersionText), info.versionName, info.versionCode);

		} catch (NameNotFoundException e) {
			Log.w(AboutFragment.class.toString(), "Unable to get application version: " + e.getMessage());
			result = "Unable to get application version.";
		}

		return result;
	}

}
