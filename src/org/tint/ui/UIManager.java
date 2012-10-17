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

package org.tint.ui;

import java.util.UUID;

import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.fragments.BaseWebViewFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.ActionMode;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebChromeClient.CustomViewCallback;

public interface UIManager extends OnTouchListener {
	
	TintBrowserActivity getMainActivity();
	
	/**
	 * Browser management.	
	 */
	void addTab(String url, boolean openInBackground, boolean privateBrowsing);
	
	void addTab(boolean loadHomePage, boolean privateBrowsing);
	
	void closeCurrentTab();
	
	void closeTab(UUID tabId);
	
	void togglePrivateBrowsing();

	void loadUrl(String url);
	
	void loadUrl(UUID tabId, String url, boolean loadInCurrentTabIfNotFound);
	
	void loadRawUrl(UUID tabId, String url, boolean loadInCurrentTabIfNotFound);
	
	void loadUrl(BaseWebViewFragment webViewFragment, String url);
	
	void loadCurrentUrl();
	
	void loadHomePage();
	
	void loadHomePage(UUID tabId, boolean loadInCurrentTabIfNotFound);
	
	void openBookmarksActivityForResult();
	
	void addBookmarkFromCurrentPage();
	
	void shareCurrentPage();
	
	void startSearch();
	
	void clearFormData();
	
	void clearCache();
	
	void setHttpAuthUsernamePassword(String host, String realm, String username, String password);
	
	CustomWebView getCurrentWebView();
	
	CustomWebView getWebViewByTabId(UUID tabId);
	
	BaseWebViewFragment getCurrentWebViewFragment();
	
	void setUploadMessage(ValueCallback<Uri> uploadMsg);
	
	ValueCallback<Uri> getUploadMessage();
	
	void onNewIntent(Intent intent);
		
	/**
	 * Events.
	 */	
	boolean onKeyBack();
	
	boolean onKeySearch();
	
	void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key);
	
	void onMenuVisibilityChanged(boolean isVisible);
	
	void onPageStarted(WebView view, String url, Bitmap favicon);
	
	void onPageFinished(WebView view, String url);
	
	void onProgressChanged(WebView view, int newProgress);
	
	void onReceivedTitle(WebView view, String title);
	
	void onReceivedIcon(WebView view, Bitmap icon);
	
	void onMainActivityPause();
	
	void onMainActivityResume();
	
	void onShowStartPage();
	
	void onHideStartPage();
	
	void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback);
	
	void onHideCustomView();
	
	void onGeolocationPermissionsShowPrompt(String origin, Callback callback);
	
	void onGeolocationPermissionsHidePrompt();
	
	void onActionModeStarted(ActionMode mode);
	
	void onActionModeFinished(ActionMode mode);

}
