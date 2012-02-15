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

package org.tint.tasks;

import java.util.Date;

import org.tint.providers.BookmarksWrapper;
import org.tint.utils.Constants;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class UpdateHistoryTask extends AsyncTask<String, Void, Void> {

	private static final long DAY_IN_MILLISECONDS = 24 * 3600 * 1000;
	
	private Activity mActivity;
	private ContentResolver mContentResolver;
	
	public UpdateHistoryTask(Activity activity) {
		mActivity = activity;
		mContentResolver = mActivity.getContentResolver();
	}
	
	@Override
	protected Void doInBackground(String... params) {
		String title = params[0];
		String url = params[1];
		String originalUrl = params[2];
		
		BookmarksWrapper.updateHistory(mContentResolver, title, url, originalUrl);
		
		// Truncate history at most once a day.
		long lastTruncation = PreferenceManager.getDefaultSharedPreferences(mActivity).getLong(Constants.TECHNICAL_PREFERENCE_LAST_HISTORY_TRUNCATION, -1);
		long now = new Date().getTime();
		
		if ((lastTruncation < 0) ||
				(now - lastTruncation > DAY_IN_MILLISECONDS)) {
			BookmarksWrapper.truncateHistory(mContentResolver, PreferenceManager.getDefaultSharedPreferences(mActivity).getString(Constants.PREFERENCE_HISTORY_SIZE, "90"));
			
			Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(mActivity).edit();
			prefEditor.putLong(Constants.TECHNICAL_PREFERENCE_LAST_HISTORY_TRUNCATION, now);
			prefEditor.commit();
		}
		
		return null;
	}

}
