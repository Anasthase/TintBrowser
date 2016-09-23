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

import org.tint.R;
import org.tint.model.BookmarkHistoryItem;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.fragments.FoldersOnlyFragment;
import org.tint.utils.Constants;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditBookmarkActivity extends Activity implements FoldersOnlyFragment.FoldersOnlyListener {

	private long mId = -1;
	private long mFolderId = -1;
	
	private EditText mLabel;
	private EditText mUrl;
	
	private Button mOk;
	private Button mCancel;
	private Button mPickFolder;
	
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
		
        mLabel = (EditText) findViewById(R.id.EditBookmarkActivity_LabelEdit);
        mUrl = (EditText) findViewById(R.id.EditBookmarkActivity_UrlEdit);
        
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

		mPickFolder = (Button) findViewById(R.id.PickFolder);

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
			if (extras.containsKey(Constants.EXTRA_FOLDER_ID)) {
				mFolderId = extras.getLong(Constants.EXTRA_FOLDER_ID);
			}
    		if (mFolderId != -1) {
				BookmarkHistoryItem bhi = BookmarksWrapper.getBookmarkById(getContentResolver(), mFolderId);
				mPickFolder.setText(bhi.getTitle());
    		}
			else {
				mPickFolder.setText(R.string.Bookmarks);
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
			
			BookmarksWrapper.setAsBookmark(getContentResolver(), mId, mFolderId, label, url, true);
			return true;
		} else {
			Toast.makeText(this, R.string.AddBookmarkLabelOrUrlEmpty, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	public void pickFolder(View view) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		//Remove fragment else it will crash as it is already added to backstack
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		final FoldersOnlyFragment newFragment = new FoldersOnlyFragment();
		Bundle args = new Bundle();
		args.putString(FoldersOnlyFragment.EXTRA_FOLDER_STACK,
				BookmarksWrapper.getFolderStack(getContentResolver(), mFolderId));
		newFragment.setArguments(args);

		newFragment.show(getFragmentManager(), "dialog");
	}

	@Override
	public void onSelectFolder(long folder_id, String folder_name) {
		//Toast.makeText(this, "Folder id "+folder_id+" selected", Toast.LENGTH_SHORT).show();
		mFolderId = folder_id;
		if (folder_id == -1) {
			mPickFolder.setText(R.string.Bookmarks);
		}
		else {
			mPickFolder.setText(folder_name);
		}
	}

}
