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
import org.tint.addons.framework.AskUserConfirmationAction;
import org.tint.controllers.Controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

public class AskUserConfirmationExecutor extends BaseActionExecutor {

	private AskUserConfirmationAction mAddonAction;
	
	@Override
	protected void finishInit(Action addonAction) {
		mAddonAction = (AskUserConfirmationAction) addonAction;
	}

	@Override
	protected void internalExecute() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		
    	builder.setCancelable(true);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(mAddonAction.getTitle());
    	builder.setMessage(mAddonAction.getMessage());

    	builder.setInverseBackgroundForced(true);
    	builder.setPositiveButton(mAddonAction.getPositiveButtonCaption(), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Controller.getInstance().getAddonManager().onUserConfirm(mContext, mWebView, mAddon, mAddonAction.getId(), true);
			}
		});
    	
    	builder.setNegativeButton(mAddonAction.getNegativeButtonCaption(), new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Controller.getInstance().getAddonManager().onUserConfirm(mContext, mWebView, mAddon, mAddonAction.getId(), false);
			}
		});
    	
    	builder.setOnCancelListener(new OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				Controller.getInstance().getAddonManager().onUserConfirm(mContext, mWebView, mAddon, mAddonAction.getId(), false);
			}
		});
    	
    	AlertDialog alert = builder.create();
    	alert.show();
	}

}
