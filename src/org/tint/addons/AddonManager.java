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

import org.tint.addons.executors.BaseActionExecutor;
import org.tint.addons.executors.ExecutorFactory;
import org.tint.addons.framework.Action;
import org.tint.ui.UIManager;
import org.tint.ui.components.CustomWebView;
import org.tint.utils.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

public class AddonManager {
	
	public static final String ACTION_ADDON = "org.tint.intent.action.ADDON";
	
	private Context mMainContext;
	private UIManager mUIManager;
	private PackageManager mPackageManager;
	private List<Addon> mAddons;
	
	public AddonManager(Context context, UIManager uiManager) {
		mMainContext = context;
		mUIManager = uiManager;
		mPackageManager = mMainContext.getPackageManager();
		mAddons = new ArrayList<Addon>();
	}
	
	public List<Addon> getAddons() {
		return mAddons;
	}
	
	public void bindAddons() {
		mAddons.clear();
		
		Intent baseIntent = new Intent(ACTION_ADDON);
		List<ResolveInfo> addonInfoList = mPackageManager.queryIntentServices(baseIntent, PackageManager.GET_RESOLVED_FILTER);
		
		int addonId = 0;
		
		for (ResolveInfo addonInfo : addonInfoList) {
			String category = addonInfo.filter.categoriesIterator().next();
			
			Addon addon = new Addon(mMainContext, addonId, addonInfo, category);			
			mAddons.add(addon);
			
			addonId++;
		}		
	}
	
	public void unbindAddons() {
		for (Addon addon : mAddons) {
			addon.unbindService();
		}
		
		mAddons.clear();
	}
	
	public void onPageStarted(Context context, CustomWebView webView, String url) {
		List<AddonResponseWrapper> responses = new ArrayList<AddonResponseWrapper>();
		
		for (Addon addon : mAddons) {
			List<Action> response = addon.onPageStarted(webView.getParentFragment().getUUID().toString(), url);
			if (response != null) {
				responses.add(new AddonResponseWrapper(addon, response));
			}
		}
		
		processResponses(context, webView, responses);
	}
	
	public void onPageFinished(Context context, CustomWebView webView, String url) {
		List<AddonResponseWrapper> responses = new ArrayList<AddonResponseWrapper>();
		
		for (Addon addon : mAddons) {
			List<Action> response = addon.onPageFinished(webView.getParentFragment().getUUID().toString(), url);
			if (response != null) {
				responses.add(new AddonResponseWrapper(addon, response));
			}
		}
		
		processResponses(context, webView, responses);
	}
	
	public void onTabOpened(Context context, CustomWebView webView) {
		List<AddonResponseWrapper> responses = new ArrayList<AddonResponseWrapper>();
		
		for (Addon addon : mAddons) {
			List<Action> response = addon.onTabOpened(webView.getParentFragmentUUID().toString());
			if (response != null) {
				responses.add(new AddonResponseWrapper(addon, response));
			}
		}
		
		processResponses(context, webView, responses);
	}
	
	public void onTabClosed(Context context, CustomWebView webView) {
		List<AddonResponseWrapper> responses = new ArrayList<AddonResponseWrapper>();
		
		for (Addon addon : mAddons) {
			List<Action> response = addon.onTabClosed(webView.getParentFragmentUUID().toString());
			if (response != null) {
				responses.add(new AddonResponseWrapper(addon, response));
			}
		}
		
		processResponses(context, webView, responses);
	}
	
	public void onTabSwitched(Context context, CustomWebView webView) {
		List<AddonResponseWrapper> responses = new ArrayList<AddonResponseWrapper>();
		
		for (Addon addon : mAddons) {
			List<Action> response = addon.onTabSwitched(webView.getParentFragmentUUID().toString());
			if (response != null) {
				responses.add(new AddonResponseWrapper(addon, response));
			}
		}
		
		processResponses(context, webView, responses);
	}
	
	public List<AddonMenuItem> getContributedMainMenuItems(CustomWebView currentWebview) {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedMainMenuItem(currentWebview.getParentFragmentUUID().toString());
			
			if (!TextUtils.isEmpty(response)) {
				result.add(new AddonMenuItem(addon, response));
			}			
		}
		
