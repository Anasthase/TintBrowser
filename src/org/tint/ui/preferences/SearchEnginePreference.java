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
import org.tint.model.SearchUrlCategory;
import org.tint.model.SearchUrlItem;
import org.tint.tasks.SearchUrlTask;
import org.tint.tasks.SearchUrlTask.ISearchUrlTaskListener;
import org.tint.utils.UrlUtils;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

public class SearchEnginePreference extends DialogPreference implements ISearchUrlTaskListener {
	
	private TextView mText;
	private EditText mEditText;
	private ImageView mDivider;
	private ExpandableListView mList;
	private ProgressBar mProgress;
	private TextView mProgressText;
	
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
		
		mText = (TextView) view.findViewById(R.id.SearchUrlGetOnlineText);
		mEditText = (EditText) view.findViewById(R.id.SearchUrlEditText);
		mDivider = (ImageView) view.findViewById(R.id.divider);
		mList = (ExpandableListView) view.findViewById(R.id.SearchUrlList);
		mProgress = (ProgressBar) view.findViewById(R.id.SearchUrlProgressBar);
		mProgressText = (TextView) view.findViewById(R.id.SearchUrlProgressText);
		
		mDivider.setVisibility(View.GONE);
		mList.setVisibility(View.GONE);
		mProgress.setVisibility(View.GONE);
		mProgressText.setVisibility(View.GONE);
		
		mText.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				startGetSearchUrlOnline();
			}
		});
		
		mList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				
				if (mAdapter != null) {
					mEditText.setText(((SearchUrlItem) mAdapter.getChild(groupPosition, childPosition)).getUrl());
					
					removeEditTextFocus();
				}
				
				return true;
			}
		});
		
		mEditText.setText(UrlUtils.getRawSearchUrl(getContext()));
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
	    	editor.putString(getKey(), mEditText.getText().toString());
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
			mText.setVisibility(View.VISIBLE);
			mProgress.setVisibility(View.GONE);
			mProgressText.setVisibility(View.GONE);
			mList.setVisibility(View.GONE);
			mDivider.setVisibility(View.GONE);
			
			Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();			
		} else {
			mProgress.setVisibility(View.INVISIBLE);
			mProgressText.setVisibility(View.INVISIBLE);
			mList.setVisibility(View.VISIBLE);
			mDivider.setVisibility(View.VISIBLE);
			
			List<SearchUrlCategory> results = mTask.getResults();
			
			mAdapter = new SearchUrlAdapter(getContext(), results);
			
			mList.setAdapter(mAdapter);
		}
		
		mSearchUrlSyncThread.compareAndSet(mTask, null);
		mProgressText.setText(R.string.SearchUrlConnecting);
	}
	
	private void startGetSearchUrlOnline() {
		removeEditTextFocus();
		
		mText.setVisibility(View.GONE);
		
		mDivider.setVisibility(View.INVISIBLE);
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
		mEditText.clearFocus();
		
		InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}

}
