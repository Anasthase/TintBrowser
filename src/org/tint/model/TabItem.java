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

import android.graphics.Picture;

public class TabItem {

	private String mTitle;	
	private String mUrl;
	private Picture mThumbnail;
	
	public TabItem(String title, String url, Picture thumbnail) {
		mTitle = title;
		mUrl = url;
		mThumbnail = thumbnail;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public Picture getThumbnail() {
		return mThumbnail;
	}
	
}
