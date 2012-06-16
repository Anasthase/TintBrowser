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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.components.CustomWebView;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.fragments.PhoneWebViewFragment;
import org.tint.ui.fragments.StartPageFragment;
import org.tint.ui.fragments.StartPageFragment.OnStartPageItemClickedListener;
import org.tint.ui.views.PhoneUrlBar;
import org.tint.ui.views.PhoneUrlBar.OnPhoneUrlBarEventListener;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class PhoneUIManager2 extends BaseUIManager {

	private List<PhoneWebViewFragment> mFragmentsList;
	private Map<UUID, PhoneWebViewFragment> mFragmentsMap;
	
	private PhoneUrlBar mUrlBar;
	
	private ImageView mFaviconView;
	
	private RelativeLayout mTopBar;
	private LinearLayout mLateralBar;
	
	private ProgressBar mProgressBar;
	
	private ImageView mHome;
	
	private ImageView mBack;
	private ImageView mForward;
	
	private ImageView mBookmarks;
	
	private ImageView mAddTab;
	private ImageView mCloseTab;
	
	private ImageView mShowPreviousTab;
	private ImageView mShowNextTab;
	
	private BitmapDrawable mDefaultFavicon;
	
	private ActionMode mActionMode;
	
	private ToolbarsAnimator2 mToolbarsAnimator;
	
	private int mCurrentTabIndex = -1;
	
	public PhoneUIManager2(TintBrowserActivity activity) {
		super(activity);
		
		mFragmentsList = new ArrayList<PhoneWebViewFragment>();
		mFragmentsMap = new Hashtable<UUID, PhoneWebViewFragment>();
	}

	@Override
	public void addTab(String url, boolean openInBackground, boolean privateBrowsing) {
		int previousIndex = mCurrentTabIndex;
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		PhoneWebViewFragment fragment = new PhoneWebViewFragment();
		
		fragment.init(this, privateBrowsing, url);
		
		fragmentTransaction.add(R.id.WebViewContainer, fragment);
		fragmentTransaction.commit();
		
		//mCurrentTabIndex++;
		mFragmentsList.add(mCurrentTabIndex + 1, fragment);
		mFragmentsMap.put(fragment.getUUID(), fragment);
		
		if (!openInBackground) {
			mCurrentTabIndex++;
			showCurrentTab(previousIndex, false);
		}
	}

	@Override
	public void closeCurrentTab() {
		if (mFragmentsList.size() > 1) {
			closeTabByIndex(mCurrentTabIndex);
		} else {
			loadHomePage();
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
			loadHomePage();
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
	public BaseWebViewFragment getCurrentWebViewFragment() {
		if (mCurrentTabIndex != -1) {
			return mFragmentsList.get(mCurrentTabIndex);
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		for (PhoneWebViewFragment fragment : mFragmentsList) {
			fragment.getWebView().loadSettings();
		}
		
		if (Constants.PREFERENCE_BUBBLE_POSITION.equals(key)) {
//			updateBubblesVisibility();
		} else if (Constants.PREFERENCE_TOOLBARS_AUTOHIDE_DURATION.equals(key)) {
//			updateToolbarsDisplayDuration();
		} else if (Constants.PREFERENCES_SWITCH_TABS_METHOD.equals(key)) {
//			updateSwitchTabsMethod();			
//			updateShowPreviousNextTabButtons();
		}
	}

	@Override
	public void onMenuVisibilityChanged(boolean isVisible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		if (view == getCurrentWebView()) {
			
			closeLateralBarIfNecessary();
			
			mProgressBar.setVisibility(View.VISIBLE);
			mFaviconView.setVisibility(View.INVISIBLE);
			
			mUrlBar.setUrl(url);
			
			mUrlBar.setGoStopReloadImage(R.drawable.ic_stop);
			
			updateBackForwardEnabled();
		}
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		
		if (view == getCurrentWebView()) {
			mFaviconView.setVisibility(View.VISIBLE);
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
		mUrlBar.hideGoStopReloadButton();
		
		mFaviconView.setImageDrawable(mDefaultFavicon);
					
		mUrlBar.setUrl(null);
		mBack.setEnabled(false);
		mForward.setEnabled(false);
	}

	@Override
	public void onHideStartPage() {
		mUrlBar.showGoStopReloadButton();
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		mActionMode = mode;

//		mTopBar.animate().translationY(mTopBar.getHeight());
	}

	@Override
	public void onActionModeFinished(ActionMode mode) {
		if (mActionMode != null) {
			mActionMode = null;
			
//			mTopBar.animate().translationY(0);
			
			InputMethodManager mgr = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(null, 0);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		closeLateralBarIfNecessary();
		
		return false;
	}

	@Override
	protected void setupUI() {
		mActionBar.hide();
		
		int buttonSize = mActivity.getResources().getInteger(R.integer.application_button_size);
		Drawable d = mActivity.getResources().getDrawable(R.drawable.ic_launcher);
		
		Bitmap bm = Bitmap.createBitmap(buttonSize, buttonSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		d.setBounds(0, 0, buttonSize, buttonSize);
		d.draw(canvas);
		
		mDefaultFavicon = new BitmapDrawable(mActivity.getResources(), bm);
		
		mProgressBar = (ProgressBar) mActivity.findViewById(R.id.WebViewProgress);
		mProgressBar.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mUrlBar.isUrlBarVisible()) {
					mUrlBar.hideUrl();
				} else {
//					loadHomePage();
					mToolbarsAnimator.toggle();					
				}
			}
		});
		
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
		
        mFaviconView = (ImageView) mActivity.findViewById(R.id.FaviconView);
        mFaviconView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mUrlBar.isUrlBarVisible()) {
					mUrlBar.hideUrl();
				} else {
//					loadHomePage();
					mToolbarsAnimator.toggle();					
				}
			}
		});
        
        mFaviconView.setImageDrawable(mDefaultFavicon);
        
		mTopBar = (RelativeLayout) mActivity.findViewById(R.id.TopBar);
		mTopBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Steal event from WebView.				
			}
		});
		
		mLateralBar = (LinearLayout) mActivity.findViewById(R.id.LateralBar);
		mLateralBar.setVisibility(View.GONE);
		mLateralBar.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				// Steal event from WebView.				
			}
		});
		
		mHome = (ImageView) mActivity.findViewById(R.id.BtnHome);
		mHome.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				loadHomePage();
				closeLateralBarIfNecessary();
			}
		});
		
		mBack = (ImageView) mActivity.findViewById(R.id.BtnBack);
        mBack.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if ((!getCurrentWebViewFragment().isStartPageShown()) &&
				    (getCurrentWebView().canGoBack())) {
					getCurrentWebView().goBack();
				}
				
