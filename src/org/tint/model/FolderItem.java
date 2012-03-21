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

public class FolderItem {
	
	private long mId;
	private String mTitle;
	
	public FolderItem(long id, String title) {
		mId = id;
		mTitle = title;
	}
	
	public long getId() {
		return mId;
	}
	
	public String getTitle() {
		return mTitle;
	}

}
