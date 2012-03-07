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

import org.tint.ui.UIManager;
import org.tint.ui.components.CustomWebChromeClient;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.components.CustomWebViewClient;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public abstract class BaseWebViewFragment extends Fragment {
	
	public interface WebViewFragmentListener {
		void onFragmentReady(BaseWebViewFragment fragment, String urlToLoadWhenReady);
	}
	
	protected UIManager mUIManager;
	protected ViewGroup mParentView;
	protected CustomWebView mWebView;
	
	protected boolean mPrivateBrowsing;
	
	protected UUID mUUID;
	
	protected String mUrlToLoadWhenReady = null;
	
	protected WebViewFragmentListener mWebViewFragmentListener = null;
	
	private Animation mShowAnimation;
	private Animation mHideAnimation;
	
	private boolean mIsStartPageShown = false;
	
	protected BaseWebViewFragment() {
		mUUID = UUID.randomUUID();
		mPrivateBrowsing = false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		mShowAnimation = new AlphaAnimation(0, 1);
		mShowAnimation.setDuration(250);
		
		mHideAnimation = new AlphaAnimation(1, 0);
		mHideAnimation.setDuration(250);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		createWebView();
	}
	
	@Override
	public void onPause() {
		mWebView.onPause();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		mWebView.onResume();
		super.onResume();
	}
	
	public UUID getUUID() {
		return mUUID;
	}

	public CustomWebView getWebView() {
		if (mWebView == null) {
			// Force fragment creation if we do not have the WebView yet.
			this.getFragmentManager().executePendingTransactions();
		}
		
		return mWebView;
	}
	
	public void resetWebView() {
		mParentView.removeView(mWebView);
		
		createWebView();
	}
	
	public void requestUrlToLoadWhenReady(String url) {
		mUrlToLoadWhenReady = url;
	}
	
	public void setPrivateBrowsing(boolean privateBrowsing) {
		mPrivateBrowsing = privateBrowsing;
	}
	
	public boolean isPrivateBrowsingEnabled() {
		return mPrivateBrowsing;
	}
	
	public boolean isStartPageShown() {
		return mIsStartPageShown;
	}
	
	public void setStartPageShown(boolean value) {
		mIsStartPageShown = value;
	}
	
	public void setWebViewFragmentListener(WebViewFragmentListener listener) {
		mWebViewFragmentListener = listener;
	}
	
	private void createWebView() {
		mWebView = new CustomWebView(getActivity(), mPrivateBrowsing);
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mWebView.setLayoutParams(params);
		
		mWebView.setParentFragment(this);
		
		mWebView.setWebChromeClient(new CustomWebChromeClient(mUIManager));
		mWebView.setWebViewClient(new CustomWebViewClient(mUIManager));
		
		mWebView.setOnTouchListener(mUIManager);
		
		mParentView.addView(mWebView);
	}

}
