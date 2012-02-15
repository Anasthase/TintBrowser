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

import org.tint.providers.BookmarksWrapper;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.AsyncTask;

public class UpdateFaviconTask extends AsyncTask<Void, Void, Void> {

	private ContentResolver mContentResolver;
	private String mUrl;
	private String mOriginalUrl;
	private Bitmap mFavicon;
	
	public UpdateFaviconTask(ContentResolver contentResolver, String url, String originalUrl, Bitmap favicon) {
		mContentResolver = contentResolver;
		mUrl = url;
		mOriginalUrl = originalUrl;
		mFavicon = favicon;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		BookmarksWrapper.updateFavicon(mContentResolver, mUrl, mOriginalUrl, mFavicon);
		return null;
	}

}
