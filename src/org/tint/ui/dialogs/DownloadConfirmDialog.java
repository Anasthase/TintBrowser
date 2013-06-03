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

package org.tint.ui.dialogs;

import org.tint.R;
import org.tint.model.DownloadItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class DownloadConfirmDialog {
	final Context mContext;
	final AlertDialog.Builder mBuilder;
	final View mView;
	IUserActionListener mCallback;
	DownloadItem mDownloadItem;
	
	public DownloadConfirmDialog(Context context) {
		mContext = context;
		mView = LayoutInflater.from(mContext).inflate(R.layout.download_confirm_dialog, null);
		mBuilder = new AlertDialog.Builder(mContext)
			.setView(mView)
			.setTitle(R.string.DownloadDialogTitle);
	}
	
	public DownloadConfirmDialog setDownloadItem(DownloadItem item) {
		mDownloadItem = item;
		((TextView) mView.findViewById(R.id.DownloadOverlayDialog_FileName)).setText(item.getFileName());
		((TextView) mView.findViewById(R.id.DownloadOverlayDialog_FileSource)).setText(item.getUrl());
		((CheckBox) mView.findViewById(R.id.DownloadOverlayDialog_Incognito)).setChecked(item.isIncognito());
		return this;
	}
	
	public DownloadConfirmDialog setCallbackListener(IUserActionListener listener) {
		mCallback = listener;
		return this;
	}
	
	public void show() {
		mBuilder.setPositiveButton(
				mContext.getResources().getText(R.string.Download),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendAcceptCallback();
					}
					
				});
		mBuilder.setNegativeButton(
				mContext.getResources().getText(R.string.Cancel),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendDenyCallback();
					}
					
				});
		mBuilder.show();
	}
	
	private void sendAcceptCallback() {
		mDownloadItem.setIncognito(((CheckBox) mView.findViewById(R.id.DownloadOverlayDialog_Incognito)).isChecked());
		mCallback.onAcceptDownload(mDownloadItem);
	}
	
	private void sendDenyCallback() {
		mCallback.onDenyDownload();
	}
	
	public static interface IUserActionListener {
		public void onAcceptDownload(DownloadItem item);
		public void onDenyDownload();
	}
}
