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

public class ThumbnailTaker extends AsyncTask<Void, Void, Void> {

	private ContentResolver mContentResolver;
	private String mUrl;
	private String mOriginalUrl;
	
	private Picture mPicture;
	
	private int mCaptureWidth;
	private int mCaptureHeight;
	
	public ThumbnailTaker(ContentResolver contentResolver, String url, String originalUrl, Picture picture, int[] dimensions) {
		mContentResolver = contentResolver;
		mUrl = url;
		mOriginalUrl = originalUrl;
		
		mPicture = picture;
		
		mCaptureWidth = dimensions[0];
		mCaptureHeight = dimensions[1];
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if (mPicture != null) {
			Bitmap bm = Bitmap.createBitmap(mCaptureWidth, mCaptureHeight, Bitmap.Config.ARGB_8888);

			Canvas canvas = new Canvas(bm);

			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
			p.setColor(0xFFFFFFFF);
			canvas.drawRect(0, 0, mCaptureWidth, mCaptureHeight, p);

			float scale = mCaptureWidth / (float) mPicture.getWidth();
			canvas.scale(scale, scale);

			mPicture.draw(canvas);

			BookmarksWrapper.updateThumbnail(mContentResolver, mUrl, mOriginalUrl, bm);
		}
		
		return null;
	}

}
