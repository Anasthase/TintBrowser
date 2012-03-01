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

import java.util.List;

import org.tint.addons.framework.Action;
import org.tint.addons.framework.IAddon;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class AddonServiceConnection implements ServiceConnection {

	public interface AddonServiceConnectionListener {
		void onServiceConnected();
	}
	
	private Intent mIntent;
	private IAddon mAddon;
	
	private boolean mIsBinded;
	
	private AddonServiceConnectionListener mListener;
	
	public AddonServiceConnection(Intent intent) {
		mIntent = intent;
		mAddon = null;
		mIsBinded = false;
		mListener = null;
	}
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder boundService) {
		mAddon = IAddon.Stub.asInterface(boundService);
		mIsBinded = true;

		try {			
			mAddon.onBind();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (mListener != null) {
			mListener.onServiceConnected();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName className) {
		
		try {
			mAddon.onUnbind();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		mAddon = null;
		mIsBinded = false;
	}
	
	public void setAddonServiceConnectionListener(AddonServiceConnectionListener listener) {
		mListener = listener;
	}
	
	public int getCallbacks() {
		try {
			return mAddon.getCallbacks();
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public String getName() {
		try {
			return mAddon.getName();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public String getShortDescription() {
		try {
			return mAddon.getShortDescription();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public String getDescription() {
		try {
			return mAddon.getDescription();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public String getEMail() {
		try {
			return mAddon.getEMail();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public String getWebsite() {
		try {
			return mAddon.getWebsite();
		} catch (RemoteException e) {				
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onPageStarted(String tabId, String url) {
		try {
			return mAddon.onPageStarted(tabId, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onPageFinished(String tabId, String url) {
		try {
			return mAddon.onPageFinished(tabId, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onTabOpened(String tabId) {
		try {
			return mAddon.onTabOpened(tabId);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onTabClosed(String tabId) {
		try {
			return mAddon.onTabClosed(tabId);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onTabSwitched(String tabId) {
		try {
			return mAddon.onTabSwitched(tabId);
		} catch (RemoteException e) {			
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedMainMenuItem(String currentTabId, String currentTitle, String currentUrl) {
		try {
			return mAddon.getContributedMainMenuItem(currentTabId, currentTitle, currentUrl);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onContributedMainMenuItemSelected(String currentTabId, String currentTitle, String currentUrl) {
		try {
			return mAddon.onContributedMainMenuItemSelected(currentTabId, currentTitle, currentUrl);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedLinkContextMenuItem(String currentTabId, int hitTestResult, String url) {
		try {
			return mAddon.getContributedLinkContextMenuItem(currentTabId, hitTestResult, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onContributedLinkContextMenuItemSelected(String currentTabId, int hitTestResult, String url) {
		try {
			return mAddon.onContributedLinkContextMenuItemSelected(currentTabId, hitTestResult, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedHistoryBookmarksMenuItem(String currentTabId) {
		try {
			return mAddon.getContributedHistoryBookmarksMenuItem(currentTabId);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onContributedHistoryBookmarksMenuItemSelected(String currentTabId) {
		try {
			return mAddon.onContributedHistoryBookmarksMenuItemSelected(currentTabId);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedBookmarkContextMenuItem(String currentTabId) {
		try {
			return mAddon.getContributedBookmarkContextMenuItem(currentTabId);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onContributedBookmarkContextMenuItemSelected(String currentTabId, String title, String url) {
		try {
			return mAddon.onContributedBookmarkContextMenuItemSelected(currentTabId, title, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getContributedHistoryContextMenuItem(String currentTabId) {
		try {
			return mAddon.getContributedHistoryContextMenuItem(currentTabId);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onContributedHistoryContextMenuItemSelected(String currentTabId, String title, String url) {
		try {
			return mAddon.onContributedHistoryContextMenuItemSelected(currentTabId, title, url);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Action> onUserAnswerQuestion(String currentTabId, String questionId, boolean positiveAnswer) {
		try {
			return mAddon.onUserAnswerQuestion(currentTabId, questionId, positiveAnswer);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void showAddonSettingsActivity() {
		try {
			mAddon.showAddonSettingsActivity();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}	
	
	public Intent getIntent() {
		return mIntent;
	}
	
	public boolean isBinded() {
		return mIsBinded;
	}

}
