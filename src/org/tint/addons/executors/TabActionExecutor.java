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

package org.tint.addons.executors;

import java.util.UUID;

import org.tint.addons.framework.Action;
import org.tint.addons.framework.TabAction;
import org.tint.ui.components.CustomWebView;

import android.text.TextUtils;

public class TabActionExecutor extends BaseActionExecutor {

	private TabAction mAddonAction;
	
	@Override
	protected void finishInit(Action addonAction) {
		mAddonAction = (TabAction) addonAction;
	}

	@Override
	protected void internalExecute() {
		String tabId = mAddonAction.getTabId();
		
		UUID id;
		CustomWebView webView;
		
		// If we do not have a tab id, use the current WebView.
		// Else, check if provided id is valid, and get the associated WebView.
		// If the provided id is not valid, or does not correspond to a currently opened tab,
		// webView will be null, and no action will be performed.
		if (!TextUtils.isEmpty(tabId)) {
			try {
				id = UUID.fromString(tabId);
				webView = mUIManager.getWebViewByTabId(id);
			} catch (NullPointerException e) {
				id = null;
				webView = null;
			} catch (IllegalArgumentException e) {
				id = null;
				webView = null;
			}
		} else {
			id = null;
			webView = mWebView;
		}
		
		switch (mAddonAction.getAction()) {
		case Action.ACTION_CLOSE_TAB:
			if (TextUtils.isEmpty(tabId)) {
				mUIManager.closeCurrentTab();
			} else {
				mUIManager.closeTab(id);
			}
			break;
			
		case Action.ACTION_BROWSE_STOP:
			if (webView != null) {
				webView.stopLoading();
			}
			break;
			
		case Action.ACTION_BROWSE_RELOAD:
			if (webView != null) {
				webView.reload();
			}
			break;
			
		case Action.ACTION_BROWSE_FORWARD:			
			if ((webView != null) &&
					(webView.canGoForward())) {
				webView.goForward();
			}
			break;
			
		case Action.ACTION_BROWSE_BACK:
			if ((webView != null) &&
					(webView.canGoBack())) {
				webView.goBack();
			}
			break;
			
		default: break;
		}
	}	

}
