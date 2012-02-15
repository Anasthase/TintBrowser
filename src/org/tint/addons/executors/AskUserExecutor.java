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
import org.tint.addons.framework.Action;
import org.tint.addons.framework.AskUserAction;
import org.tint.controllers.Controller;
import org.tint.ui.UIManager;
import org.tint.ui.components.CustomWebView;
import org.tint.utils.ApplicationUtils;

import android.content.Context;
import android.content.DialogInterface;

public class AskUserExecutor extends BaseActionExecutor {

	private AskUserAction mAddonAction;
	
	@Override
	public void init(Context context, UIManager uiManager, CustomWebView webView, Addon addon, Action addonAction) {
		baseInit(context, uiManager, webView, addon);
		
		mAddonAction = (AskUserAction) addonAction;
	}

	@Override
	public void execute() {
		ApplicationUtils.showAddonAskUserDialog(
				mContext,
				mAddonAction.getTitle(),
				mAddonAction.getMessage(),
				mAddonAction.getPositiveButtonCaption(),
				mAddonAction.getNegativeButtonCaption(),
				new DialogInterface.OnClickListener() {						
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Controller.getInstance().getAddonManager().onUserAnswerQuestion(mContext, mWebView, mAddon, mAddonAction.getId().toString(), true);
					}
				},
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Controller.getInstance().getAddonManager().onUserAnswerQuestion(mContext, mWebView, mAddon, mAddonAction.getId().toString(), false);
					}
				});
	}

}
