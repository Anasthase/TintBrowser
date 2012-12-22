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

package org.tint.ui.preferences;

import org.tint.R;
import org.tint.utils.ApplicationUtils;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

public class WebViewDialogPreference extends DialogPreference {
	
	private WebView mWebView;
	
	private String mHtml;

	public WebViewDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}
	
	public WebViewDialogPreference(Context context, AttributeSet attrs,	int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	@Override
	protected View onCreateDialogView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.webview_dialog_preference, null);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		
		mWebView = (WebView) view.findViewById(R.id.PreferenceWebView);
		mWebView.loadData(mHtml, "text/html; charset=UTF-8", null);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);		
		builder.setNegativeButton(null, null);		
	}

	private void init(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WebViewDialogPreference);
			
			int id = a.getResourceId(R.styleable.WebViewDialogPreference_html, -1);
			if (id != -1) {
				mHtml = ApplicationUtils.getStringFromRawResource(getContext(), id);
			} else {
				mHtml = "Unable to get resource.";
			}
			
			a.recycle();
		}
	}

}
