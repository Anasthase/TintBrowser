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

package org.tint.controllers;

import java.util.ArrayList;
import java.util.List;

import org.tint.addons.AddonManager;
import org.tint.model.DownloadItem;
import org.tint.ui.UIManager;
import org.tint.ui.activities.TintBrowserActivity;

public class Controller { 
	
	/**
	 * Holder for singleton implementation.
	 */
	private static final class ControllerHolder {
		private static final Controller INSTANCE = new Controller();
		/**
		 * Private Constructor.
		 */
		private ControllerHolder() { }
	}
	
	/**
	 * Get the unique instance of the Controller.
	 * @return The instance of the Controller
	 */
	public static Controller getInstance() {
		return ControllerHolder.INSTANCE;
	}
	
	/**
	 * Private Constructor.
	 */
	private Controller() {
		mDownloads = new ArrayList<DownloadItem>();
	}
	
	private UIManager mUIManager;
	private TintBrowserActivity mMainActivity;
	
	private List<DownloadItem> mDownloads;
	
	private AddonManager mAddonManager;
	
	public void init(UIManager uiManager, TintBrowserActivity activity) {
		mUIManager = uiManager;
		mMainActivity = activity;
		mAddonManager = new AddonManager(mMainActivity, mUIManager);
	}
	
	public UIManager getUIManager() {
		return mUIManager;
	}
	
	public TintBrowserActivity getMainActivity() {
		return mMainActivity;
	}

	public List<DownloadItem> getDownloadsList() {
		return mDownloads;
	}
	
	public DownloadItem getDownloadItemById(long id) {
		for (DownloadItem item : mDownloads) {
			if (item.getId() == id) {
				return item;
			}
		}
		
		return null;
	}
	
	public AddonManager getAddonManager() {
		return mAddonManager;
	}
	
}
