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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.components.BadgedImageView;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.fragments.PhoneStartPageFragment2;
import org.tint.ui.fragments.PhoneWebViewFragment;
import org.tint.ui.fragments.StartPageFragment.OnStartPageItemClickedListener;
import org.tint.ui.views.PanelLayout;
import org.tint.ui.views.PanelLayout.PanelEventsListener;
import org.tint.ui.views.PhoneUrlBar;
import org.tint.ui.views.PhoneUrlBar.OnPhoneUrlBarEventListener;
import org.tint.ui.views.TabView;
import org.tint.ui.views.TabsScroller.OnRemoveListener;
import org.tint.utils.Constants;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class PhoneUIManager2 extends BaseUIManager {

	private enum AnimationType {
		NONE,
		FADE
	}
	
	private static final AnimationType sAnimationType = AnimationType.NONE;

	private List<PhoneWebViewFragment> mFragmentsList;
	private Map<UUID, PhoneWebViewFragment> mFragmentsMap;

	private PanelLayout mPanel;
	
	private PhoneUrlBar mUrlBar;
	private RelativeLayout mTopBar;
	
	private BadgedImageView mFaviconView;
	
	private ImageView mBack;
	private ImageView mForward;
	private ImageView mHome;
	
	private ProgressBar mProgressBar;
	
	private int mCurrentTabIndex = -1;
	private Fragment mCurrentFragment = null;
	
	private ActionMode mActionMode;
	
	private TabAdapter mAdapter;

	public PhoneUIManager2(TintBrowserActivity activity) {
		super(activity);
		mFragmentsList = new ArrayList<PhoneWebViewFragment>();
		mFragmentsMap = new HashMap<UUID, PhoneWebViewFragment>();
		
		mAdapter = new TabAdapter();
        mPanel.getTabsScroller().setAdapter(mAdapter);
	}
	
	@Override
	protected void setupUI() {	
		super.setupUI();
		
		mActionBar.hide();
		
		mPanel = (PanelLayout) mActivity.findViewById(R.id.panel_layout);
		
		mPanel.setPanelEventsListener(new PanelEventsListener() {
			
			@Override
			public void onPanelShown() {
				mPanel.getTabsScroller().snapToSelected(mCurrentTabIndex, true);
			}
			
			@Override
			public void onPanelHidden() { }
		});
		
		ImageView openTabView = (ImageView) mActivity.findViewById(R.id.BtnAddTab);
		openTabView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				addTab(true, false);
				
				// Wait for the adapter/scoller to updated before scrolling to the new tab.
				// Maybe find a better way to do this.
				mPanel.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mPanel.getTabsScroller().snapToSelected(mCurrentTabIndex, true);
					}
				}, 50);				
			}
		});
		
		ImageView openBookmarksView = (ImageView) mActivity.findViewById(R.id.BtnBookmarks);
		openBookmarksView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				openBookmarksActivityForResult();
			}
		});
		
		mProgressBar = (ProgressBar) mActivity.findViewById(R.id.WebViewProgress);
		mProgressBar.setIndeterminate(false);
		mProgressBar.setMax(100);
		mProgressBar.setVisibility(View.GONE);
		
		mUrlBar = (PhoneUrlBar) mActivity.findViewById(R.id.UrlBar);		
		
		mUrlBar.setEventListener(new OnPhoneUrlBarEventListener() {
			
			@Override
			public void onVisibilityChanged(boolean urlBarVisible) {
				
			}
			
			@Override
			public void onUrlValidated() {
				loadCurrentUrl();
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
			public void onMenuVisibilityChanged(boolean isVisible) {
				mMenuVisible = isVisible;				
			}
		});
        
        mUrlBar.setTitle(R.string.ApplicationName);
        mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);
		
        mFaviconView = (BadgedImageView) mActivity.findViewById(R.id.FaviconView);
        mFaviconView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mUrlBar.isUrlBarVisible()) {
					mUrlBar.hideUrl();
				} else {
					mPanel.togglePanel();
				}
			}
		});
        
		mTopBar = (RelativeLayout) mActivity.findViewById(R.id.TopBar);
		mTopBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Steal event from WebView.				
			}
		});
		
		mBack = (ImageView) mActivity.findViewById(R.id.BtnBack);
        mBack.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if ((!getCurrentWebViewFragment().isStartPageShown()) &&
				    (getCurrentWebView().canGoBack())) {
					getCurrentWebView().goBack();
					mPanel.hidePanel();
				}
			}
		});
        mBack.setEnabled(false);
        
        mForward = (ImageView) mActivity.findViewById(R.id.BtnForward);
        mForward.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if ((!getCurrentWebViewFragment().isStartPageShown()) &&
				    (getCurrentWebView().canGoForward())) {
					getCurrentWebView().goForward();
					mPanel.hidePanel();
				}
			}
		});
        mForward.setEnabled(false);
        
        mHome = (ImageView) mActivity.findViewById(R.id.BtnHome);
        mHome.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mProgressBar.setProgress(0);
				mProgressBar.setVisibility(View.GONE);
				loadHomePage();
				mPanel.hidePanel();
			}
		});
        
        mPanel.getTabsScroller().setOnRemoveListener(new OnRemoveListener() {
			
			@Override
			public void onRemovePosition(int position) {
				if (mFragmentsList.size() > 1) {
					closeTabByIndex(position);
				} else {
					loadHomePage();
				}
			}
		});
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

		updateUrlBar();
		
		mAdapter.notifyDataSetChanged();
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

	@Override
	public CustomWebView getCurrentWebView() {
		if (mCurrentTabIndex != -1) {
			return mFragmentsList.get(mCurrentTabIndex).getWebView();
		} else {
			return null;
		}
	}

	@Override
	public void loadUrl(String url) {
		mUrlBar.hideUrl();
		super.loadUrl(url);
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
	public boolean onKeyBack() {
		if (!super.onKeyBack()) {
			if (mUrlBar.isUrlBarVisible()) {
				mUrlBar.hideUrl();
				
				return true;
			} else {
				CustomWebView currentWebView = getCurrentWebView();
				
				if ((currentWebView != null) &&
						(currentWebView.canGoBack())) {
					currentWebView.goBack();
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public boolean onKeySearch() {
		mUrlBar.showUrl();
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		for (PhoneWebViewFragment fragment : mFragmentsList) {
			fragment.getWebView().loadSettings();
		}
	}

	@Override
	public void onMenuVisibilityChanged(boolean isVisible) { }
	
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
			mProgressBar.setVisibility(View.GONE);			
						
			mUrlBar.setUrl(url);
			
			mUrlBar.setGoStopReloadImage(R.drawable.ic_refresh);
			
			updateBackForwardEnabled();
		}
		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClientPageFinished(CustomWebView view, String url) {
		super.onClientPageFinished(view, url);
		mAdapter.notifyDataSetChanged();
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
		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onShowStartPage() {
		mUrlBar.setTitle(mActivity.getString(R.string.ApplicationName));
		mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);
		mUrlBar.hideGoStopReloadButton();
					
		mUrlBar.setUrl(null);
		mBack.setEnabled(false);
		mForward.setEnabled(false);
		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onHideStartPage() {
		mUrlBar.showGoStopReloadButton();
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		mActionMode = mode;
	}

	@Override
	public void onActionModeFinished(ActionMode mode) {
		if (mActionMode != null) {
			mActionMode = null;
			
			InputMethodManager mgr = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(null, 0);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if ((requestCode == TintBrowserActivity.ACTIVITY_BOOKMARKS) &&
				(resultCode == Activity.RESULT_OK)) {
			if (mPanel.isPanelShown()) {
				mPanel.hidePanel();
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
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
	protected BaseWebViewFragment getWebViewFragmentByUUID(UUID fragmentId) {
		return mFragmentsMap.get(fragmentId);
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
	protected void setFullScreenFromPreferences() {
		Window win = mActivity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		
		if (PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean(Constants.PREFERENCE_FULL_SCREEN, false)) {
			winParams.flags |=  bits;
		} else {
			winParams.flags &= ~bits;
		}
		
		win.setAttributes(winParams);
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
			String title = currentWebView.getTitle();
			String url = currentWebView.getUrl();
			Bitmap icon = currentWebView.getFavicon();

			if ((title != null) &&
					(!title.isEmpty())) {
				mUrlBar.setTitle(title);
			} else {
				mUrlBar.setTitle(R.string.ApplicationName);
			}

			if ((url != null) &&
					(!url.isEmpty())) {				
				mUrlBar.setSubtitle(url);
				mUrlBar.setUrl(url);
			} else {
				mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);
				mUrlBar.setUrl(null);
			}
			
			setApplicationButtonImage(icon);
			
			if (currentWebView.isLoading()) {
				mProgressBar.setProgress(currentWebView.getProgress());
				mProgressBar.setVisibility(View.VISIBLE);
				mUrlBar.setGoStopReloadImage(R.drawable.ic_stop);
			} else {
				mProgressBar.setVisibility(View.GONE);
				mUrlBar.setGoStopReloadImage(R.drawable.ic_refresh);
			}
			
			updateBackForwardEnabled();
		} else {
			mUrlBar.setTitle(R.string.ApplicationName);
			mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);

			mProgressBar.setVisibility(View.GONE);
						
			mUrlBar.setUrl(null);
			mBack.setEnabled(false);
			mForward.setEnabled(false);
		}
		
		mFaviconView.setValue(mFragmentsList.size());
		
		mUrlBar.setPrivateBrowsingIndicator(currentFragment != null ? currentFragment.isPrivateBrowsingEnabled() : false);
	}

	private void createStartPageFragment() {
		mStartPageFragment = new PhoneStartPageFragment2();
		mStartPageFragment.setOnStartPageItemClickedListener(new OnStartPageItemClickedListener() {					
			@Override
			public void onStartPageItemClicked(String url) {
				loadUrl(url);
			}
		});
	}

	private void setCurrentFragment(Fragment fragment, AnimationType animationType) {
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
	
	private void closeLastTab() {
		PhoneWebViewFragment fragment = mFragmentsList.get(mCurrentTabIndex);
		
		CustomWebView webView = fragment.getWebView();
		
		if (!webView.isPrivateBrowsingEnabled()) {
			Controller.getInstance().getAddonManager().onTabClosed(mActivity, webView);
		}
		
		webView.onPause();
		
		loadHomePage();
		updateUrlBar();
		
		mAdapter.notifyDataSetChanged();
	}
	
	private void closeTabByIndex(int index) {
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

			updateUrlBar();

			mAdapter.notifyDataSetChanged();
		}
	}
	
	private void updateBackForwardEnabled() {
		CustomWebView currentWebView = getCurrentWebView();
		
		mBack.setEnabled(currentWebView.canGoBack());
		mForward.setEnabled(currentWebView.canGoForward());
	}
	
	private void showTabByIndex(int index, boolean notifyTabSwitched) {
		PhoneWebViewFragment oldFragment = mFragmentsList.get(mCurrentTabIndex);
		oldFragment.getWebView().onPause();
		
		TabView oldTabView = mAdapter.getViewAt(mCurrentTabIndex);
		if (oldTabView != null) {
			oldTabView.setSelected(false);
		}
		
		mCurrentTabIndex = index;
		showCurrentTab(notifyTabSwitched);
	}
	
	private void showCurrentTab(boolean notifyTabSwitched) {
		PhoneWebViewFragment newFragment = mFragmentsList.get(mCurrentTabIndex);
		
		if (newFragment.isStartPageShown()) {
			setCurrentFragment(mStartPageFragment, sAnimationType);
			mUrlBar.hideGoStopReloadButton();
		} else {			
			setCurrentFragment(newFragment, sAnimationType);
			mUrlBar.showGoStopReloadButton();
			newFragment.getWebView().onResume();
		}
		
		TabView currentTabView = mAdapter.getViewAt(mCurrentTabIndex);
		if (currentTabView != null) {
			currentTabView.setSelected(true);
		}

		updateUrlBar();
		
		if (notifyTabSwitched) {
			CustomWebView webView = getCurrentWebView();
			
			if (!webView.isPrivateBrowsingEnabled()) {
				Controller.getInstance().getAddonManager().onTabSwitched(mActivity, webView);
			}
		}
	}
	
	private class TabAdapter extends BaseAdapter {
		
		private SparseArray<TabView> mViews;
		
		public TabAdapter() {
			super();
			mViews = new SparseArray<TabView>();
		}

		@Override
		public int getCount() {
			return mFragmentsList.size();
		}

		@Override
		public PhoneWebViewFragment getItem(int position) {
			return mFragmentsList.get(position);
		}

		@Override
		public long getItemId(int position) {			
			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			mViews.clear();
			super.notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final TabView tabview = new TabView(mActivity);
			
			PhoneWebViewFragment fragment = getItem(position);
			
			if (fragment.isStartPageShown()) {
				tabview.setTitle(R.string.StartPageLabel);
			} else {
				CustomWebView webView = fragment.getWebView();
				
				tabview.setTitle(webView.getTitle());
				tabview.setImage(webView.isLoading() ? null : webView.capturePicture());
			}
			
			tabview.setSelected(position == mCurrentTabIndex);
			
			tabview.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (tabview.isClose(v)) {
						mPanel.getTabsScroller().animateOut(tabview);
					} else {
						showTabByIndex(position, true);
						mPanel.hidePanel();
					}
				}
			});
			
			mViews.put(position, tabview);
			
			return tabview;
		}
		
		public TabView getViewAt(int position) {
			return mViews.get(position);
		}
		
	}

}
