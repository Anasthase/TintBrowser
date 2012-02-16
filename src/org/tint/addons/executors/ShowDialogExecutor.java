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
import org.tint.addons.framework.ShowDialogAction;
import org.tint.utils.ApplicationUtils;

public class ShowDialogExecutor extends BaseActionExecutor {

	private ShowDialogAction mAddonAction;
	
	@Override
	protected void finishInit(Action addonAction) {
		mAddonAction = (ShowDialogAction) addonAction;
	}

	@Override
	protected void internalExecute() {
		ApplicationUtils.showMessageDialog(mContext, mAddonAction.getTitle(), mAddonAction.getMessage());
	}

}
