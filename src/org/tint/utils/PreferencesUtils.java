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

package org.tint.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferencesUtils {

	/**
	 * Try to get an int value from preferences. If it fails, try to get it as string and store the value as int.
	 * Used to convert an old string pref to a new int one.
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static int getConvertedIntPreference(Context context, String key, int defaultValue) {
		int value;

		try {
			value = PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
		} catch (ClassCastException e) {
			try {
				value = Integer.parseInt(
						PreferenceManager.getDefaultSharedPreferences(context).getString(key, Integer.toString(defaultValue)));
			} catch (Exception ex) {
				value = defaultValue;
			}
			
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.remove(key);
			editor.putInt(key, value);
			editor.commit();
		}

		return value;
	}

}
