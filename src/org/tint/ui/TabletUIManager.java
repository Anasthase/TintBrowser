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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.fragments.StartPageFragment;
import org.tint.ui.fragments.TabletWebViewFragment;
import org.tint.ui.fragments.StartPageFragment.OnStartPageItemClickedListener;
import org.tint.ui.tabs.WebViewFragmentTabListener;
import org.tint.ui.views.TabletUrlBar;
import org.tint.ui.views.TabletUrlBar.OnTabletUrlBarEventListener;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class TabletUIManager extends BaseUIManager {

	private Map<Tab, TabletWebViewFragment> mTabs;
	private Map<UUID, TabletWebViewFragment> mFragmentsMap;
	
	private TabletUrlBar mUrlBar;
	private ProgressBar mProgressBar;
	
	public TabletUIManager(TintBrowserActivity activity) {
		super(activity);
		
		mTabs = new Hashtable<Tab, TabletWebViewFragment>();
		mFragmentsMap = new Hashtable<UUID, TabletWebViewFragment>();		

		if (mStartPageFragment == null) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			
			mStartPageFragment = new StartPageFragment();
			mStartPageFragment.setOnStartPageItemClickedListener(new OnStartPageItemClickedListener() {					
				@Override
				public void onStartPageItemClicked(String url) {
					loadUrl(url);
				}
			});
			
			ft.add(R.id.WebViewContainer, mStartPageFragment);
			ft.hide(mStartPageFragment);
			
			ft.commit();
		}
	}
	
	public void onTabSelected(Tab tab) {
		updateUrlBar();
		
		CustomWebView webView = getCurrentWebView();
		if ((webView != null) &&
				(!webView.isPrivateBrowsingEnabled())) {
			Controller.getInstance().getAddonManager().onTabSwitched(mActivity, webView);
		}
	}
	
	@Override
	protected void setupUI() {
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setHomeButtonEnabled(false);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mProgressBar = (ProgressBar) mActivity.findViewById(R.id.WebViewProgress);
		
		mUrlBar = (TabletUrlBar) mActivity.findViewById(R.id.UrlBar);
		mUrlBar.setEventListener(new OnTabletUrlBarEventListener() {
			
			@Override
			public void onUrlValidated() {
				loadCurrentUrl();
			}
			
			@Override
			public void onHomeClicked() {
				loadHomePage();
			}
			
			@Override
			public void onGoStopReloadClicked() {
				if (mUrlBar.isUrlChangedByUser()) {
					// Use the UIManager to load urls, as it perform check on them.
					loadCurrentUrl();
				} else if (getCurrentWebView().isLoading()) {
					getCurrentWebView().stopLoading();
				} else {
					getCurrentWebView().reload();
				}
			}
			
			@Override
			public void onForwardClicked() {
				if ((!getCurrentWebViewFragment().isStartPageShown()) &&
						(getCurrentWebView().canGoForward())) {
					getCurrentWebView().goForward();
				}
			}
			
			@Override
			public void onBookmarksClicked() {
				openBookmarksActivityForResult();
			}
			
			@Override
			public void onBackClicked() {
				if ((!getCurrentWebViewFragment().isStartPageShown()) &&
						(getCurrentWebView().canGoBack())) {
					getCurrentWebView().goBack();
				}
			}
		});
	}

	@Override
	public CustomWebView getCurrentWebView() {
		if (mActionBar.getSelectedTab() != null) {			
			return mTabs.get(mActionBar.getSelectedTab()).getWebView();
		} else {
			return null;
		}
	}
	
	@Override
	public String getCurrentUrl() {
		return mUrlBar.getUrl();
	}
	
	@Override
	public BaseWebViewFragment getCurrentWebViewFragment() {
		if (mActionBar.getSelectedTab() != null) {
			return mTabs.get(mActionBar.getSelectedTab());
		} else {
			return null;
		}
	}
	
	@Override
	public List<BaseWebViewFragment> getTabs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTab(String url, boolean openInBackground, boolean privateBrowsing) {
		Tab tab = mActionBar.newTab();
		tab.setText(R.string.NewTab);
		
		TabletWebViewFragment fragment = new TabletWebViewFragment();
		
		fragment.init(this, tab, privateBrowsing, url);
		
		tab.setTabListener(new WebViewFragmentTabListener(this, fragment));
		
		mTabs.put(tab, fragment);
		mFragmentsMap.put(fragment.getUUID(), fragment);
		
		mActionBar.addTab(tab, mActionBar.getSelectedNavigationIndex() + 1);
		
		if (!openInBackground) {
			mActionBar.selectTab(tab);
		}
	}
	
	@Override
	public void closeCurrentTab() {
		if (mActionBar.getTabCount() > 1) {
			closeTabByTab(mActionBar.getSelectedTab());
		} else {
			loadHomePage();
		}
	}
	
	@Override
	public void closeTab(UUID tabId) {
		if (mActionBar.getTabCount() > 1) {
			TabletWebViewFragment fragment = (TabletWebViewFragment) getWebViewFragmentByUUID(tabId);
			if (fragment != null) {
				Tab tab = fragment.getTab();
				if (tab != null) {
					closeTabByTab(tab);
				}
			}
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		for (TabletWebViewFragment fragment : mTabs.values()) {
			fragment.getWebView().loadSettings();
		}		
	}
	
	@Override
	public void onMenuVisibilityChanged(boolean isVisible) { }
	
	@Override
	public boolean onKeyBack() {
		if (!super.onKeyBack()) {
			CustomWebView currentWebView = getCurrentWebView();
			
			if ((currentWebView != null) &&
					(currentWebView.canGoBack())) {
				currentWebView.goBack();
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		if (view == getCurrentWebView()) {
			mProgressBar.setProgress(0);
			mProgressBar.setVisibility(View.VISIBLE);
			
			mUrlBar.setUrl(url);
			
			mUrlBar.setGoStopReloadImage(R.drawable.ic_stop);
			
			updateBackForwardEnabled();
		}
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		
		if (view == getCurrentWebView()) {
			mProgressBar.setProgress(100);
			mProgressBar.setVisibility(View.INVISIBLE);			
						
			mUrlBar.setUrl(url);
			
			mUrlBar.setGoStopReloadImage(R.drawable.ic_refresh);
			
			updateBackForwardEnabled();
		}
	}
	
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		if (view == getCurrentWebView()) {
			mProgressBar.setProgress(newProgress);
		}
	}

	@Override
	public void onReceivedTitle(WebView view, String title) {
		for (TabletWebViewFragment fragment : mTabs.values()) {
			fragment.onReceivedTitle(view, title);
		}
	}
	
	@Override
	public void onShowStartPage() {
		mUrlBar.setUrl(null);
		mUrlBar.setBackEnabled(false);
		mUrlBar.setForwardEnabled(false);
		mUrlBar.setGoStopReloadImage(R.drawable.ic_go);
		
		mActionBar.setIcon(R.drawable.ic_launcher);
		
		Tab tab = mActionBar.getSelectedTab();
		tab.setText(R.string.NewTab);
	}
	
	@Override
	public void onHideStartPage() { }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	@Override
	protected int getTabCount() {		
		return mTabs.size();
	}
	
	@Override
	protected BaseWebViewFragment getWebViewFragmentByUUID(UUID fragmentId) {
		return mFragmentsMap.get(fragmentId);
	}
	
	@Override
	public void onActionModeStarted(ActionMode mode) { }

	@Override
	public void onActionModeFinished(ActionMode mode) { }
	
	public StartPageFragment getStartPageFragment() {
		return mStartPageFragment;
	}

	@Override
	protected void showStartPage(BaseWebViewFragment webViewFragment) {
		
		if ((webViewFragment != null) &&
				(!webViewFragment.isStartPageShown())) {
		
			webViewFragment.getWebView().onPause();
			webViewFragment.setStartPageShown(true);
			
			if (webViewFragment == getCurrentWebViewFragment()) {

				FragmentTransaction ft = mFragmentManager.beginTransaction();
				
				ft.hide(webViewFragment);
				ft.show(mStartPageFragment);

				ft.commit();

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

				FragmentTransaction ft = mFragmentManager.beginTransaction();

				ft.hide(mStartPageFragment);
				ft.show(webViewFragment);

				ft.commit();

				onHideStartPage();
			}
		}
	}
	
	@Override
	protected void resetUI() {
		updateUrlBar();		
	}
	
	private void closeTabByTab(Tab tab) {
		TabletWebViewFragment oldFragment = mTabs.get(tab);
		
		if (oldFragment != null) {
			CustomWebView webView = oldFragment.getWebView();
			
			if (!webView.isPrivateBrowsingEnabled()) {
				Controller.getInstance().getAddonManager().onTabClosed(mActivity, webView);
			}

			webView.onPause();
			
			mTabs.remove(tab);
			mFragmentsMap.remove(oldFragment.getUUID());

			mActionBar.removeTab(tab);
		}
	}
	
	private void updateUrlBar() {
		CustomWebView currentWebView;
		BaseWebViewFragment currentFragment = getCurrentWebViewFragment();

		if ((currentFragment != null) &&
				(currentFragment.isStartPageShown())) {
			currentWebView = null;
		} else {
			currentWebView = getCurrentWebView();
		}

		if (currentWebView != null) {
			String url = currentWebView.getUrl();

			if ((url != null) &&
					(!url.isEmpty())) {
				mUrlBar.setUrl(url);
			} else {
				mUrlBar.setUrl(null);
			}

			setApplicationButtonImage(currentWebView.getFavicon());

			if (currentWebView.isLoading()) {
				mUrlBar.setGoStopReloadImage(R.drawable.ic_stop);
				mProgressBar.setVisibility(View.VISIBLE);
			} else {
				mUrlBar.setGoStopReloadImage(R.drawable.ic_refresh);
				mProgressBar.setVisibility(View.GONE);
			}

			updateBackForwardEnabled();
		} else {
			mUrlBar.setUrl(null);
			mUrlBar.setBackEnabled(false);
			mUrlBar.setForwardEnabled(false);

			mActionBar.setIcon(R.drawable.ic_launcher);
		}
		
		mUrlBar.setPrivateBrowsingIndicator(currentFragment != null ? currentFragment.isPrivateBrowsingEnabled() : false);
	}
	
	private void updateBackForwardEnabled() {
		CustomWebView currentWebView = getCurrentWebView();
		
		mUrlBar.setBackEnabled(currentWebView.canGoBack());
		mUrlBar.setForwardEnabled(currentWebView.canGoForward());
	}
}
