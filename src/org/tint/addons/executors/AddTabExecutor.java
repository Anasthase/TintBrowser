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

import org.tint.addons.Addon;
import org.tint.addons.framework.AddTabAction;
import org.tint.addons.framework.Action;
import org.tint.ui.UIManager;
import org.tint.ui.components.CustomWebView;

import android.content.Context;
import android.text.TextUtils;

public class AddTabExecutor extends BaseActionExecutor {

	private AddTabAction mAddonAction;
	
	@Override
	public void init(Context context, UIManager uiManager, CustomWebView webView, Addon addon, Action addonAction) {
		baseInit(context, uiManager, webView, addon);
		
		mAddonAction = (AddTabAction) addonAction;
	}

	@Override
	public void execute() {
		String url = mAddonAction.getUrl();
		
		if (TextUtils.isEmpty(url)) {
			mUIManager.addTab(true);
		} else {
			mUIManager.addTab(url);
		}
	}

}
