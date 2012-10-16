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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.tint.R;
import org.tint.model.SearchUrlAdapter;
import org.tint.model.SearchUrlGroup;
import org.tint.model.SearchUrlItem;
import org.tint.tasks.SearchUrlTask;
import org.tint.tasks.SearchUrlTask.ISearchUrlTaskListener;
import org.tint.utils.Constants;
import org.tint.utils.UrlUtils;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchEnginePreference extends DialogPreference implements ISearchUrlTaskListener {
	
	private TextView mText2;
	private TextView mCurrentEngine;
	private TextView mCustomEngineText;
	private EditText mCustomEngineEditText;
	private ImageView mDivider1;
	private ImageView mDivider2;
	private ExpandableListView mList;
	private ProgressBar mProgress;
	private TextView mProgressText;
	
	private TextWatcher mTextWatcher;
	
	private SearchUrlTask mTask;
	
	private SearchUrlAdapter mAdapter;
	
	private static final AtomicReference<AsyncTask<Void, Integer, String>> mSearchUrlSyncThread =
		      new AtomicReference<AsyncTask<Void, Integer, String>>();
	
	public SearchEnginePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected View onCreateDialogView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.search_engine_preference, null);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		
		mText2 = (TextView) view.findViewById(R.id.SearchUrlText2);
		mCurrentEngine = (TextView) view.findViewById(R.id.CurrentSearchEngine);
		mCustomEngineText = (TextView) view.findViewById(R.id.SearchUrlManualEdit);
		mCustomEngineEditText = (EditText) view.findViewById(R.id.SearchUrlEditText);
		mDivider1 = (ImageView) view.findViewById(R.id.divider1);
		mDivider2 = (ImageView) view.findViewById(R.id.divider2);
		mList = (ExpandableListView) view.findViewById(R.id.SearchUrlList);
		mProgress = (ProgressBar) view.findViewById(R.id.SearchUrlProgressBar);
		mProgressText = (TextView) view.findViewById(R.id.SearchUrlProgressText);
		
		mCustomEngineText.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mText2.setVisibility(View.GONE);
				mCustomEngineText.setVisibility(View.GONE);
				mDivider1.setVisibility(View.GONE);
				mDivider2.setVisibility(View.GONE);
				mList.setVisibility(View.GONE);
				mProgress.setVisibility(View.GONE);
				mProgressText.setVisibility(View.GONE);
				
				mCustomEngineEditText.setVisibility(View.VISIBLE);
				mCustomEngineEditText.requestFocus();
				showKeyboard();
			}
		});
		
		mList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				
				if (mAdapter != null) {
					mCustomEngineEditText.removeTextChangedListener(mTextWatcher);
					
					SearchUrlItem item = (SearchUrlItem) mAdapter.getChild(groupPosition, childPosition);
					
					mCurrentEngine.setText(item.getName());
					mCustomEngineEditText.setText(item.getUrl());
					
					removeEditTextFocus();
					
					mCustomEngineEditText.addTextChangedListener(mTextWatcher);
				}
				
				return true;
			}
		});
		
		mTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mCurrentEngine.setText(getContext().getString(R.string.SearchUrlCustom));
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) { }
			
			@Override
			public void afterTextChanged(Editable s) { }
		};
		
		mCurrentEngine.setText(getCurrentSearchEngineName());
		mCustomEngineEditText.setText(UrlUtils.getRawSearchUrl(getContext()));
		
		mCustomEngineEditText.setVisibility(View.GONE);
		mCustomEngineEditText.addTextChangedListener(mTextWatcher);
		
		startGetSearchUrlOnline();
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
	    	editor.putString(getKey(), mCustomEngineEditText.getText().toString());
	    	editor.putString(getKey() + "_NAME",  mCurrentEngine.getText().toString());
	    	editor.commit();
		}
	}

	@Override
	public void onProgress(int step) {
		switch (step) {
		case 0:
			mProgressText.setText(R.string.SearchUrlConnecting);
			break;
		case 1:
			mProgressText.setText(R.string.SearchUrlParsing);
			break;
		default:
			mProgressText.setText(R.string.SearchUrlConnecting);
			break;
		}
	}

	@Override
	public void onDone(String result) {		
		if (result != null) {
			mProgress.setVisibility(View.GONE);
			mProgressText.setText(result);			
		} else {
			mProgress.setVisibility(View.INVISIBLE);
			mProgressText.setVisibility(View.INVISIBLE);
			mList.setVisibility(View.VISIBLE);
			
			List<SearchUrlGroup> results = mTask.getResults();
			
			mAdapter = new SearchUrlAdapter(getContext(), results);
			
			mList.setAdapter(mAdapter);
		}
		
		mSearchUrlSyncThread.compareAndSet(mTask, null);
	}
	
	private void startGetSearchUrlOnline() {
		removeEditTextFocus();
		
		mList.setVisibility(View.INVISIBLE);
		mProgress.setVisibility(View.VISIBLE);
		mProgressText.setVisibility(View.VISIBLE);		
		
		mTask = new SearchUrlTask(getContext(), this);
		boolean retVal = mSearchUrlSyncThread.compareAndSet(null, mTask);
		if (retVal) {
			mTask.execute();
		}
	}
	
	private void removeEditTextFocus() {
		mCustomEngineEditText.clearFocus();
		
		InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(mCustomEngineEditText.getWindowToken(), 0);
	}
	
	private void showKeyboard() {
		InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(mCustomEngineEditText, InputMethodManager.SHOW_IMPLICIT);
	}
	
	private String getCurrentSearchEngineName() {
		String name = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constants.PREFERENCE_SEARCH_URL + "_NAME", "");
		
		if (TextUtils.isEmpty(name)) {
			String searchUrl = UrlUtils.getRawSearchUrl(getContext());
			if (getContext().getString(R.string.SearchUrlGoogle).equals(searchUrl)) {
				name = getContext().getString(R.string.SearchUrlDefault);
			} else {
				name = getContext().getString(R.string.SearchUrlCustom);
			}
		}
		
		return name;
	}
}
