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

package org.tint.ui.activities;

import java.util.List;

import org.tint.R;
import org.tint.model.FolderItem;
import org.tint.providers.BookmarksWrapper;
import org.tint.utils.Constants;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditBookmarkActivity extends Activity {

	private long mId = -1;
	
	private EditText mLabel;
	private EditText mUrl;
	
	private Spinner mFoldersSpinner;
	
	private EditText mNewFolderName;
	
	private Button mOk;
	private Button mCancel;
	
	private List<FolderItem> mFolders;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_bookmark_activity);
		setTitle(R.string.AddBookmarkTitle);
		
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
        
		mFolders = BookmarksWrapper.getFirstLevelFoldersList(getContentResolver());
		mFolders.add(0, new FolderItem(-1, getString(R.string.Bookmarks)));
		mFolders.add(0, new FolderItem(-2, getString(R.string.NewFolder)));
		
        mLabel = (EditText) findViewById(R.id.EditBookmarkActivity_LabelEdit);
        mUrl = (EditText) findViewById(R.id.EditBookmarkActivity_UrlEdit);
        
        mFoldersSpinner = (Spinner) findViewById(R.id.EditBookmarkActivity_FolderSpinner);
        
        FoldersAdapter adapter = new FoldersAdapter(this, mFolders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFoldersSpinner.setAdapter(adapter);
        
        mFoldersSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				if (position == 0) {
					mNewFolderName.setVisibility(View.VISIBLE);
					mNewFolderName.requestFocus();
				} else {
					mNewFolderName.setVisibility(View.GONE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) { }
		});
        
        // Default to root folder.
        mFoldersSpinner.setSelection(1);
        
        mNewFolderName = (EditText) findViewById(R.id.EditBookmarkActivity_FolderValue);
        
        mOk = (Button) findViewById(R.id.EditBookmarkActivity_OK);
        mOk.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				if (save()) {
					setResult(RESULT_OK);
					finish();
				}
			}
		});
        
        mCancel = (Button) findViewById(R.id.EditBookmarkActivity_Cancel);
        mCancel.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
	            finish();
			}
		});
        
        Bundle extras = getIntent().getExtras();
    	if (extras != null) {
    		String label = extras.getString(Constants.EXTRA_LABEL);
    		if (!TextUtils.isEmpty(label)) {
    			mLabel.setText(label);
    		}
    		
    		String url = extras.getString(Constants.EXTRA_URL);
    		if (!TextUtils.isEmpty(url)) {
    			mUrl.setText(url);
    		}
    		
    		// This is a bit dirty...
    		long folderId = extras.getLong(Constants.EXTRA_FOLDER_ID);
    		if (folderId != -1) {
    			for (int i = 0; i < mFolders.size(); i++) {
    				if (mFolders.get(i).getId() == folderId) {
    					mFoldersSpinner.setSelection(i);
    					break;
    				}
    			}
    		}
    		
    		mId = extras.getLong(Constants.EXTRA_ID);
    	}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_CANCELED);
            finish();
			return true;
		default: return super.onContextItemSelected(item);
		 }
	}
	
	private boolean save() {
		String label = mLabel.getText().toString();
		String url = mUrl.getText().toString();
		
		if ((!TextUtils.isEmpty(label)) &&
				(!TextUtils.isEmpty(url))) {
			
			long folderId = -1;
			int folderSpinnerSelection = mFoldersSpinner.getSelectedItemPosition();
			
			switch (folderSpinnerSelection) {
			case 0:
				if (TextUtils.isEmpty(mNewFolderName.getText().toString())) {
					Toast.makeText(this, R.string.ProvideNewFolderName, Toast.LENGTH_SHORT).show();
					return false;
				} else {
					folderId = BookmarksWrapper.getFolderId(getContentResolver(), mNewFolderName.getText().toString(), true);
				}
				break;
				
			case 1:
				folderId = -1;
				break;
			default:
				folderId = mFolders.get(folderSpinnerSelection).getId();
				break;
			}			
			
			BookmarksWrapper.setAsBookmark(getContentResolver(), mId, folderId, label, url, true);
			return true;
		} else {
			Toast.makeText(this, R.string.AddBookmarkLabelOrUrlEmpty, Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	private class FoldersAdapter extends ArrayAdapter<FolderItem> {
		
		public FoldersAdapter(Context context, List<FolderItem> values) {
			super(context, android.R.layout.simple_spinner_item, android.R.id.text1, values);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			
			TextView tv = (TextView) v.findViewById(android.R.id.text1);
			tv.setText(getItem(position).getTitle());
			
			return v;
		}

		@Override
		public View getDropDownView(int position, View convertView,	ViewGroup parent) {
			View v = super.getDropDownView(position, convertView, parent);
			
			TextView tv = (TextView) v.findViewById(android.R.id.text1);
			tv.setText(getItem(position).getTitle());
			
			return v;
		}

				
	}

}
