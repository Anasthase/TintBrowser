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
