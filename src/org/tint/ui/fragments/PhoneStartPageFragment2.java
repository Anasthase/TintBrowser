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

package org.tint.ui.fragments;

import org.tint.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class PhoneStartPageFragment2 extends StartPageFragment {

	private TextView mBookmarks;
	
	@Override
	protected int getStartPageFragmentLayout() {
		return R.layout.start_page_fragment2;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		
		mBookmarks = (TextView) v.findViewById(R.id.open_bookmarks);
		mBookmarks.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mUIManager.openBookmarksActivityForResult();
			}
		});
		
		return v;
	}

}
