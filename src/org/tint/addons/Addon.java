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

package org.tint.addons;

import java.util.ArrayList;
import java.util.List;

import org.tint.R;
import org.tint.addons.AddonServiceConnection.AddonServiceConnectionListener;
import org.tint.addons.framework.AddonCallbacks;
import org.tint.addons.framework.AddonResponse;
import org.tint.utils.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Addon {
	
	private Context mContext;
	
	/**
	 * The addon id is its position in the addons array in the AddonManager.
	 */
	private int mId;
	
	private String mName;
	private String mShortDescription;
	private String mDescription;
	private String mEMail;
	private String mWebsite;
	
	private String mContributedMainMenu;
	private String mContributedLinkContextMenu;	
	private String mContributedHistoryBookmarksMenu;
	private String mContributedBookmarkContextMenu;
	private String mContributedHistoryContextMenu;
	
	private String mPreferenceName;
	
	private boolean mIsEnabled;
	private int mCallbacks;
	
	private AddonServiceConnection mServiceConnection;
	
	private OnSharedPreferenceChangeListener mPreferenceChangeListener;
	
	public Addon(Context context, int id, String category) {
		mContext = context;
		mId = id;
		
		mIsEnabled = false;
		mCallbacks = 0;
		
		mContributedMainMenu = null;
		mContributedLinkContextMenu = null;
		mContributedHistoryBookmarksMenu = null;
		mContributedBookmarkContextMenu = null;
		mContributedHistoryContextMenu = null;
		
		mPreferenceChangeListener = null;
		
		Intent i = new Intent(AddonManager.ACTION_ADDON);
		i.addCategory(category);
		
		mServiceConnection = new AddonServiceConnection(i);
		
		mServiceConnection.setAddonServiceConnectionListener(new AddonServiceConnectionListener() {			
			@Override
			public void onServiceConnected() {
				init();
			}
		});
		
		mContext.bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);				
	}
	
	/**
	 * Gets the menu id for this addon.
	 * I'm not sure how to avoid conflicts between Android generated menu ids,
	 * and the ones used when an addon contribute to a menu.
	 * For now, make the assumption that Android do not use negatives ids, and start from - 1000.
	 * The real addon id can be retrived by the inverse operation.
	 * @return The menu id to use when this addon contribute to a menu. 
	 */
	public int getMenuId() {
		return - (1000 + mId);
	}
	
	public String getName() {
		return mName;
	}
	
	public String getShortDescription() {
		return mShortDescription;
	}
	
	public String getDescription() {
		return mDescription;
	}
	
	public String getEMail() {
		return mEMail;
	}
	
	public String getWebsite() {
		return mWebsite;
	}
	
	public boolean isEnabled() {
		return mIsEnabled;
	}
	
	public boolean hasPreferencePage() {
		return (mCallbacks & AddonCallbacks.HAS_PREFERENCES_PAGE) == AddonCallbacks.HAS_PREFERENCES_PAGE;
	}
	
	public void setEnabled(boolean value) {
		mIsEnabled = value;
		
		Editor edit = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		edit.putBoolean(mPreferenceName, mIsEnabled);
		edit.commit();
	}
	
	public void unbindService() {
		mIsEnabled = false;
		PreferenceManager.getDefaultSharedPreferences(mContext).unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
		
		mContext.unbindService(mServiceConnection);
	}
	
	public AddonResponse onPageStarted(String url) {
		if (makeCall(AddonCallbacks.PAGE_STARTED)) {
			return mServiceConnection.onPageStarted(url);
		} else {
			return null;
		}
	}
	
	public AddonResponse onPageFinished(String url) {
		if (makeCall(AddonCallbacks.PAGE_FINISHED)) {
			return mServiceConnection.onPageFinished(url);
		} else {
			return null;
		}
	}
	
	public String getContributedMainMenuItem() {
		if (makeCall(AddonCallbacks.CONTRIBUTE_MAIN_MENU)) {
			if (mContributedMainMenu == null) {
				mContributedMainMenu = mServiceConnection.getContributedMainMenuItem(); 
			}
			
			return mContributedMainMenu;
		} else {
			return null;
		}
	}
	
	public AddonResponse onContributedMainMenuItemSelected(String currentTitle, String currentUrl) {
		if (makeCall(AddonCallbacks.CONTRIBUTE_MAIN_MENU)) {
			return mServiceConnection.onContributedMainMenuItemSelected(currentTitle, currentUrl);
		} else {
			return null;
		}
	}
	
	public String getContributedLinkContextMenuItem() {
		if (makeCall(AddonCallbacks.CONTRIBUTE_LINK_CONTEXT_MENU)) {
			if (mContributedLinkContextMenu == null) {
				mContributedLinkContextMenu = mServiceConnection.getContributedLinkContextMenuItem();
			}
			
			return mContributedLinkContextMenu;
		} else {
			return null;
		}
	}
	
	public AddonResponse onContributedLinkContextMenuItemSelected(int hitTestResult, String url) {
		if (makeCall(AddonCallbacks.CONTRIBUTE_LINK_CONTEXT_MENU)) {
			return mServiceConnection.onContributedLinkContextMenuItemSelected(hitTestResult, url);
		} else {
			return null;
		}
	}
	
	public String getContributedHistoryBookmarksMenuItem() {
		if (makeCall(AddonCallbacks.CONTRIBUTE_HISTORY_BOOKMARKS_MENU)) {
			if (mContributedHistoryBookmarksMenu == null) {
				mContributedHistoryBookmarksMenu = mServiceConnection.getContributedHistoryBookmarksMenuItem(); 
			}
			
			return mContributedHistoryBookmarksMenu;
		} else {
			return null;
		}
	}
	
	public AddonResponse onContributedHistoryBookmarksMenuItemSelected() {
		if (makeCall(AddonCallbacks.CONTRIBUTE_HISTORY_BOOKMARKS_MENU)) {
			return mServiceConnection.onContributedHistoryBookmarksMenuItemSelected();
		} else {
			return null;
		}
	}
	
	public String getContributedBookmarkContextMenuItem() {
		if (makeCall(AddonCallbacks.CONTRIBUTE_BOOKMARK_CONTEXT_MENU)) {
			if (mContributedBookmarkContextMenu == null) {
				mContributedBookmarkContextMenu = mServiceConnection.getContributedBookmarkContextMenuItem();
			}
			
			return mContributedBookmarkContextMenu;
		} else {
			return null;
		}
	}
	
	public AddonResponse onContributedBookmarkContextMenuItemSelected(String title, String url) {
		if (makeCall(AddonCallbacks.CONTRIBUTE_BOOKMARK_CONTEXT_MENU)) {
			return mServiceConnection.onContributedBookmarkContextMenuItemSelected(title, url);
		} else {
			return null;
		}
	}
	
	public String getContributedHistoryContextMenuItem() {
		if (makeCall(AddonCallbacks.CONTRIBUTE_HISTORY_CONTEXT_MENU)) {
			if (mContributedHistoryContextMenu == null) {
				mContributedHistoryContextMenu = mServiceConnection.getContributedHistoryContextMenuItem();
			}
			
			return mContributedHistoryContextMenu;
		} else {
			return null;
		}
	}
	
	public AddonResponse onContributedHistoryContextMenuItemSelected(String title, String url) {
		if (makeCall(AddonCallbacks.CONTRIBUTE_HISTORY_CONTEXT_MENU)) {
			return mServiceConnection.onContributedHistoryContextMenuItemSelected(title, url);
		} else {
			return null;
		}
	}
	
	public AddonResponse onUserAnswerQuestion(String questionId, boolean positiveAnswer) {
		if (makeCallWithoutSpecificCallback()) {
			return mServiceConnection.onUserAnswerQuestion(questionId, positiveAnswer);
		} else {
			return null;
		}
	}
	
	public void showAddonPreferenceActivity() {
		if (makeCallEvenDisabled(AddonCallbacks.HAS_PREFERENCES_PAGE)) {
			mServiceConnection.showAddonPreferenceActivity();
		}
	}
	
	public List<String> getUserReadbleCallbacks() {
		List<String> results = new ArrayList<String>();
		
		if ((mCallbacks & AddonCallbacks.PAGE_STARTED) == AddonCallbacks.PAGE_STARTED) {
			results.add(mContext.getString(R.string.AddonCallbackPageStarted));
		}
		
		if ((mCallbacks & AddonCallbacks.PAGE_FINISHED) == AddonCallbacks.PAGE_FINISHED) {
			results.add(mContext.getString(R.string.AddonCallbackPageFinished));
		}		

		if ((mCallbacks & AddonCallbacks.CONTRIBUTE_MAIN_MENU) == AddonCallbacks.CONTRIBUTE_MAIN_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeMainMenu));
		}
		
		if ((mCallbacks & AddonCallbacks.CONTRIBUTE_LINK_CONTEXT_MENU) == AddonCallbacks.CONTRIBUTE_LINK_CONTEXT_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeLinkContextMenu));
		}
		
		if ((mCallbacks & AddonCallbacks.CONTRIBUTE_HISTORY_BOOKMARKS_MENU) == AddonCallbacks.CONTRIBUTE_HISTORY_BOOKMARKS_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeHistoryBookmarksMenu));
		}
		
		if ((mCallbacks & AddonCallbacks.CONTRIBUTE_BOOKMARK_CONTEXT_MENU) == AddonCallbacks.CONTRIBUTE_BOOKMARK_CONTEXT_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeBookmarkContextMenu));
		}
		
		if ((mCallbacks & AddonCallbacks.CONTRIBUTE_HISTORY_CONTEXT_MENU) == AddonCallbacks.CONTRIBUTE_HISTORY_CONTEXT_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeHistoryContextMenu));
		}
		
		if ((mCallbacks & AddonCallbacks.HAS_PREFERENCES_PAGE) == AddonCallbacks.HAS_PREFERENCES_PAGE) {
			results.add(mContext.getString(R.string.AddonCallbackHasPreferencesPage));
		}
		
		return results;
	}
	
	private boolean makeCall(int callback) {
		return (mIsEnabled && mServiceConnection.isBinded() && (mCallbacks & callback) == callback);
	}
	
	private boolean makeCallEvenDisabled(int callback) {
		return (mServiceConnection.isBinded() && (mCallbacks & callback) == callback);
	}
	
	private boolean makeCallWithoutSpecificCallback() {
		return mIsEnabled && mServiceConnection.isBinded();
	}
	
	private void init() {
		mIsEnabled = false;
		mPreferenceChangeListener = null;
		
		mCallbacks = mServiceConnection.getCallbacks();
		
		mName = mServiceConnection.getName();
		mShortDescription = mServiceConnection.getShortDescription();
		mDescription = mServiceConnection.getDescription();
		mEMail = mServiceConnection.getEMail();
		mWebsite = mServiceConnection.getWebsite();
		
		if (!TextUtils.isEmpty(mName)) {
			mPreferenceName = Constants.TECHNICAL_PREFERENCE_ADDON_ENABLED + mName.toUpperCase().replace(" ", "_");
			mIsEnabled = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(mPreferenceName, true);
			
			mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					if (mPreferenceName.equals(key)) {
						mIsEnabled = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(mPreferenceName, true);
					}
				}				
			};
			
			PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
		}
	}

}
