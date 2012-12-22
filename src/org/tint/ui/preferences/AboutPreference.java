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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutPreference extends Preference {

	public AboutPreference(Context context) {
		super(context);
		init();
	}
	
	public AboutPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public AboutPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		setLayoutResource(R.layout.about_preference);
	}
	
	@Override
    protected View onCreateView(ViewGroup parent) {
        View root = super.onCreateView(parent);
        
        TextView version = (TextView) root.findViewById(R.id.AboutVersionText);
        version.setText(getVersion());
        
        return root;
	}
	
	/**
	 * Get the current package version.
	 * @return The current version.
	 */
	private String getVersion() {
		String result = "";		
		try {

			PackageManager manager = getContext().getPackageManager();
			PackageInfo info = manager.getPackageInfo(getContext().getPackageName(), 0);

			result = String.format(getContext().getString(R.string.AboutVersionText), info.versionName, info.versionCode);

		} catch (NameNotFoundException e) {
			Log.w(AboutPreference.class.toString(), "Unable to get application version: " + e.getMessage());
			result = "Unable to get application version.";
		}

		return result;
	}

}
