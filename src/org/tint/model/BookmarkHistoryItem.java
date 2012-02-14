package org.tint.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Represent an history element.
 */
public class BookmarkHistoryItem {
	
	private long mId;
	private String mTitle;
	private String mUrl;
	private boolean mIsBookmark;
	private Bitmap mFavicon;

	/**
	 * Constructor.
	 * @param id The element id.
	 * @param title The title.
	 * @param url The url.
	 * @param isBookmark True if this item is also a bookmark.
	 * @param faviconData The favicon.
	 */
	public BookmarkHistoryItem(long id, String title, String url, boolean isBookmark, byte[] faviconData) {
		mId = id;
		mTitle = title;
		mUrl = url;
		mIsBookmark = isBookmark;
		
		if (faviconData != null) {
			mFavicon = BitmapFactory.decodeByteArray(faviconData, 0, faviconData.length);
		} else {
			mFavicon = null;
		}
	}

	/**
	 * Get the id.
	 * @return The id.
	 */
	public long getId() {
		return mId;
	}

	/**
	 * Get the title.
	 * @return The title.
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Get the url.
	 * @return The url.
	 */
	public String getUrl() {
		return mUrl;
	}
	
	public boolean isBookmark() {
		return mIsBookmark;
	}
	
	/**
	 * Get the favicon.
	 * @return The favicon.
	 */
	public Bitmap getFavicon() {
		return mFavicon;
	}
	
}
