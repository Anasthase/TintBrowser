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

import org.tint.addons.framework.Action;
import org.tint.addons.framework.OpenTabAction;

import android.text.TextUtils;

public class OpenTabExecutor extends BaseActionExecutor {

	private OpenTabAction mAddonAction;
	
	@Override
	protected void finishInit(Action addonAction) {
		mAddonAction = (OpenTabAction) addonAction;
	}

	@Override
	protected void internalExecute() {
		String url = mAddonAction.getUrl();
		
		if (TextUtils.isEmpty(url)) {
			mUIManager.addTab(true, false);
		} else {
			mUIManager.addTab(url, false, false);
		}
	}

}
