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

import java.util.UUID;

import org.tint.ui.components.CustomWebChromeClient;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.components.CustomWebViewClient;
import org.tint.ui.managers.UIManager;
import org.tint.utils.Constants;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public abstract class BaseWebViewFragment extends Fragment {
	
	protected UUID mUUID;
	
	protected UIManager mUIManager;
	protected ViewGroup mParentView;
	protected CustomWebView mWebView;
	
	protected boolean mPrivateBrowsing;
	
	private boolean mIsStartPageShown;
	private boolean mWebViewAddedToParent;
	
	private String mUrlToLoad;
	
	protected BaseWebViewFragment() {
		mUUID = UUID.randomUUID();
		mPrivateBrowsing = false;
		mIsStartPageShown = false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	public void init(UIManager uiManager, boolean privateBrowsing, String urlToLoad) {
		mUIManager = uiManager;
		mPrivateBrowsing = privateBrowsing;
		
		mUrlToLoad = urlToLoad;
		
		createWebView(false);
	}
	
	public void resetWebView() {
		if (mWebViewAddedToParent) {
			mParentView.removeView(mWebView);
		}
		
		createWebView(true);
	}
	
	public UUID getUUID() {
		return mUUID;
	}
	
	public CustomWebView getWebView() {
		return mWebView;
	}
	
	public boolean isStartPageShown() {
		return mIsStartPageShown;
	}
	
	public void setStartPageShown(boolean value) {
		mIsStartPageShown = value;
	}
	
	public boolean isPrivateBrowsingEnabled() {
		return mPrivateBrowsing;
	}
	
	public void setPrivateBrowsing(boolean privateBrowsing) {
		mPrivateBrowsing = privateBrowsing;
	}
	
	public boolean isWebViewOnUrl(String url) {
		if (mWebView != null) {
			String currentUrl = mWebView.getUrl();
			
			return currentUrl != null && currentUrl.equals(url);
		}
		
		return false;
	}
	
	protected void onViewCreated() {
		if (!mWebViewAddedToParent) {
			mParentView.addView(mWebView);
			mWebViewAddedToParent = true;
		}
		
		if (mUrlToLoad != null) {
			mUIManager.loadUrl(this, mUrlToLoad);
			mUrlToLoad = null;
		}
	}
	
	private void createWebView(boolean addToParent) {
		mWebView = new CustomWebView(mUIManager, mPrivateBrowsing);
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mWebView.setLayoutParams(params);
		
		mWebView.setParentFragment(this);
		
		mWebView.setWebChromeClient(new CustomWebChromeClient(mUIManager));
		mWebView.setWebViewClient(new CustomWebViewClient(mUIManager));
		
		mWebView.setOnTouchListener(mUIManager);		
		
		if ((addToParent) &&
				(mParentView != null)) {
			mParentView.addView(mWebView);
			mWebViewAddedToParent = true;
		} else {
			mWebViewAddedToParent = false;
		}
		
		// Little trick here. We must load the url here, in order to
		// the background loading to work. But if we show the start page
		// from here, onCreateView() from the inherited class won't be
		// called (don't know why), and the WebView will never be attached.
		// So the start page will be loaded when the view has been created,
		// through onViewCreated().
		if ((mUrlToLoad != null) &&
				(!Constants.URL_ABOUT_START.equals(mUrlToLoad))) {
			mUIManager.loadUrl(this, mUrlToLoad);
			mUrlToLoad = null;
		}
	}

}
