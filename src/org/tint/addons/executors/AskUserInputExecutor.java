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
import org.tint.addons.framework.AskUserInputAction;
import org.tint.controllers.Controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AskUserInputExecutor extends BaseActionExecutor {

	private AskUserInputAction mAddonAction;

	private LayoutInflater mInflater = null;
	
	private View mView = null;
	private TextView mMessage;
	private EditText mInput;
	
	@Override
	protected void finishInit(Action addonAction) {
		mAddonAction = (AskUserInputAction) addonAction;
		
		if (mInflater == null) {
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
		}
	}

	@Override
	protected void internalExecute() {
		
		mView = mInflater.inflate(R.layout.ask_user_input_dialog, null);
		mMessage = (TextView) mView.findViewById(R.id.AskUserInputMessage);
		mInput = (EditText) mView.findViewById(R.id.AskUserInput);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setCancelable(true);
		
		builder.setTitle(mAddonAction.getTitle());
		
		mMessage.setText(mAddonAction.getMessage());
				
		mInput.setInputType(mAddonAction.getInputType());
		mInput.setHint(mAddonAction.getInputHint());
		mInput.setText(mAddonAction.getDefaultInput());
		
		builder.setView(mView);
		
		builder.setPositiveButton(R.string.OK, new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Controller.getInstance().getAddonManager().onUserInput(
						mContext,
						mWebView,
						mAddon,
						mAddonAction.getId().toString(),
						false,
						mInput.getText().toString());
			}
		});
		
		builder.setNegativeButton(R.string.Cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Controller.getInstance().getAddonManager().onUserInput(
						mContext,
						mWebView,
						mAddon,
						mAddonAction.getId().toString(),
						true,
						null);
			}
		});
		
		builder.setOnCancelListener(new OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				Controller.getInstance().getAddonManager().onUserInput(
						mContext,
						mWebView,
						mAddon,
						mAddonAction.getId().toString(),
						true,
						null);
			}
		});
		
		AlertDialog alert = builder.create();
    	alert.show();		
	}

}
