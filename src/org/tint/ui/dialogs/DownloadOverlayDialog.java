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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.CheckBox;
import android.widget.TextView;

public class DownloadOverlayDialog {
	final Context mContext;
	final View mView;
	final RelativeLayout mInsertView;
	IUserActionListener mCallback;
	DownloadItem mDownloadItem;
	
	public DownloadOverlayDialog(RelativeLayout insertView) {
		mInsertView = insertView;
		mContext = insertView.getContext();
		mView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.download_overlaydialog, null);
		mView.setAlpha(0f);
		mView.setVisibility(View.VISIBLE);
	}
	
	public DownloadOverlayDialog setDownloadItem(DownloadItem item) {
		mDownloadItem = item;
		((TextView) mView.findViewById(R.id.DownloadOverlayDialog_FileName)).setText(item.getFileName());
		((TextView) mView.findViewById(R.id.DownloadOverlayDialog_FileSource)).setText(item.getUrl());
		((CheckBox) mView.findViewById(R.id.DownloadOverlayDialog_Incognito)).setChecked(item.isIncognito());
		return this;
	}
	
	public DownloadOverlayDialog setCallbackListener(IUserActionListener listener) {
		mCallback = listener;
		return this;
	}
	
	public void show() {
		Button acceptButton = (Button)mView.findViewById(R.id.DownloadOverlayDialog_Download),
				denyButton = (Button)mView.findViewById(R.id.DownloadOverlayDialog_Cancel);
		acceptButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mView.animate()
					.alpha(0f)
					.setDuration(100)
					.setListener(new AnimatorListener() {
						@Override
						public void onAnimationCancel(Animator animation) {
						}

						@Override
						public void onAnimationEnd(Animator animation) {
							mInsertView.removeView(mView);
						}

						@Override
						public void onAnimationRepeat(Animator animation) {
						}
						@Override
						public void onAnimationStart(Animator animation) {
						}
					})
					.start();
				sendAcceptCallback();
			}
		});
		denyButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mView.animate()
					.alpha(0f)
					.setDuration(100)
					.setListener(new AnimatorListener() {
						@Override
						public void onAnimationCancel(Animator animation) {
						}
	
						@Override
						public void onAnimationEnd(Animator animation) {
							mInsertView.removeView(mView);
						}
	
						@Override
						public void onAnimationRepeat(Animator animation) {
						}
						@Override
						public void onAnimationStart(Animator animation) {
						}
					})
					.start();
				sendDenyCallback();
			}
		});
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		mInsertView.addView(mView, params);
		mView.animate()
			.alpha(1f)
			.setDuration(100)
			.setListener(null)
			.start();
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
