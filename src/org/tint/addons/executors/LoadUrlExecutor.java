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
import org.tint.addons.framework.LoadUrlAction;
import android.text.TextUtils;

public class LoadUrlExecutor extends BaseActionExecutor {

	private LoadUrlAction mAddonAction;
	
	@Override
	protected void finishInit(Action addonAction) {
		mAddonAction = (LoadUrlAction) addonAction;
	}

	@Override
	protected void internalExecute() {
		String tabId = mAddonAction.getTabId();				
		String url = mAddonAction.getUrl();
		
		if (TextUtils.isEmpty(tabId)) {
			if (mWebView != null) {				
				
				if (mAddonAction.getLoadRawUrl()) {
					mWebView.loadRawUrl(url);
				} else {
					if (TextUtils.isEmpty(url)) {
						mUIManager.loadHomePage();
					} else {
						mUIManager.loadUrl(url);
					}
				}
			}
		} else {
			
			UUID id;			
			try {
				id = UUID.fromString(tabId);
			} catch (NullPointerException e) {
				id = null;
			} catch (IllegalArgumentException e) {
				id = null;
			}
			
			if (id != null) {			
				if (mAddonAction.getLoadRawUrl()) {
					mUIManager.loadRawUrl(id, url, false);
				} else {
					if (TextUtils.isEmpty(url)) {
						mUIManager.loadHomePage(id, false);
					} else {
						mUIManager.loadUrl(id, url, false);
					}
				}
			}
		}
	}

}
