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

package org.tint.ui.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.fragments.PhoneWebViewFragment;
import org.tint.ui.views.PhoneUrlBar;
import org.tint.utils.Constants;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.view.ActionMode;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public abstract class BasePhoneUIManager extends BaseUIManager {

	protected enum AnimationType {
		NONE,
		FADE
	}
	
	protected static AnimationType sAnimationType;
	
	protected List<PhoneWebViewFragment> mFragmentsList;
	protected Map<UUID, PhoneWebViewFragment> mFragmentsMap;
	
	protected PhoneUrlBar mUrlBar;
	
	protected ProgressBar mProgressBar;
	
	protected RelativeLayout mTopBar;
	
	protected ImageView mBack;
	protected ImageView mForward;
	
	protected int mCurrentTabIndex = -1;
	protected Fragment mCurrentFragment = null;
	
	protected ActionMode mActionMode;
	
	public BasePhoneUIManager(TintBrowserActivity activity) {
		super(activity);
		
		mFragmentsList = new ArrayList<PhoneWebViewFragment>();
		mFragmentsMap = new HashMap<UUID, PhoneWebViewFragment>();
	}
	
	@Override
	public void addTab(String url, boolean openInBackground, boolean privateBrowsing) {
		boolean startPage = false;
		if (Constants.URL_ABOUT_START.equals(url)) {
			url = null;
			startPage = true;
		}
		
		PhoneWebViewFragment fragment = new PhoneWebViewFragment();
		fragment.init(this, privateBrowsing, url);		
		
		mFragmentsList.add(mCurrentTabIndex + 1, fragment);
		mFragmentsMap.put(fragment.getUUID(), fragment);		
		
		if (!openInBackground) {
			mCurrentTabIndex++;
			
			if (startPage) {
				fragment.setStartPageShown(true);
				
				if (mStartPageFragment == null) {
					createStartPageFragment();
				}
				
				setCurrentFragment(mStartPageFragment, sAnimationType);
				onShowStartPage();
			} else {
				fragment.setStartPageShown(false);
				setCurrentFragment(fragment, sAnimationType);
			}			
			
			CustomWebView webView = getCurrentWebView();

			if (!webView.isPrivateBrowsingEnabled()) {
				Controller.getInstance().getAddonManager().onTabSwitched(mActivity, webView);
			}
		}
	}
	
	@Override
	public void closeCurrentTab() {
		if (mFragmentsList.size() > 1) {
			closeTabByIndex(mCurrentTabIndex);
		} else {
			closeLastTab();			
		}
	}
	
	@Override
	public void closeTab(UUID tabId) {
		int index = mFragmentsList.indexOf(getWebViewFragmentByUUID(tabId));
		
		if (mFragmentsList.size() > 1) {			
			if ((index >= 0) &&
					(index < mFragmentsList.size())) {
				closeTabByIndex(index);
			}
		} else if (index == mCurrentTabIndex) {
			closeLastTab();
		}
	}
	
	protected void closeLastTab() {
		PhoneWebViewFragment fragment = mFragmentsList.get(mCurrentTabIndex);
		
		CustomWebView webView = fragment.getWebView();
		
		if (!webView.isPrivateBrowsingEnabled()) {
			Controller.getInstance().getAddonManager().onTabClosed(mActivity, webView);
		}
		
		webView.onPause();
		
		loadHomePage();
		updateUrlBar();
	}
	
	protected void closeTabByIndex(int index) {
		if ((index >= 0) &&
				(index < mFragmentsList.size())) {
			boolean currentTab = index == mCurrentTabIndex;		

			PhoneWebViewFragment fragment = mFragmentsList.get(index);

			CustomWebView webView = fragment.getWebView();

			if (!webView.isPrivateBrowsingEnabled()) {
				Controller.getInstance().getAddonManager().onTabClosed(mActivity, webView);
			}

			webView.onPause();

			mFragmentsList.remove(index);
			mFragmentsMap.remove(fragment.getUUID());

			if (currentTab) {
				if (mCurrentTabIndex > 0) {
					mCurrentTabIndex--;
				}

				showCurrentTab(true);
			} else {
				if (index < mCurrentTabIndex) {
					mCurrentTabIndex--;
				}
			}
		}
	}
	
	protected void showCurrentTab(boolean notifyTabSwitched) {
		PhoneWebViewFragment newFragment = mFragmentsList.get(mCurrentTabIndex);
		
		if (newFragment.isStartPageShown()) {
			setCurrentFragment(mStartPageFragment, sAnimationType);
			mUrlBar.hideGoStopReloadButton();
		} else {
			setCurrentFragment(newFragment, sAnimationType);
			mUrlBar.showGoStopReloadButton();
			newFragment.getWebView().onResume();						
		}
		
		if (notifyTabSwitched) {
			CustomWebView webView = getCurrentWebView();
			
			if (!webView.isPrivateBrowsingEnabled()) {
				Controller.getInstance().getAddonManager().onTabSwitched(mActivity, webView);
			}
		}
		
		updateUrlBar();
	}
	
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		if (view == getCurrentWebView()) {
			mProgressBar.setProgress(newProgress);
		}
	}
	
	@Override
	public void onReceivedTitle(WebView view, String title) {
		if (view == getCurrentWebView()) {
			if ((title != null) &&
					(!title.isEmpty())) {
				mUrlBar.setTitle(title);
				mUrlBar.setSubtitle(view.getUrl());
			} else {
				mUrlBar.setTitle(R.string.ApplicationName);
				mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);
			}
		}
	}
	
	@Override
	public void onShowStartPage() {
		mUrlBar.setTitle(mActivity.getString(R.string.ApplicationName));
		mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);
		mUrlBar.setGoStopReloadImage(R.drawable.ic_go);
		mUrlBar.hideGoStopReloadButton();
					
		mUrlBar.setUrl(null);
		mBack.setEnabled(false);
		mForward.setEnabled(false);
	}
	
	@Override
	public void onHideStartPage() {
		mUrlBar.showGoStopReloadButton();
	}

	@Override
	public void loadUrl(String url) {
		mUrlBar.hideUrl();
		super.loadUrl(url);
	}
	
	@Override
	public CustomWebView getCurrentWebView() {
		if (mCurrentTabIndex != -1) {
			return mFragmentsList.get(mCurrentTabIndex).getWebView();
		} else {
			return null;
		}
	}
	
	@Override
	public BaseWebViewFragment getCurrentWebViewFragment() {
		if (mCurrentTabIndex != -1) {
			return mFragmentsList.get(mCurrentTabIndex);
		} else {
			return null;
		}
	}
	
	@Override
	protected BaseWebViewFragment getWebViewFragmentByUUID(UUID fragmentId) {
		return mFragmentsMap.get(fragmentId);
	}
	
	@Override
	protected String getCurrentUrl() {
		return mUrlBar.getUrl();
	}

	@Override
	protected int getTabCount() {
		return mFragmentsList.size();
	}
	
	@Override
	protected void showStartPage(BaseWebViewFragment webViewFragment) {
		if ((webViewFragment != null) &&
				(!webViewFragment.isStartPageShown())) {
		
			webViewFragment.getWebView().onPause();
			webViewFragment.setStartPageShown(true);
			
			if (webViewFragment == getCurrentWebViewFragment()) {

				if (mStartPageFragment == null) {
					createStartPageFragment();
				}

				setCurrentFragment(mStartPageFragment, sAnimationType);
				
				onShowStartPage();
			}
		}
	}

	@Override
	protected void hideStartPage(BaseWebViewFragment webViewFragment) {
		if ((webViewFragment != null) &&
				(webViewFragment.isStartPageShown())) {
		
			webViewFragment.setStartPageShown(false);
			
			if (webViewFragment == getCurrentWebViewFragment()) {
				setCurrentFragment(webViewFragment, sAnimationType);

				onHideStartPage();
			}
		}
	}

	@Override
	protected void resetUI() {
		updateUrlBar();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		for (PhoneWebViewFragment fragment : mFragmentsList) {
			fragment.getWebView().loadSettings();
		}
	}
	
	@Override
	protected Collection<BaseWebViewFragment> getTabsFragments() {		
		return new ArrayList<BaseWebViewFragment>(mFragmentsList);
	}
	
	protected void setCurrentFragment(Fragment fragment, AnimationType animationType) {
		if (fragment != mCurrentFragment) {
			mCurrentFragment = fragment;
			
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();			
			
			switch (animationType) {
			case NONE: break;
			case FADE: fragmentTransaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out); break;
			default: break;
			}
			
			fragmentTransaction.replace(R.id.WebViewContainer, mCurrentFragment);				
			fragmentTransaction.commit();
		}
	}
	
	protected void updateBackForwardEnabled() {
		CustomWebView currentWebView = getCurrentWebView();
		
		mBack.setEnabled(currentWebView.canGoBack());
		mForward.setEnabled(currentWebView.canGoForward());
	}
	
	protected abstract void createStartPageFragment();
	
	protected abstract void updateUrlBar();

}
