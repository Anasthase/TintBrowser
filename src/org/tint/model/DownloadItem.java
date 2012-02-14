package org.tint.model;

import android.app.DownloadManager.Request;
import android.net.Uri;
import android.os.Environment;

public class DownloadItem extends Request {
	
	private long mId;
	private String mUrl;
	private String mFileName;
	
	public DownloadItem(String url) {
		super(Uri.parse(url));
		mUrl = url;
		mFileName = mUrl.substring(url.lastIndexOf("/") + 1);
		
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
