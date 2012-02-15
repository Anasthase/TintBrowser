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
import org.tint.ui.UIManagerProvider;
import android.app.Activity;
import android.app.ActionBar.Tab;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class TabletWebViewFragment extends BaseWebViewFragment {
		
	private Tab mTab;
	
	private boolean mInitialized;
	
	public TabletWebViewFragment() {		
		mUIManager = null;
		mTab = null;		
		mParentView = null;
		mWebView = null;
		
		mUrlToLoadWhenReady = null;
		mInitialized = false;
	}
	
	public void onTabSelected(Tab tab) {
		mTab = tab;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (!mInitialized) {
			try {
				mUIManager = ((UIManagerProvider) activity).getUIManager();
			} catch (ClassCastException e) {
				Log.e("TabletWebViewFragment.onAttach()", e.getMessage());
			}
			
			mInitialized = true;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
//		mUrlBar = (TabletUrlBar) mParentView.findViewById(R.id.UrlBar);
//		
//		mProgressBar = (ProgressBar) mParentView.findViewById(R.id.WebViewProgress);
//		mProgressBar.setVisibility(View.GONE);
//		
//		mUrlBar.setEventListener(new OnTabletUrlBarEventListener() {
//			
//			@Override
//			public void onUrlValidated() {
//				// Use the UIManager to load urls, as it perform check on them.
//				mUIManager.loadCurrentUrl();
//			}
//			
//			@Override
//			public void onHomeClicked() {
//				mUIManager.loadHomePage();
//			}
//			
//			@Override
//			public void onGoStopReloadClicked() {
//				if (mUrlBar.isUrlChangedByUser()) {
//					// Use the UIManager to load urls, as it perform check on them.
//					mUIManager.loadCurrentUrl();
//				} else if (mWebView.isLoading()) {
//					mWebView.stopLoading();
//				} else {
//					mWebView.reload();
//				}
//			}
//			
//			@Override
//			public void onForwardClicked() {
//				if (mWebView.canGoForward()) {
//					mWebView.goForward();
//				}
//			}
//			
//			@Override
//			public void onBookmarksClicked() {
//				mUIManager.openBookmarksActivityForResult();
//			}
//			
//			@Override
//			public void onBackClicked() {
//				if (mWebView.canGoBack()) {
//					mWebView.goBack();
//				}
//			}
//		});
		
		if (mUrlToLoadWhenReady != null) {
			mUIManager.loadUrl(mUrlToLoadWhenReady);
			mUrlToLoadWhenReady = null;
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mParentView == null) {
			mParentView = inflater.inflate(R.layout.tablet_webview_fragment, container, false);
		}
		
		return mParentView;
	}
	
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		if (view == mWebView) {
//			mUrlBar.setUrl(url);
//			
//			mUrlBar.setGoStopReloadImage(R.drawable.ic_stop);
//			
//			mUrlBar.setBackEnabled(view.canGoBack());
//			mUrlBar.setForwardEnabled(view.canGoForward());
//			
//			mProgressBar.setProgress(0);
//			mProgressBar.setVisibility(View.VISIBLE);
		}
	}
	
	public void onPageFinished(WebView view, String url) {
		if (view == mWebView) {
//			mUrlBar.setUrl(url);
//			
//			mUrlBar.setGoStopReloadImage(R.drawable.ic_refresh);
//			
//			mUrlBar.setBackEnabled(view.canGoBack());
//			mUrlBar.setForwardEnabled(view.canGoForward());
//			
//			if (PreferenceManager.getDefaultSharedPreferences(mUIManager.getMainActivity()).getBoolean(Constants.PREFERENCE_ENABLE_ADBLOCKER, true)) {
//				mWebView.loadAdSweepIfNeeded();
//			}
//			
//			mProgressBar.setProgress(100);
//			mProgressBar.setVisibility(View.GONE);
		}
	}
	
	public void onProgressChanged(WebView view, int newProgress) {
		if (view == mWebView) {
//			mProgressBar.setProgress(newProgress);
		}
	}
	
	public void onReceivedTitle(WebView view, String title) {
		if (view == mWebView) {
			if (!TextUtils.isEmpty(title)) {
				mTab.setText(title);
			} else {
				mTab.setText(R.string.NewTab);
			}
		}
	}

}
