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

package org.tint.model;

import org.tint.R;
import org.tint.providers.BookmarksProvider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BookmarksAdapter extends SimpleCursorAdapter {
	
	private int mDefaultThumbnailId;
	
	public BookmarksAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, int defaultThumbnailId) {
		super(context, layout, c, from, to, flags);
		
		mDefaultThumbnailId = defaultThumbnailId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View superView = super.getView(position, convertView, parent);
				
		ImageView thumbnailView = (ImageView) superView.findViewById(R.id.BookmarkRow_Thumbnail);
		
		boolean isFolder = getCursor().getInt(getCursor().getColumnIndex(BookmarksProvider.Columns.IS_FOLDER)) > 0 ? true : false;
		
		if (!isFolder) {			
			byte[] thumbnail = getCursor().getBlob(getCursor().getColumnIndex(BookmarksProvider.Columns.THUMBNAIL));
			if (thumbnail != null) {
				thumbnailView.setImageBitmap(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
			} else {
				thumbnailView.setImageResource(mDefaultThumbnailId);
			}
		} else {
			TextView tv = (TextView) superView.findViewById(R.id.BookmarkRow_Url);
			if (tv != null) {
				tv.setText(R.string.Folder);
			}
			
			thumbnailView.setImageResource(R.drawable.ic_folder);
		}
		
		return superView;
	}

}
