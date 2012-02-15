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

import org.tint.addons.framework.AddonAction;
import org.tint.addons.framework.AddonResponse;
import org.tint.ui.UIManager;
import org.tint.ui.components.CustomWebView;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.widget.Toast;

public class AddonManager {
	
	public static final String ACTION_ADDON = "org.tint.intent.action.PICK_ADDON";
	
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
			
			Addon addon = new Addon(mMainContext, addonId, category);			
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
			AddonResponse response = addon.onPageStarted(url);
			if (response != null) {
				responses.add(new AddonResponseWrapper(addon, response));
			}
		}
		
		processResponses(context, webView, responses);
	}
	
	public void onPageFinished(Context context, CustomWebView webView, String url) {
		List<AddonResponseWrapper> responses = new ArrayList<AddonResponseWrapper>();
		
		for (Addon addon : mAddons) {
			AddonResponse response = addon.onPageFinished(url);
			if (response != null) {
				responses.add(new AddonResponseWrapper(addon, response));
			}
		}
		
		processResponses(context, webView, responses);
	}
	
	public List<AddonMenuItem> getContributedMainMenuItems() {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedMainMenuItem();
			
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
			
			AddonResponse response = addon.onContributedMainMenuItemSelected(currentWebview.getTitle(), currentWebview.getUrl());
			processOneResponse(context, currentWebview, addon, response);

			return true;
		} else {
			return false;
		}
	}
	
	public List<AddonMenuItem> getContributedLinkContextMenuItems() {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedLinkContextMenuItem();
			
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
			
			AddonResponse response = addon.onContributedLinkContextMenuItemSelected(
					intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1),
					intent.getStringExtra(Constants.EXTRA_URL));
			
			processOneResponse(context, currentWebview, addon, response);
		}
	}
	
	public List<AddonMenuItem> getContributedHistoryBookmarksMenuItems() {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedHistoryBookmarksMenuItem();
			
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
			
			AddonResponse response = addon.onContributedHistoryBookmarksMenuItemSelected();
			processOneResponse(context, currentWebView, addon, response);

			return true;
		} else {
			return false;
		}
	}
	
	public List<AddonMenuItem> getContributedBookmarkContextMenuItems() {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedBookmarkContextMenuItem();
			
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
			
			AddonResponse response = addon.onContributedBookmarkContextMenuItemSelected(title, url);
			
			processOneResponse(context, currentWebView, addon, response);
			
			return true;
		} else {
			return false;
		}
	}
	
	public List<AddonMenuItem> getContributedHistoryContextMenuItems() {
		List<AddonMenuItem> result = new ArrayList<AddonMenuItem>();
		
		for (Addon addon : mAddons) {
			String response = addon.getContributedHistoryContextMenuItem();
			
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
			
			AddonResponse response = addon.onContributedHistoryContextMenuItemSelected(title, url);
			
			processOneResponse(context, currentWebView, addon, response);
			
			return true;
		} else {
			return false;
		}
	}
	
	private void processOneAction(final Context context, final CustomWebView webView, final Addon addon, final AddonAction action) {
		String data;
		
		switch (action.getAction()) {
		case AddonAction.ACTION_SHOW_TOAST:
			Toast.makeText(context, action.getData1(), Toast.LENGTH_SHORT).show();
			break;
			
		case AddonAction.ACTION_SHOW_DIALOG:
			ApplicationUtils.showMessageDialog(context, addon.getName(), action.getData1());
			break;
			
		case AddonAction.ACTION_ASK_USER:
			ApplicationUtils.showAddonAskUserDialog(
					context,
					addon.getName(),
					action.getData1(),
					action.getData2(),
					action.getData3(),
					new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							AddonResponse response = addon.onUserAnswerQuestion(action.getId().toString(), true);
							processOneResponse(context, webView, addon, response);
						}
					},
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							AddonResponse response = addon.onUserAnswerQuestion(action.getId().toString(), false);
							processOneResponse(context, webView, addon, response);
						}
					});		
			
			break;
			
		case AddonAction.ACTION_ADD_TAB:
			data = action.getData1();
			if (TextUtils.isEmpty(data)) {
				mUIManager.addTab(true);
			} else {
				mUIManager.addTab(action.getData1());
			}
			
			break;
			
		case AddonAction.ACTION_CLOSE_CURRENT_TAB:
			mUIManager.closeCurrentTab();
			break;
			
		case AddonAction.ACTION_LOAD_URL:
			data = action.getData1();
			if ((!TextUtils.isEmpty(data)) &&
					(webView != null)) {
				webView.loadUrl(data);
			}
			
			break;
			
		case AddonAction.ACTION_BROWSE_STOP:
			if (webView != null) {
				webView.stopLoading();
			}
			
			break;
			
		case AddonAction.ACTION_BROWSE_RELOAD:
			if (webView != null) {
				webView.reload();
			}
			
			break;
			
		case AddonAction.ACTION_BROWSE_FORWARD:
			if ((webView != null) &&
					(webView.canGoForward())) {
				webView.goForward();
			}
			
			break;
			
		case AddonAction.ACTION_BROWSE_BACK:
			if ((webView != null) &&
					(webView.canGoBack())) {
				webView.goBack();
			}
			
			break;
			
		default: break;
		}
	}
	
	private void processOneResponse(Context context, CustomWebView webView, AddonResponseWrapper responseWrapper) {
		Addon addon = responseWrapper.getAddon();
		AddonResponse response = responseWrapper.getResponse();
		
		processOneResponse(context, webView, addon, response);
	}
	
	private void processOneResponse(Context context, CustomWebView webView, Addon addon, AddonResponse response) {
		for (AddonAction action : response.getActions()) {
			processOneAction(context, webView, addon, action);
		}
	}
	
	private void processResponses(Context context, CustomWebView webView, List<AddonResponseWrapper> responses) {
		for (AddonResponseWrapper response : responses) {
			processOneResponse(context, webView, response);
		}
	}

}
