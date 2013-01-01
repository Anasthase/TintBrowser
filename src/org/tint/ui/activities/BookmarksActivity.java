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
import java.util.concurrent.atomic.AtomicReference;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.providers.BookmarksWrapper;
import org.tint.tasks.HistoryBookmarksExportTask;
import org.tint.tasks.HistoryBookmarksImportTask;
import org.tint.ui.fragments.BookmarksFragment;
import org.tint.ui.fragments.HistoryFragment;
import org.tint.ui.managers.UIManager;
import org.tint.ui.preferences.IHistoryBookmaksExportListener;
import org.tint.ui.preferences.IHistoryBookmaksImportListener;
import org.tint.ui.tabs.GenericTabListener;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;
import org.tint.utils.IOUtils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class BookmarksActivity extends Activity implements IHistoryBookmaksExportListener, IHistoryBookmaksImportListener {
	
	private static final String EXTRA_SELECTED_TAB_INDEX = "EXTRA_SELECTED_TAB_INDEX";
	
	private UIManager mUIManager;
	
	private ProgressDialog mProgress;
	
	private HistoryBookmarksImportTask mImportTask;
	private HistoryBookmarksExportTask mExportTask;
	
	private static final AtomicReference<AsyncTask<String, Integer, String>> mImportSyncThread =
		      new AtomicReference<AsyncTask<String, Integer, String>>();
	
	private static final AtomicReference<AsyncTask<Cursor, Integer, String>> mExportSyncThread =
		      new AtomicReference<AsyncTask<Cursor, Integer, String>>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.BookmarksTitle);
        
        mUIManager = Controller.getInstance().getUIManager();
        
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        Tab tab = actionBar.newTab();
        tab.setText(R.string.BookmarksTabTitle);
        tab.setTabListener(new GenericTabListener<BookmarksFragment>(this, "bookmarks", BookmarksFragment.class));
        actionBar.addTab(tab);
        
        tab = actionBar.newTab();
        tab.setText(R.string.HistoryTabTitle);
        tab.setTabListener(new GenericTabListener<HistoryFragment>(this, "history", HistoryFragment.class));
        actionBar.addTab(tab);
        
        if ((savedInstanceState != null) &&
        		(savedInstanceState.containsKey(EXTRA_SELECTED_TAB_INDEX))) {
        	int selectedIndex = savedInstanceState.getInt(EXTRA_SELECTED_TAB_INDEX);
        	
        	if ((selectedIndex == 0) ||
        			(selectedIndex == 1)) {
        		actionBar.setSelectedNavigationItem(selectedIndex);
        	}
        }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(EXTRA_SELECTED_TAB_INDEX, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.bookmarks_activity_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (getActionBar().getSelectedNavigationIndex() == 0) {
			menu.findItem(R.id.BookmarksActivityMenuSortBookmarks).setVisible(true);
		} else {
			menu.findItem(R.id.BookmarksActivityMenuSortBookmarks).setVisible(false);
		}
		
		menu.removeGroup(R.id.BookmarksActivity_AddonsMenuGroup);
		
		List<AddonMenuItem> contributedMenuItems = Controller.getInstance().getAddonManager().getContributedHistoryBookmarksMenuItems(mUIManager.getCurrentWebView());
		for (AddonMenuItem item : contributedMenuItems) {
			menu.add(R.id.BookmarksActivity_AddonsMenuGroup, item.getAddon().getMenuId(), 0, item.getMenuItem());
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_CANCELED);
            finish();
			return true;
			
		case R.id.BookmarksActivityMenuAddBookmark:
			Intent i = new Intent(this, EditBookmarkActivity.class);
			i.putExtra(Constants.EXTRA_ID, -1);
			startActivity(i);
			
			return true;
			
		case R.id.BookmarksActivityMenuSortBookmarks:
			changeSortMode();
			return true;
			
		case R.id.BookmarksActivityMenuImportHistoryBookmarks:
			importHistoryBookmarks();
			return true;
			
		case R.id.BookmarksActivityMenuExportHistoryBookmarks:
			exportHistoryBookmarks();
			return true;
			
		case R.id.BookmarksActivityMenuClearHistoryBookmarks:
			clearHistoryBookmarks();
			return true;
			
		default:
			if (Controller.getInstance().getAddonManager().onContributedHistoryBookmarksMenuItemSelected(
					this,
					item.getItemId(),
					mUIManager.getCurrentWebView())) {
				return true;
			} else {
				return super.onContextItemSelected(item);
			}
		}
	}
	
	@Override
	public void onExportProgress(int step, int progress, int total) {
		switch(step) {
		case 0:
			mProgress.setMessage(getString(R.string.HistoryBookmarksExportCheckCardMessage));
			break;
		case 1:
			mProgress.setMessage(String.format(getString(R.string.HistoryBookmarksExportProgressMessage), progress, total));
			break;
		default: break;
		}
	}

	@Override
	public void onExportDone(String message) {
		mExportSyncThread.compareAndSet(mExportTask, null);
		mProgress.dismiss();
		
		if (message != null) {
			ApplicationUtils.showErrorDialog(this,
					getString(R.string.HistoryBookmarksExportErrorTitle),
					String.format(getString(R.string.HistoryBookmarksExportErrorMessage), message));
		}
	}

	@Override
	public void onImportProgress(int step, int progress, int total) {
		switch(step) {
		case 0:
			mProgress.setMessage(getString(R.string.HistoryBookmarksImportReadingFile));
			break;
		case 1:
			mProgress.setMessage(getString(R.string.HistoryBookmarksImportParsingFile));
			break;
		case 2:			
			mProgress.setMessage(String.format(getString(R.string.HistoryBookmarksImportProgressMessage), progress, total));
			break;
		case 3:
			mProgress.setMessage(String.format(getString(R.string.HistoryBookmarksImportFoldersProgressMessage), progress, total));
			break;
		case 4:
			mProgress.setMessage(getString(R.string.HistoryBookmarksImportFoldersLinkMessage));
			break;
		case 5:
			mProgress.setMessage(String.format(getString(R.string.HistoryBookmarksImportBookmarksProgressMessage), progress, total));
			break;
		case 6:
			mProgress.setMessage(String.format(getString(R.string.HistoryBookmarksImportHistoryProgressMessage), progress, total));
			break;
		case 7:
			mProgress.setMessage(getString(R.string.HistoryBookmarksImportInsertMessage));
			break;
		default: break;
		}
	}

	@Override
	public void onImportDone(String message) {
		mImportSyncThread.compareAndSet(mImportTask, null);
		mProgress.dismiss();
		
		if (message != null) {
			ApplicationUtils.showErrorDialog(this,
					getString(R.string.HistoryBookmarksImportErrorTitle),
					String.format(getString(R.string.HistoryBookmarksImportErrorMessage), message));
		}
	}
	
	private void importHistoryBookmarks() {
		List<String> exportedFiles = IOUtils.getExportedBookmarksFileList();		    	
    	final String[] choices = exportedFiles.toArray(new String[exportedFiles.size()]);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(getResources().getString(R.string.HistoryBookmarksImportSourceTitle));
    	builder.setSingleChoiceItems(choices, 0, new OnClickListener() {
    		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
				
				mImportTask = new HistoryBookmarksImportTask(BookmarksActivity.this, BookmarksActivity.this);
				
				mProgress = ProgressDialog.show(BookmarksActivity.this,
						getString(R.string.HistoryBookmarksImportTitle),
						getString(R.string.HistoryBookmarksImportInitialMessage),
						true,
						false);
				
				mProgress.show();
				
				boolean retVal = mImportSyncThread.compareAndSet(null, mImportTask);
				if (retVal) {
					mImportTask.execute(choices[which]);
				}						
				
			}    		
    	});    	
    	
    	builder.setCancelable(true);
    	builder.setNegativeButton(R.string.Cancel, null);
    	
    	AlertDialog alert = builder.create();
    	alert.show();
	}
	
	private void exportHistoryBookmarks() {
		mExportTask = new HistoryBookmarksExportTask(this, this);
		
		mProgress = ProgressDialog.show(this,
				getString(R.string.HistoryBookmarksExportTitle),
				getString(R.string.HistoryBookmarksExportInitialMessage),
				true,
				false);
		
		mProgress.show();
		
		boolean retVal = mExportSyncThread.compareAndSet(null, mExportTask);
		if (retVal) {
			mExportTask.execute(BookmarksWrapper.getAllHistoryBookmarks(getContentResolver()));
		}
	}
	
	private void clearHistoryBookmarks() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(getResources().getString(R.string.HistoryBookmarksClearTitle));
    	builder.setSingleChoiceItems(getResources().getStringArray(R.array.ClearHistoryBookmarksChoice), 0, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
				switch (which) {
				case 0:
					BookmarksWrapper.clearHistoryAndOrBookmarks(getContentResolver(), true, false);
					break;
				
				case 1:
					BookmarksWrapper.clearHistoryAndOrBookmarks(getContentResolver(), false, true);
					break;
					
				case 2:
					BookmarksWrapper.clearHistoryAndOrBookmarks(getContentResolver(), true, true);
					break;

				default:
					break;
				}
			}
    		
    	});
    	
    	builder.setCancelable(true);
    	builder.setNegativeButton(R.string.Cancel, null);
    	
    	AlertDialog alert = builder.create();
    	alert.show();
	}
	
	private void changeSortMode() {
		int currentSort = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.PREFERENCE_BOOKMARKS_SORT_MODE, 0);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setInverseBackgroundForced(true);
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setTitle(getResources().getString(R.string.SortBookmarks));
    	
    	builder.setSingleChoiceItems(
    			new String[] {
    					getResources().getString(R.string.MostUsedSortMode),
    					getResources().getString(R.string.AlphaSortMode),
    					getResources().getString(R.string.RecentSortMode)
    			},
    			currentSort,
    			new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editor editor = PreferenceManager.getDefaultSharedPreferences(BookmarksActivity.this).edit();
		    	editor.putInt(Constants.PREFERENCE_BOOKMARKS_SORT_MODE, which);
		    	editor.commit();
				
				dialog.dismiss();				
			}    		
    	});
    	
    	builder.setCancelable(true);
    	builder.setNegativeButton(android.R.string.cancel, null);
    	
    	AlertDialog alert = builder.create();
    	alert.show();		
	}
	
}
