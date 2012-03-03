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
import org.tint.addons.framework.Action;
import org.tint.addons.framework.Callbacks;
import org.tint.utils.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ResolveInfo;
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
	private String mContact;
	
	private String mPreferenceName;
	
	private ResolveInfo mResolveInfo;
	
	private boolean mIsEnabled;
	private int mCallbacks;
	
	private AddonServiceConnection mServiceConnection;
	
	private OnSharedPreferenceChangeListener mPreferenceChangeListener;
	
	public Addon(Context context, int id, ResolveInfo resolveInfo, String category) {
		mContext = context;
		mId = id;
		mResolveInfo = resolveInfo;
		
		mIsEnabled = false;
		mCallbacks = 0;
		
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
	
	public ResolveInfo getResolveInfo() {
		return mResolveInfo;
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
	
	public String getContact() {
		return mContact;
	}
	
	public boolean isEnabled() {
		return mIsEnabled;
	}
	
	public boolean hasSettingsPage() {
		return (mCallbacks & Callbacks.HAS_SETTINGS_PAGE) == Callbacks.HAS_SETTINGS_PAGE;
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
	
	public List<Action> onPageStarted(String tabId, String url) {
		if (makeCall(Callbacks.PAGE_STARTED)) {
			return mServiceConnection.onPageStarted(tabId, url);
		} else {
			return null;
		}
	}
	
	public List<Action> onPageFinished(String tabId, String url) {
		if (makeCall(Callbacks.PAGE_FINISHED)) {
			return mServiceConnection.onPageFinished(tabId, url);
		} else {
			return null;
		}
	}
	
	public List<Action> onTabOpened(String tabId) {
		if (makeCall(Callbacks.TAB_OPENED)) {
			return mServiceConnection.onTabOpened(tabId);
		} else {
			return null;
		}
	}
	
	public List<Action> onTabClosed(String tabId) {
		if (makeCall(Callbacks.TAB_CLOSED)) {
			return mServiceConnection.onTabClosed(tabId);
		} else {
			return null;
		}
	}
	
	public List<Action> onTabSwitched(String tabId) {
		if (makeCall(Callbacks.TAB_SWITCHED)) {
			return mServiceConnection.onTabSwitched(tabId);
		} else {
			return null;
		}
	}
	
	public String getContributedMainMenuItem(String currentTabId, String currentTitle, String currentUrl) {
		if (makeCall(Callbacks.CONTRIBUTE_MAIN_MENU)) {
			return mServiceConnection.getContributedMainMenuItem(currentTabId, currentTitle, currentUrl); 
		} else {
			return null;
		}
	}
	
	public List<Action> onContributedMainMenuItemSelected(String currentTabId, String currentTitle, String currentUrl) {
		if (makeCall(Callbacks.CONTRIBUTE_MAIN_MENU)) {
			return mServiceConnection.onContributedMainMenuItemSelected(currentTabId, currentTitle, currentUrl);
		} else {
			return null;
		}
	}
	
	public String getContributedLinkContextMenuItem(String currentTabId, int hitTestResult, String url) {
		if (makeCall(Callbacks.CONTRIBUTE_LINK_CONTEXT_MENU)) {
				return mServiceConnection.getContributedLinkContextMenuItem(currentTabId, hitTestResult, url);
		} else {
			return null;
		}
	}
	
	public List<Action> onContributedLinkContextMenuItemSelected(String currentTabId, int hitTestResult, String url) {
		if (makeCall(Callbacks.CONTRIBUTE_LINK_CONTEXT_MENU)) {
			return mServiceConnection.onContributedLinkContextMenuItemSelected(currentTabId, hitTestResult, url);
		} else {
			return null;
		}
	}
	
	public String getContributedHistoryBookmarksMenuItem(String currentTabId) {
		if (makeCall(Callbacks.CONTRIBUTE_HISTORY_BOOKMARKS_MENU)) {
			return mServiceConnection.getContributedHistoryBookmarksMenuItem(currentTabId);
		} else {
			return null;
		}
	}
	
	public List<Action> onContributedHistoryBookmarksMenuItemSelected(String currentTabId) {
		return mServiceConnection.onContributedHistoryBookmarksMenuItemSelected(currentTabId);
	}
	
	public String getContributedBookmarkContextMenuItem(String currentTabId) {
		if (makeCall(Callbacks.CONTRIBUTE_BOOKMARK_CONTEXT_MENU)) {
			return mServiceConnection.getContributedBookmarkContextMenuItem(currentTabId);
		} else {
			return null;
		}
	}
	
	public List<Action> onContributedBookmarkContextMenuItemSelected(String currentTabId, String title, String url) {
		if (makeCall(Callbacks.CONTRIBUTE_BOOKMARK_CONTEXT_MENU)) {
			return mServiceConnection.onContributedBookmarkContextMenuItemSelected(currentTabId, title, url);
		} else {
			return null;
		}
	}
	
	public String getContributedHistoryContextMenuItem(String currentTabId) {
		if (makeCall(Callbacks.CONTRIBUTE_HISTORY_CONTEXT_MENU)) {
			return mServiceConnection.getContributedHistoryContextMenuItem(currentTabId);
		} else {
			return null;
		}
	}
	
	public List<Action> onContributedHistoryContextMenuItemSelected(String currentTabId, String title, String url) {
		if (makeCall(Callbacks.CONTRIBUTE_HISTORY_CONTEXT_MENU)) {
			return mServiceConnection.onContributedHistoryContextMenuItemSelected(currentTabId, title, url);
		} else {
			return null;
		}
	}
	
	public List<Action> onUserConfirm(String currentTabId, String questionId, boolean positiveAnswer) {
		if (makeCallWithoutSpecificCallback()) {
			return mServiceConnection.onUserConfirm(currentTabId, questionId, positiveAnswer);
		} else {
			return null;
		}
	}
	
	public List<Action> onUserInput(String currentTabId, String questionId, boolean cancelled, String userInput) {
		if (makeCallWithoutSpecificCallback()) {
			return mServiceConnection.onUserInput(currentTabId, questionId, cancelled, userInput);
		} else {
			return null;
		}
	}
	
	public List<Action> onUserChoice(String currentTabId, String questionId, boolean cancelled, int userChoice) {
		if (makeCallWithoutSpecificCallback()) {
			return mServiceConnection.onUserChoice(currentTabId, questionId, cancelled, userChoice);
		} else {
			return null;
		}
	}
	
	public void showAddonSettingsActivity() {
		if (makeCallEvenDisabled(Callbacks.HAS_SETTINGS_PAGE)) {
			mServiceConnection.showAddonSettingsActivity();
		}
	}
	
	public List<String> getUserReadbleCallbacks() {
		List<String> results = new ArrayList<String>();
		
		if ((mCallbacks & Callbacks.PAGE_STARTED) == Callbacks.PAGE_STARTED) {
			results.add(mContext.getString(R.string.AddonCallbackPageStarted));
		}
		
		if ((mCallbacks & Callbacks.PAGE_FINISHED) == Callbacks.PAGE_FINISHED) {
			results.add(mContext.getString(R.string.AddonCallbackPageFinished));
		}
		
		if ((mCallbacks & Callbacks.TAB_OPENED) == Callbacks.TAB_OPENED) {
			results.add(mContext.getString(R.string.AddonCallbackTabOpened));
		}
		
		if ((mCallbacks & Callbacks.TAB_CLOSED) == Callbacks.TAB_CLOSED) {
			results.add(mContext.getString(R.string.AddonCallbackTabClosed));
		}
		
		if ((mCallbacks & Callbacks.TAB_SWITCHED) == Callbacks.TAB_SWITCHED) {
			results.add(mContext.getString(R.string.AddonCallbackTabSwitched));
		}

		if ((mCallbacks & Callbacks.CONTRIBUTE_MAIN_MENU) == Callbacks.CONTRIBUTE_MAIN_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeMainMenu));
		}
		
		if ((mCallbacks & Callbacks.CONTRIBUTE_LINK_CONTEXT_MENU) == Callbacks.CONTRIBUTE_LINK_CONTEXT_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeLinkContextMenu));
		}
		
		if ((mCallbacks & Callbacks.CONTRIBUTE_HISTORY_BOOKMARKS_MENU) == Callbacks.CONTRIBUTE_HISTORY_BOOKMARKS_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeHistoryBookmarksMenu));
		}
		
		if ((mCallbacks & Callbacks.CONTRIBUTE_BOOKMARK_CONTEXT_MENU) == Callbacks.CONTRIBUTE_BOOKMARK_CONTEXT_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeBookmarkContextMenu));
		}
		
		if ((mCallbacks & Callbacks.CONTRIBUTE_HISTORY_CONTEXT_MENU) == Callbacks.CONTRIBUTE_HISTORY_CONTEXT_MENU) {
			results.add(mContext.getString(R.string.AddonCallbackContributeHistoryContextMenu));
		}
		
		if ((mCallbacks & Callbacks.HAS_SETTINGS_PAGE) == Callbacks.HAS_SETTINGS_PAGE) {
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
		mContact = mServiceConnection.getContact();
		
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
