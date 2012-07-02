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
import org.tint.utils.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

public class AccessibilityPreviewPreference extends Preference implements OnSharedPreferenceChangeListener {
	
	private static final String HTML_FORMAT = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><style type=\"text/css\">p { margin: 2px auto;}</style><body><p style=\"font-size: 4pt\">%s</p><p style=\"font-size: 8pt\">%s</p><p style=\"font-size: 10pt\">%s</p><p style=\"font-size: 14pt\">%s</p><p style=\"font-size: 18pt\">%s</p></body></html>";

	private WebView mWebView;

	private String mHtml;
	
	public AccessibilityPreviewPreference(Context context) {
		super(context);
		init();
	}
	
	public AccessibilityPreviewPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public AccessibilityPreviewPreference(Context context, AttributeSet attrs,	int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		setLayoutResource(R.layout.accessibility_preview);
		
		Object[] visualNames = getContext().getResources().getStringArray(R.array.FontPreviewText);
		
        mHtml = String.format(HTML_FORMAT, visualNames);
	}
	
	@Override
    protected View onCreateView(ViewGroup parent) {
        View root = super.onCreateView(parent);
        
        TextView title = (TextView) root.findViewById(R.id.AccessibilityPreviewTitle);
        title.setText(getTitle());
        
        TextView summary = (TextView) root.findViewById(R.id.AccessibilityPreviewSummary);
        if (!TextUtils.isEmpty(getSummary())) {
        	summary.setText(getSummary());
        } else {
        	summary.setVisibility(View.GONE);
        }
        
        WebView wv = (WebView) root.findViewById(R.id.AccessibilityPreviewWebView);
        wv.setFocusable(false);
        wv.setFocusableInTouchMode(false);
        wv.setClickable(false);
        wv.setLongClickable(false);
        wv.setHorizontalScrollBarEnabled(false);
        wv.setVerticalScrollBarEnabled(false);
        wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        
        return root;
	}
	
	@Override
    protected void onBindView(View view) {
		super.onBindView(view);
		mWebView = (WebView) view.findViewById(R.id.AccessibilityPreviewWebView);
		updatePreview();
	}	

	@Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPrepareForRemoval() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPrepareForRemoval();
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (Constants.PREFERENCE_MINIMUM_FONT_SIZE.equals(key) ||
				Constants.PREFERENCE_TEXT_SCALING.equals(key)) {
			updatePreview();
		}
	}
	
	private void updatePreview() {
        if (mWebView == null) return;

        WebSettings ws = mWebView.getSettings();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        
        int fontSize = prefs.getInt(Constants.PREFERENCE_MINIMUM_FONT_SIZE, 1);
        int textScaling = prefs.getInt(Constants.PREFERENCE_TEXT_SCALING, 100);
        		
        ws.setMinimumFontSize(fontSize);
        ws.setTextZoom(textScaling);
        
        mWebView.loadData(mHtml, "text/html; charset=utf-8", "utf-8");
    }

}
