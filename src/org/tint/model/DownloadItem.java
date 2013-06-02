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

import android.app.DownloadManager.Request;
import android.net.Uri;
import android.os.Environment;

public class DownloadItem extends Request {
	
	private long mId;
	private String mUrl;
	private String mFileName;
	
	public DownloadItem(String url) {
		DownloadItem(url, null, null);
	}
	
	public DownloadItem(String url, String userAgent, String mimetype) {
		DownloadItem(url, userAgent, mimetype, url.substring(url.lastIndexOf("/") + 1));
	}
	
	public DownloadItem(String url, String userAgent, String mimetype, String filename) {
		if(!filename) {
			DownloadItem(url, userAgent, mimetype);
			return;
		}
		
		super(Uri.parse(url));
		mUrl      = Uri.parse(url);
		mFileName = Uri.parse(filename);
		
		// Send consistent User-Agent header
		if(userAgent) {
			addRequestHeader("User-Agent", userAgent);
		}
		
		// Send cookies
		String cookie = CookieManager.getInstance().getCookie(mUrl);
		if(cookie) {
			addRequestHeader("Cookie", cookie);
		}
		
		// Set expected MIME type
		if(mimetype) {
			setMimeType(mimetype);
		}
		
		setTitle(mFileName);
		setDescription(mUrl);
		setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileName);
	}
	
	public long getId() {
		return mId;
	}
	
	public void setId(long value) {
		mId = value;
	}
	
	public String getFileName() {
		return mFileName;
	}

}