//				closeLateralBarIfNecessary();
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
				}
				
//				closeLateralBarIfNecessary();
			}
		});
        mForward.setEnabled(false);
        
        mBookmarks = (ImageView) mActivity.findViewById(R.id.BtnBookmarks);
        mBookmarks.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				openBookmarksActivityForResult();
				closeLateralBarIfNecessary();
			}
		});
        
        mAddTab = (ImageView) mActivity.findViewById(R.id.BtnAddTab);
        mAddTab.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				addTab(true, false);
				closeLateralBarIfNecessary();
			}
		});
        
        mCloseTab = (ImageView) mActivity.findViewById(R.id.BtnCloseTab);
        mCloseTab.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				closeCurrentTab();
				closeLateralBarIfNecessary();
			}
		});
        
        mShowPreviousTab = (ImageView) mActivity.findViewById(R.id.PreviousTabView);
        mShowPreviousTab.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				showPreviousTab();
//				closeLateralBarIfNecessary();
			}
		});
        
        mShowNextTab = (ImageView) mActivity.findViewById(R.id.NextTabView);
        mShowNextTab.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showNextTab();
//				closeLateralBarIfNecessary();
			}
		});
		
		mToolbarsAnimator = new ToolbarsAnimator2(mLateralBar);
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
	protected void setApplicationButtonImage(Bitmap icon) {
		BitmapDrawable image = ApplicationUtils.getApplicationButtonImage(mActivity, icon);
		
		if (image != null) {			
			mFaviconView.setImageDrawable(image);
		} else {
			mFaviconView.setImageDrawable(mDefaultFavicon);
		}
	}
	
	@Override
	protected void showStartPage(BaseWebViewFragment webViewFragment) {
		if ((webViewFragment != null) &&
				(!webViewFragment.isStartPageShown())) {
		
			webViewFragment.getWebView().onPause();
			webViewFragment.setStartPageShown(true);
			
			if (webViewFragment == getCurrentWebViewFragment()) {

				FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

				if (mStartPageFragment == null) {
					mStartPageFragment = new StartPageFragment();
					mStartPageFragment.setOnStartPageItemClickedListener(new OnStartPageItemClickedListener() {					
						@Override
						public void onStartPageItemClicked(String url) {
							loadUrl(url);
						}
					});

					fragmentTransaction.add(R.id.WebViewContainer, mStartPageFragment);
				}

				fragmentTransaction.hide(webViewFragment);
				fragmentTransaction.show(mStartPageFragment);

				fragmentTransaction.commit();

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

				FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();			

				fragmentTransaction.hide(mStartPageFragment);
				fragmentTransaction.show(webViewFragment);

				fragmentTransaction.commit();

				onHideStartPage();
			}
		}
	}

	@Override
	protected void resetUI() {
		updateUrlBar();
	}
	
	private void updateBackForwardEnabled() {
		CustomWebView currentWebView = getCurrentWebView();
		
		mBack.setEnabled(currentWebView.canGoBack());
		mForward.setEnabled(currentWebView.canGoForward());
	}
	
	private void closeLateralBarIfNecessary() {
		if (mToolbarsAnimator.isToolbarsVisible()) {
			mToolbarsAnimator.startHideAnimation();
		}
	}
	
	private void showPreviousTab() {
		if (mCurrentTabIndex > 0) {
			mUrlBar.hideUrl();
			
			int previousIndex = mCurrentTabIndex;
			mCurrentTabIndex--;
			
			showCurrentTab(previousIndex, true);
		}
	}
	
	private void showNextTab() {
		if (mCurrentTabIndex < mFragmentsList.size() - 1) {
			mUrlBar.hideUrl();
			
			int previousIndex = mCurrentTabIndex;
			mCurrentTabIndex++;
			
			showCurrentTab(previousIndex, true);
		}
	}
	
	private void showCurrentTab(int previousIndex, boolean notifyTabSwitched) {
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		
		if (previousIndex != -1) {
			PhoneWebViewFragment oldFragment = mFragmentsList.get(previousIndex);
			if (oldFragment.isStartPageShown()) {
				fragmentTransaction.hide(mStartPageFragment);
			} else {
				fragmentTransaction.hide(oldFragment);
			}
		}
		
		PhoneWebViewFragment newFragment = mFragmentsList.get(mCurrentTabIndex);
		
		if (newFragment.isStartPageShown()) {
			fragmentTransaction.show(mStartPageFragment);
			mUrlBar.hideGoStopReloadButton();
		} else {
			fragmentTransaction.show(newFragment);
			mUrlBar.showGoStopReloadButton();
		}
		
		fragmentTransaction.commit();

		updateShowPreviousNextTabButtons();
		updateUrlBar();
		
		if (notifyTabSwitched) {
			CustomWebView webView = getCurrentWebView();
			
			if (!webView.isPrivateBrowsingEnabled()) {
				Controller.getInstance().getAddonManager().onTabSwitched(mActivity, webView);
			}
		}
	}
	
	private void closeTabByIndex(int index) {
		boolean currentTab = index == mCurrentTabIndex;		
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		PhoneWebViewFragment fragment = mFragmentsList.get(index);
		
		CustomWebView webView = fragment.getWebView();
		
		if (!webView.isPrivateBrowsingEnabled()) {
			Controller.getInstance().getAddonManager().onTabClosed(mActivity, webView);
		}
		
		webView.onPause();
		
		if (fragment.isStartPageShown()) {
			fragmentTransaction.hide(mStartPageFragment);
		} else {
			//fragmentTransaction.remove(fragment);
		}
		// TODO: Check this.
		fragmentTransaction.remove(fragment);
		
		fragmentTransaction.commit();
		
		mFragmentsList.remove(index);
		mFragmentsMap.remove(fragment.getUUID());
		
		if (currentTab) {
			if (mCurrentTabIndex > 0) {
				mCurrentTabIndex--;
			}
			
			showCurrentTab(-1, true);
		} else {
			if (index < mCurrentTabIndex) {
				mCurrentTabIndex--;
			}
		}
	}
	
	private void updateShowPreviousNextTabButtons() {
		if (mCurrentTabIndex == 0) {
			mShowPreviousTab.setEnabled(false);
			
			if (mFragmentsList.size() > 1) {
				mShowNextTab.setEnabled(true);
			} else {
				mShowNextTab.setEnabled(false);
			}
		} else {
			mShowPreviousTab.setEnabled(true);
			if (mCurrentTabIndex < mFragmentsList.size() - 1) {
				mShowNextTab.setEnabled(true);
			} else {
				mShowNextTab.setEnabled(false);
			}
		}
	}
	
	private void updateUrlBar() {
		CustomWebView currentWebView;
		BaseWebViewFragment currentFragment = getCurrentWebViewFragment();
		
		if ((currentFragment != null) &&
				(currentFragment.isStartPageShown())) {
			currentWebView = null;
			
//			if (!mToolbarsAnimator.isToolbarsVisible()) {
//				setToolbarsVisibility(true);
//			}
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
				mProgressBar.setVisibility(View.VISIBLE);
				mFaviconView.setVisibility(View.INVISIBLE);
			} else {
				mFaviconView.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);				
			}
			
			updateBackForwardEnabled();
		} else {
			mUrlBar.setTitle(R.string.ApplicationName);
			mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);
			mFaviconView.setImageDrawable(mDefaultFavicon);
			
			mFaviconView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
						
			mUrlBar.setUrl(null);
			mBack.setEnabled(false);
			mForward.setEnabled(false);
		}
		
		mUrlBar.setPrivateBrowsingIndicator(currentFragment != null ? currentFragment.isPrivateBrowsingEnabled() : false);
	}

}
