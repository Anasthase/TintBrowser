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

package org.tint.ui.components;

import org.tint.R;
import org.tint.tasks.UpdateFaviconTask;
import org.tint.tasks.UpdateHistoryTask;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.managers.UIManager;
import org.tint.utils.Constants;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

public class CustomWebChromeClient extends WebChromeClient {

	private UIManager mUIManager;
	
	private Bitmap mDefaultVideoPoster = null;
	private View mVideoProgressView = null;
	private SharedPreferences mPreferences = null;
	
	public CustomWebChromeClient(UIManager uiManager) {
		mUIManager = uiManager;
		mPreferences = PreferenceManager.getDefaultSharedPreferences(mUIManager.
				getMainActivity().getApplicationContext());
	}
	
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		mUIManager.onProgressChanged(view, newProgress);
	}
	
	@Override
	public void onReceivedTitle(WebView view, String title) {
		mUIManager.onReceivedTitle(view, title);

		if (!view.isPrivateBrowsingEnabled()) {
			UpdateHistoryTask task = new UpdateHistoryTask(mUIManager.getMainActivity());
			task.execute(view.getTitle(), view.getUrl(), view.getOriginalUrl());
		}
	}
	
	@Override
	public void onReceivedIcon(WebView view, Bitmap icon) {
		mUIManager.onReceivedIcon(view, icon);

		UpdateFaviconTask task = new UpdateFaviconTask(mUIManager.getMainActivity().getContentResolver(), view.getUrl(), view.getOriginalUrl(), icon);
		task.execute();
	}
	
	@Override
	public boolean onCreateWindow(WebView view, final boolean dialog, final boolean userGesture, final Message resultMsg) {
		WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;

		CustomWebView curentWebView = mUIManager.getCurrentWebView();

		mUIManager.addTab(false, curentWebView.isPrivateBrowsingEnabled());

		transport.setWebView(mUIManager.getCurrentWebView());
		resultMsg.sendToTarget();

		return true;
	}
	
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		mUIManager.setUploadMessage(uploadMsg);
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType((acceptType == null || acceptType.isEmpty()) ? "*/*" : acceptType);
		mUIManager.getMainActivity().startActivityForResult(
				Intent.createChooser(i,  mUIManager.getMainActivity().getString(R.string.FileChooserPrompt)),
				TintBrowserActivity.ACTIVITY_OPEN_FILE_CHOOSER);
	}
	
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		mUIManager.setUploadMessage(uploadMsg);
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");
		mUIManager.getMainActivity().startActivityForResult(
				Intent.createChooser(i,  mUIManager.getMainActivity().getString(R.string.FileChooserPrompt)),
				TintBrowserActivity.ACTIVITY_OPEN_FILE_CHOOSER);
	}
	
	@Override
	public Bitmap getDefaultVideoPoster() {
		if (mDefaultVideoPoster == null) {
			mDefaultVideoPoster = BitmapFactory.decodeResource(mUIManager.getMainActivity().getResources(), R.drawable.default_video_poster);
		}

		return mDefaultVideoPoster;
	}
	
	@Override
	public View getVideoLoadingProgressView() {
		if (mVideoProgressView == null) {
			LayoutInflater inflater = LayoutInflater.from(mUIManager.getMainActivity());
			mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
		}

		return mVideoProgressView;
	}
	
	@Override
	public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
		new AlertDialog.Builder(mUIManager.getMainActivity())
		.setTitle(R.string.JavaScriptAlertDialog)
		.setMessage(message)
		.setPositiveButton(android.R.string.ok,
				new AlertDialog.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) {
				result.confirm();
			}
		})
		.setCancelable(false)
		.create()
		.show();

		return true;
	}
	
	@Override
	public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
		new AlertDialog.Builder(mUIManager.getMainActivity())
		.setTitle(R.string.JavaScriptConfirmDialog)
		.setMessage(message)
		.setPositiveButton(android.R.string.ok, 
				new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) {
				result.confirm();
			}
		})
		.setNegativeButton(android.R.string.cancel, 
				new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) {
				result.cancel();
			}
		})
		.create()
		.show();

		return true;
	}
	
	@Override
	public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {

		final LayoutInflater factory = LayoutInflater.from(mUIManager.getMainActivity());
		final View v = factory.inflate(R.layout.javascript_prompt_dialog, null);
		((TextView) v.findViewById(R.id.JavaScriptPromptMessage)).setText(message);
		((EditText) v.findViewById(R.id.JavaScriptPromptInput)).setText(defaultValue);

		new AlertDialog.Builder(mUIManager.getMainActivity())
		.setTitle(R.string.JavaScriptPromptDialog)
		.setView(v)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = ((EditText) v.findViewById(R.id.JavaScriptPromptInput)).getText()
						.toString();
				result.confirm(value);
			}
		})
		.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				result.cancel();
			}
		})
		.setOnCancelListener(
				new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						result.cancel();
					}
				})
				.show();

		return true;

	}
	
	@Override
	public void onHideCustomView() {
		super.onHideCustomView();
		mUIManager.onHideCustomView();
	}
	
	@Override
	public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
		super.onShowCustomView(view, requestedOrientation, callback);
		mUIManager.onShowCustomView(view, requestedOrientation, callback);
	}
	
	@Override
	public void onShowCustomView(View view, CustomViewCallback callback) {		
		super.onShowCustomView(view, callback);
		mUIManager.onShowCustomView(view, -1, callback);
	}
	
	@Override
	public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
		mUIManager.onGeolocationPermissionsShowPrompt(origin, callback);
	}
	
	@Override
	public void onGeolocationPermissionsHidePrompt() {
		mUIManager.onGeolocationPermissionsHidePrompt();
	}
	
	@Override
	public void onConsoleMessage(String message, int lineNumber, String sourceID) {
		if(mPreferences.getBoolean(Constants.PREFERENCE_JS_LOG_ON_LOGCAT, false)) {
			Log.d("TintJS", sourceID + ":" + lineNumber + " " + message);
		}
	}
	
	@Override
	public boolean onConsoleMessage(ConsoleMessage cm) {
		if(mPreferences.getBoolean(Constants.PREFERENCE_JS_LOG_ON_LOGCAT, false)) {
			Log.d("TintJS", cm.sourceId() + ":" + cm.lineNumber() + " " + cm.message());
		}
		return true;
	}

}
