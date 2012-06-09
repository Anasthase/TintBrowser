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

import org.tint.R;
import org.tint.addons.framework.Action;
import org.tint.addons.framework.AskUserChoiceAction;
import org.tint.controllers.Controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

public class AskUserChoiceExecutor extends BaseActionExecutor {

	private AskUserChoiceAction mAddonAction;
	
	@Override
	protected void finishInit(Action addonAction) {
		mAddonAction = (AskUserChoiceAction) addonAction;
	}

	@Override
	protected void internalExecute() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(mAddonAction.getTitle());
		
		builder.setSingleChoiceItems(mAddonAction.getChoices().toArray(new String[mAddonAction.getChoices().size()]), 0, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
				Controller.getInstance().getAddonManager().onUserChoice(
						mContext,
						mWebView,
						mAddon,
						mAddonAction.getId(),
						false,
						which);
			}			
		});
		
		builder.setCancelable(true);
		builder.setNegativeButton(R.string.Cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Controller.getInstance().getAddonManager().onUserChoice(
						mContext,
						mWebView,
						mAddon,
						mAddonAction.getId(),
						true,
						-1);
			}			
			
		});
		
		builder.setOnCancelListener(new OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				Controller.getInstance().getAddonManager().onUserChoice(
						mContext,
						mWebView,
						mAddon,
						mAddonAction.getId(),
						true,
						-1);
			}
		});
		
		AlertDialog alert = builder.create();
    	alert.show();
	}

}
