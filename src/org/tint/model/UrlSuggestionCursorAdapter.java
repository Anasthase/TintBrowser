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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

/**
 * Adapter for suggestions.
 */
public class UrlSuggestionCursorAdapter extends SimpleCursorAdapter {
	
	public interface QueryBuilderListener {
		public void onSuggestionSelected(String url);
	}
	
	private QueryBuilderListener mQueryBuilderListener = null;
	
	/**
	 * Constructor.
	 * @param context The context.
	 * @param layout The layout.
	 * @param c The Cursor. 
	 * @param from Input array.
	 * @param to Output array.
	 */
	public UrlSuggestionCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, QueryBuilderListener listener) {		
		super(context, layout, c, from, to, flags);
		mQueryBuilderListener = listener;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View superView = super.getView(position, convertView, parent);		
		
		ImageView iconView = (ImageView) superView.findViewById(R.id.AutocompleteImageView);

		int resultType = getCursor().getInt(getCursor().getColumnIndex(BookmarksProvider.Columns.BOOKMARK));
		
		switch (resultType) {
		case 0: iconView.setImageResource(R.drawable.ic_search_category_history); break;
		default: iconView.setImageResource(R.drawable.ic_search_category_bookmark); break;
		}
		
		final String url = getCursor().getString(getCursor().getColumnIndex(BookmarksProvider.Columns.URL));
		
		ImageView queryBuilderView = (ImageView) superView.findViewById(R.id.AutoCompleteQueryBuilder);
		queryBuilderView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mQueryBuilderListener != null) {
					mQueryBuilderListener.onSuggestionSelected(url);
				}
			}
		});
		
		return superView;
	}
	
	

}