		return result;
	}
	
	public boolean onContributedMainMenuItemSelected(Context context, int addonId, CustomWebView currentWebview) {
		addonId = Math.abs(addonId) - 1000;
		
		if ((addonId >= 0) &&
				(addonId < mAddons.size())) {
			
			Addon addon = mAddons.get(addonId);
			
			List<Action> response = addon.onContributedMainMenuItemSelected(
					currentWebview.getParentFragmentUUID().toString(),
					currentWebview.getTitle(),
					currentWebview.getUrl());
			
			processOneResponse(context, currentWebview, addon, response);

			return true;
		} else {
			return false;
		}
	}
	
	public List<AddonMenuItem> getContributedLinkContextMenuItems(CustomWebView currentWebview, int hitTestResult, String url) {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedLinkContextMenuItem(
					currentWebview.getParentFragmentUUID().toString(),
					hitTestResult,
					url);
			
			if (!TextUtils.isEmpty(response)) {
				result.add(new AddonMenuItem(addon, response));
			}			
		}
		
		return result;
	}
	
	public void onContributedContextLinkMenuItemSelected(Context context, int addonId, Intent intent, CustomWebView currentWebview) {
		addonId = Math.abs(addonId) - 1000;
		
		if ((addonId >= 0) &&
				(addonId < mAddons.size())) {
			
			Addon addon = mAddons.get(addonId);
			
			List<Action> response = addon.onContributedLinkContextMenuItemSelected(
					currentWebview.getParentFragmentUUID().toString(),
					intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1),
					intent.getStringExtra(Constants.EXTRA_URL));
			
			processOneResponse(context, currentWebview, addon, response);
		}
	}
	
	public List<AddonMenuItem> getContributedHistoryBookmarksMenuItems(CustomWebView currentWebview) {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedHistoryBookmarksMenuItem(currentWebview.getParentFragmentUUID().toString());
			
			if (!TextUtils.isEmpty(response)) {
				result.add(new AddonMenuItem(addon, response));
			}			
		}
		
		return result;
	}
	
	public boolean onContributedHistoryBookmarksMenuItemSelected(Context context, int addonId, CustomWebView currentWebView) {
		addonId = Math.abs(addonId) - 1000;
		
		if ((addonId >= 0) &&
				(addonId < mAddons.size())) {
			
			Addon addon = mAddons.get(addonId);
			
			List<Action> response = addon.onContributedHistoryBookmarksMenuItemSelected(currentWebView.getParentFragmentUUID().toString());
			processOneResponse(context, currentWebView, addon, response);

			return true;
		} else {
			return false;
		}
	}
	
	public List<AddonMenuItem> getContributedBookmarkContextMenuItems(CustomWebView currentWebview) {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedBookmarkContextMenuItem(currentWebview.getParentFragmentUUID().toString());
			
			if (!TextUtils.isEmpty(response)) {
				result.add(new AddonMenuItem(addon, response));
			}			
		}
		
		return result;
	}
	
	public boolean onContributedBookmarkContextMenuItemSelected(Context context, int addonId, String title, String url, CustomWebView currentWebView) {
		addonId = Math.abs(addonId) - 1000;
		
		if ((addonId >= 0) &&
				(addonId < mAddons.size())) {
			
			Addon addon = mAddons.get(addonId);
			
			List<Action> response = addon.onContributedBookmarkContextMenuItemSelected(
					currentWebView.getParentFragmentUUID().toString(),
					title,
					url);
			
			processOneResponse(context, currentWebView, addon, response);
			
			return true;
		} else {
			return false;
		}
	}
	
	public List<AddonMenuItem> getContributedHistoryContextMenuItems(CustomWebView currentWebview) {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedHistoryContextMenuItem(currentWebview.getParentFragmentUUID().toString());
			
			if (!TextUtils.isEmpty(response)) {
				result.add(new AddonMenuItem(addon, response));
			}			
		}
		
		return result;
	}
	
	public boolean onContributedHistoryContextMenuItemSelected(Context context, int addonId, String title, String url, CustomWebView currentWebView) {
		addonId = Math.abs(addonId) - 1000;
		
		if ((addonId >= 0) &&
				(addonId < mAddons.size())) {
			
			Addon addon = mAddons.get(addonId);
			
			List<Action> response = addon.onContributedHistoryContextMenuItemSelected(
					currentWebView.getParentFragmentUUID().toString(),
					title,
					url);
			
			processOneResponse(context, currentWebView, addon, response);
			
			return true;
		} else {
			return false;
		}
	}
	
	public void onUserAnswerQuestion(Context context, CustomWebView currentWebView, Addon addon, String actionId, boolean positiveAnswer) {
		List<Action> response = addon.onUserAnswerQuestion(
				currentWebView.getParentFragmentUUID().toString(),
				actionId,
				positiveAnswer);
		
		processOneResponse(context, currentWebView, addon, response);
	}
	
	private void processOneAction(final Context context, final CustomWebView webView, final Addon addon, final Action addonAction) {
		BaseActionExecutor executor = ExecutorFactory.getExecutor(addonAction);
		
		if (executor != null) {
			executor.execute(context, mUIManager, webView, addon, addonAction);
		}
	}
	
	private void processOneResponse(Context context, CustomWebView webView, Addon addon, List<Action> response) {
		for (Action action : response) {
			processOneAction(context, webView, addon, action);
		}
	}
	
	private void processOneResponse(Context context, CustomWebView webView, AddonResponseWrapper responseWrapper) {
		Addon addon = responseWrapper.getAddon();
		List<Action> response = responseWrapper.getResponse();
		
		processOneResponse(context, webView, addon, response);
	}
	
	private void processResponses(Context context, CustomWebView webView, List<AddonResponseWrapper> responses) {
		for (AddonResponseWrapper response : responses) {
			processOneResponse(context, webView, response);
		}
	}

}
