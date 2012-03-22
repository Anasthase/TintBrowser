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

package org.tint.ui.fragments;

import java.util.List;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.model.BookmarkHistoryItem;
import org.tint.model.BookmarksAdapter;
import org.tint.providers.BookmarksProvider;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.UIManager;
import org.tint.ui.activities.EditBookmarkActivity;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class BookmarksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final int CONTEXT_MENU_OPEN_IN_TAB = Menu.FIRST;
	private static final int CONTEXT_MENU_EDIT_BOOKMARK = Menu.FIRST + 1;
	private static final int CONTEXT_MENU_COPY_URL = Menu.FIRST + 2;
	private static final int CONTEXT_MENU_SHARE_URL = Menu.FIRST + 3;
	private static final int CONTEXT_MENU_DELETE_BOOKMARK = Menu.FIRST + 4;
	
	private static final int CONTEXT_MENU_DELETE_FOLDER = Menu.FIRST + 5;
	
	private View mContainer = null;
	
	private UIManager mUIManager;
	
	private GridView mBookmarksGrid;
	
	private FragmentBreadCrumbs mFoldersBreadCrumb;
	
	private BookmarksAdapter mAdapter;
	
	private long mFolderId = -1;
	
	private boolean mIsTablet;
	
	public BookmarksFragment() {
		mUIManager = Controller.getInstance().getUIManager();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);		
		
		String[] from = new String[] { BookmarksProvider.Columns.TITLE, BookmarksProvider.Columns.URL };
		int[] to = new int[] { R.id.BookmarkRow_Title, R.id.BookmarkRow_Url };
		
		mAdapter = new BookmarksAdapter(
				getActivity(),
				R.layout.bookmark_row,
				null,
				from,
				to,
				ApplicationUtils.getBookmarksThumbnailsDimensions(getActivity()),
				R.drawable.browser_thumbnail);
		
		mBookmarksGrid.setAdapter(mAdapter);
		
		mBookmarksGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				BookmarkHistoryItem item = BookmarksWrapper.getBookmarkById(getActivity().getContentResolver(), id);
				
				if (item != null) {
					if (item.isFolder()) {
						setFolderId(item.getId(), item.getTitle());						
					} else {
						Intent result = new Intent();
						result.putExtra(Constants.EXTRA_URL, item.getUrl());

						getActivity().setResult(Activity.RESULT_OK, result);
						getActivity().finish();
					}
				}
			}
		});
		
		registerForContextMenu(mBookmarksGrid);
		
		getLoaderManager().initLoader(0, null, this);
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mIsTablet = ApplicationUtils.isTablet(getActivity());
		
		if (mContainer == null) {
			mContainer = inflater.inflate(R.layout.bookmarks_fragment, container, false);
			
			mFoldersBreadCrumb = (FragmentBreadCrumbs) mContainer.findViewById(R.id.BookmarksBreadCrumb);
			mFoldersBreadCrumb.setMaxVisible(2);
			mFoldersBreadCrumb.setActivity(getActivity());

			mFoldersBreadCrumb.setParentTitle(getString(R.string.Bookmarks), null, new OnClickListener() {				
				@Override
				public void onClick(View v) {
					setFolderId(-1, null);
				}
			});
			
			mBookmarksGrid = (GridView) mContainer.findViewById(R.id.BookmarksGridView);
			
			if (!mIsTablet) {
				mFoldersBreadCrumb.setVisibility(View.GONE);
			}
		}
		
		return mContainer;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		long id = ((AdapterContextMenuInfo) menuInfo).id;
		if (id != -1) {
			BookmarkHistoryItem selectedItem = BookmarksWrapper.getBookmarkById(getActivity().getContentResolver(), id);
			if (selectedItem != null) {
				
				menu.setHeaderTitle(selectedItem.getTitle());
				
				if (!selectedItem.isFolder()) {
					BitmapDrawable icon = ApplicationUtils.getApplicationButtonImage(getActivity(), selectedItem.getFavicon());
					if (icon != null) {
						menu.setHeaderIcon(icon);
					}					

					menu.add(0, CONTEXT_MENU_OPEN_IN_TAB, 0, R.string.OpenInTab);
					menu.add(0, CONTEXT_MENU_EDIT_BOOKMARK, 0, R.string.EditBookmark);
					menu.add(0, CONTEXT_MENU_COPY_URL, 0, R.string.CopyUrl);
					menu.add(0, CONTEXT_MENU_SHARE_URL, 0, R.string.ContextMenuShareUrl);
					menu.add(0, CONTEXT_MENU_DELETE_BOOKMARK, 0, R.string.DeleteBookmark);

					
				} else {
				
					menu.add(0, CONTEXT_MENU_DELETE_FOLDER, 0, R.string.DeleteFolder);
					
				}
				
				List<AddonMenuItem> addonsContributions = Controller.getInstance().getAddonManager().getContributedBookmarkContextMenuItems(mUIManager.getCurrentWebView());
				for (AddonMenuItem item : addonsContributions) {
					menu.add(0, item.getAddon().getMenuId(), 0, item.getMenuItem());
				}
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		BookmarkHistoryItem selectedItem = BookmarksWrapper.getBookmarkById(getActivity().getContentResolver(), info.id);
		
		Intent i;
		switch (item.getItemId()) {
		case CONTEXT_MENU_OPEN_IN_TAB:			
			if (item != null) {
				Intent result = new Intent();
				result.putExtra(Constants.EXTRA_NEW_TAB, true);
				result.putExtra(Constants.EXTRA_URL, selectedItem.getUrl());
				
				getActivity().setResult(Activity.RESULT_OK, result);
				getActivity().finish();
			}
			return true;
			
		case CONTEXT_MENU_EDIT_BOOKMARK:
			if (selectedItem != null) {
				i = new Intent(getActivity(), EditBookmarkActivity.class);
				i.putExtra(Constants.EXTRA_ID, info.id);
				i.putExtra(Constants.EXTRA_FOLDER_ID, selectedItem.getFolderId());
				i.putExtra(Constants.EXTRA_LABEL, selectedItem.getTitle());
				i.putExtra(Constants.EXTRA_URL, selectedItem.getUrl());
				
				startActivity(i);
			}
			
			return true;

		case CONTEXT_MENU_COPY_URL:
			if (selectedItem != null) {
				ApplicationUtils.copyTextToClipboard(getActivity(), selectedItem.getUrl(), getActivity().getResources().getString(R.string.UrlCopyToastMessage));
			}
			
			return true;
			
		case CONTEXT_MENU_SHARE_URL:
			if (selectedItem != null) {
				ApplicationUtils.sharePage(getActivity(), null, selectedItem.getUrl());						
			}
			
			return true;
			
		case CONTEXT_MENU_DELETE_BOOKMARK:
			BookmarksWrapper.deleteBookmark(getActivity().getContentResolver(), info.id);
			return true;
			
		case CONTEXT_MENU_DELETE_FOLDER:
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setCancelable(true);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle(R.string.DeleteFolder);
			builder.setMessage(R.string.ConfirmDeleteFolderMessage);
			
			builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					BookmarksWrapper.deleteFolder(getActivity().getContentResolver(), info.id);					
				}				
			});
			
			builder.setNegativeButton(R.string.No, null);
			
			builder.create().show();
						
			return true;
		
		default:
			if (Controller.getInstance().getAddonManager().onContributedBookmarkContextMenuItemSelected(
					getActivity(),
					item.getItemId(),
					selectedItem.getTitle(),
					selectedItem.getUrl(),
					mUIManager.getCurrentWebView())) {
				return true;
			} else {
				return super.onContextItemSelected(item);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return BookmarksWrapper.getCursorLoaderForBookmarks(getActivity(), mFolderId);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	private void setFolderId(long folderId, String folderTitle) {
		mFolderId = folderId;
		getLoaderManager().restartLoader(0, null, this);
		
		if (mFolderId == -1) {
			if (!mIsTablet) {
				mFoldersBreadCrumb.setVisibility(View.GONE);
			}
			
			mFoldersBreadCrumb.setTitle(null, null);
		} else {
			mFoldersBreadCrumb.setTitle(folderTitle, folderTitle);
			
			if (!mIsTablet) {
				mFoldersBreadCrumb.setVisibility(View.VISIBLE);
			}
		}		
	}

}
