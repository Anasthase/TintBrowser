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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.os.AsyncTask;
import android.webkit.WebView;

public class ThumbnailTaker extends AsyncTask<Void, Void, Void> {

	private ContentResolver mContentResolver;
	private String mUrl;
	private String mOriginalUrl;
	private WebView mWebView;
	
	private int mCaptureWidth;
	private int mCaptureHeight;
	
	public ThumbnailTaker(ContentResolver contentResolver, String url, String originalUrl, WebView webView, int[] dimensions) {
		mContentResolver = contentResolver;
		mUrl = url;
		mOriginalUrl = originalUrl;
		mWebView = webView;
		
		mCaptureWidth = dimensions[0];
		mCaptureHeight = dimensions[1];
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		// Only save a thumbnail if there is a bookmark corresponding to one of the url.
		// Thumbnails for history records are not used, so save space in database.
		if (BookmarksWrapper.urlHasBookmark(mContentResolver, mUrl, mOriginalUrl)) {
			
			// Wait before taking the screenshot, to give a change to the page to fully load.
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// Don't care.
			}
			
			Picture picture = mWebView.capturePicture();
			
			if (picture != null) {
				Bitmap bm = Bitmap.createBitmap(mCaptureWidth, mCaptureHeight, Bitmap.Config.ARGB_4444);

				Canvas canvas = new Canvas(bm);

				Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
				p.setColor(0xFFFFFFFF);
				canvas.drawRect(0, 0, mCaptureWidth, mCaptureHeight, p);

				float scale = mCaptureWidth / (float) picture.getWidth();
				canvas.scale(scale, scale);

				picture.draw(canvas);

				BookmarksWrapper.updateThumbnail(mContentResolver, mUrl, mOriginalUrl, bm);
			}
		}
		
		return null;
	}

}
