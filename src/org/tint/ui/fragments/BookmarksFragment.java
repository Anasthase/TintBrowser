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

import java.util.ArrayList;
import java.util.List;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.model.BookmarkHistoryItem;
import org.tint.model.BookmarksAdapter;
import org.tint.providers.BookmarksProvider;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.activities.EditBookmarkActivity;
import org.tint.ui.managers.UIFactory;
import org.tint.ui.managers.UIManager;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;

public class BookmarksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String EXTRA_FOLDER_STACK = "EXTRA_FOLDER_STACK";

	private static final String STACK_SEPARATOR = "//;//";

	private static final int CONTEXT_MENU_OPEN_IN_TAB = Menu.FIRST;
	private static final int CONTEXT_MENU_EDIT_BOOKMARK = Menu.FIRST + 1;
	private static final int CONTEXT_MENU_COPY_URL = Menu.FIRST + 2;
	private static final int CONTEXT_MENU_SHARE_URL = Menu.FIRST + 3;
	private static final int CONTEXT_MENU_DELETE_BOOKMARK = Menu.FIRST + 4;

	private static final int CONTEXT_MENU_DELETE_FOLDER = Menu.FIRST + 5;

	private View mContainer = null;

	private UIManager mUIManager;

	private GridView mBookmarksGrid;

	private ProgressBar mProgress;

	private ViewGroup mBreadCrumbGroup;
	private FragmentBreadCrumbs mFoldersBreadCrumb;
	private ImageView mBackBreadCrumb;

	private BookmarksAdapter mAdapter;

	private List<NavigationItem> mNavigationList;

	private boolean mIsTablet;
	private boolean mIsListShown = true;

	private ProgressDialog mProgressDialog;
	
	private OnSharedPreferenceChangeListener mPreferenceChangeListener;

	public BookmarksFragment() {
		mUIManager = Controller.getInstance().getUIManager();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (Constants.PREFERENCE_BOOKMARKS_SORT_MODE.equals(key)) {
					getLoaderManager().restartLoader(0, null, BookmarksFragment.this);
				}
			}				
		};
		
		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
	}
	
	@Override
	public void onDestroy() {		
		super.onDestroy();
		PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mIsTablet = UIFactory.isTablet(getActivity());

		if (mContainer == null) {
			mContainer = inflater.inflate(R.layout.bookmarks_fragment, container, false);
			
			mBreadCrumbGroup = (ViewGroup) mContainer.findViewById(R.id.BookmarksBreadCrumbGroup);			

			mFoldersBreadCrumb = (FragmentBreadCrumbs) mContainer.findViewById(R.id.BookmarksBreadCrumb);
			mFoldersBreadCrumb.setMaxVisible(2);
			mFoldersBreadCrumb.setActivity(getActivity());

			mFoldersBreadCrumb.setParentTitle(getString(R.string.Bookmarks), null, new OnClickListener() {				
				@Override
				public void onClick(View v) {
					popNavigation();					
				}
			});

			mBackBreadCrumb = (ImageView) mContainer.findViewById(R.id.BookmarksBreadCrumbBackHierarchy);
			mBackBreadCrumb.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					popNavigation();
				}
			});

			mBookmarksGrid = (GridView) mContainer.findViewById(R.id.BookmarksGridView);
			mProgress = (ProgressBar) mContainer.findViewById(R.id.BookmarksProgressBar);

			String[] from = new String[] { BookmarksProvider.Columns.TITLE, BookmarksProvider.Columns.URL };
			int[] to = new int[] { R.id.BookmarkRow_Title, R.id.BookmarkRow_Url };

			mAdapter = new BookmarksAdapter(
					getActivity(),
					R.layout.bookmark_row,
					null,
					from,
					to,
					0,
					R.drawable.browser_thumbnail);

			mBookmarksGrid.setAdapter(mAdapter);

			mBookmarksGrid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					BookmarkHistoryItem item = BookmarksWrapper.getBookmarkById(getActivity().getContentResolver(), id);

					if (item != null) {
						if (item.isFolder()) {
							mNavigationList.add(new NavigationItem(item.getId(), item.getTitle()));
							updateFolderId();						
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

			if (!mIsTablet) {
				mBreadCrumbGroup.setVisibility(View.GONE);
				mBreadCrumbGroup.setTranslationY(- mBreadCrumbGroup.getHeight());
			}

			mNavigationList = new ArrayList<NavigationItem>();		

			if ((savedInstanceState != null) && 
					(savedInstanceState.containsKey(EXTRA_FOLDER_STACK))) {
				String folderStack = savedInstanceState.getString(EXTRA_FOLDER_STACK);

				String[] stack = folderStack.split(STACK_SEPARATOR);
				for (int i = 0; i < stack.length; i++) {
					mNavigationList.add(new NavigationItem(stack[i]));
				}
			} else {
				mNavigationList.add(new NavigationItem(-1, null));
			}

			setListShown(false);

			updateFolderId();
		}

		return mContainer;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {		
		super.onSaveInstanceState(outState);

		StringBuilder sb = new StringBuilder();
		for (NavigationItem item : mNavigationList) {
			sb.append(item.toString() + STACK_SEPARATOR);
		}

		outState.putString(EXTRA_FOLDER_STACK, sb.toString());
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
					doDeleteFolder(info.id);					
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
		setListShown(false);
		return BookmarksWrapper.getCursorLoaderForBookmarks(getActivity(), mNavigationList.get(mNavigationList.size() - 1).getId());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		setListShown(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private void setListShown(boolean shown) {
		if (mIsListShown == shown) {
			return;
		}

		mIsListShown = shown;

		if (shown) {
			mProgress.setVisibility(View.GONE);
			mBookmarksGrid.setVisibility(View.VISIBLE);
		} else {
			mProgress.setVisibility(View.VISIBLE);
			mBookmarksGrid.setVisibility(View.GONE);
		}
	}

	private void updateFolderId() {
		if (mAdapter != null) {
			mAdapter.swapCursor(null);
		}

		NavigationItem current = mNavigationList.get(mNavigationList.size() - 1);
		if (current.getId() == -1) {
			if (!mIsTablet) {
				// Dirty workaround for the first time the BreadCrumb is shown.
				// At this time, its size has not been computed, so its height is 0 
				// and does not show with an animation.
				int height = mBreadCrumbGroup.getHeight();
				if (height == 0) {
					height = 80;
				}

				AnimatorSet animator = new AnimatorSet();
				animator.play(ObjectAnimator.ofFloat(mBreadCrumbGroup, "translationY", - height));

				animator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mBreadCrumbGroup.setVisibility(View.GONE);
						getLoaderManager().restartLoader(0, null, BookmarksFragment.this);
					}					
				});

				animator.start();

			} else {
				mBackBreadCrumb.setVisibility(View.GONE);
				getLoaderManager().restartLoader(0, null, this);
			}

			mFoldersBreadCrumb.setParentTitle(getString(R.string.Bookmarks), null, new OnClickListener() {					
				@Override
				public void onClick(View arg0) {
					popNavigation();
				}
			});

		} else {			
			if (!mIsTablet) {
				mBreadCrumbGroup.setVisibility(View.VISIBLE);

				AnimatorSet animator = new AnimatorSet();
				animator.play(ObjectAnimator.ofFloat(mBreadCrumbGroup, "translationY", 0));

				animator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mBreadCrumbGroup.requestLayout();
						getLoaderManager().restartLoader(0, null, BookmarksFragment.this);
					}					
				});

				animator.start();

			} else {
				mBackBreadCrumb.setVisibility(View.VISIBLE);
				getLoaderManager().restartLoader(0, null, this);
			}

			if (mNavigationList.size() > 2) {
				NavigationItem previous = mNavigationList.get(mNavigationList.size() - 2);
				mFoldersBreadCrumb.setParentTitle(previous.getTitle(), null, new OnClickListener() {					
					@Override
					public void onClick(View arg0) {
						popNavigation();
					}
				});
			} else {
				mFoldersBreadCrumb.setParentTitle(getString(R.string.Bookmarks), null, new OnClickListener() {					
					@Override
					public void onClick(View arg0) {
						popNavigation();
					}
				});
			}
		}

		mFoldersBreadCrumb.setTitle(current.getTitle(), current.getTitle());
	}

	private void popNavigation() {
		mNavigationList.remove(mNavigationList.size() - 1);
		updateFolderId();
	}

	private void doDeleteFolder(long folderId) {
		mProgressDialog = ProgressDialog.show(
				getActivity(),
				getString(R.string.DeleteFolderTitle),
				getString(R.string.DeleteFolderMessage));

		new Thread(new DeleteFolderRunnable(folderId)).start();
	}

	@SuppressLint("HandlerLeak")
	private class DeleteFolderRunnable implements Runnable {

		private long mFolderId;

		public DeleteFolderRunnable(long folderId) {
			mFolderId = folderId;
		}

		@Override
		public void run() {			
			BookmarksWrapper.deleteFolder(getActivity().getContentResolver(), mFolderId);
			mHandler.sendEmptyMessage(0);
		}

		private Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				mProgressDialog.dismiss();
				getLoaderManager().restartLoader(0, null, BookmarksFragment.this);
			}
		};

	}

	private class NavigationItem {
		private long mId;
		private String mTitle;

		public NavigationItem(long id, String title) {
			mId = id;
			mTitle = title;
		}

		public NavigationItem(String builder) {
			if ((builder.startsWith("{")) &&
					(builder.endsWith("}"))) {

				try {
					builder = builder.substring(1, builder.length() - 1);
					String[] parts = builder.split(",");

					mId = Long.parseLong(parts[0]);
					if (mId == -1) {
						mTitle = null;
					} else {
						mTitle = parts[1];
					}
				} catch (Exception e) {
					mId = -1;
					mTitle = null;
				}

			} else {
				mId = -1;
				mTitle = null;
			}
		}

		public long getId() {
			return mId;
		}

		public String getTitle() {
			return mTitle;
		}

		@Override
		public String toString() {
			return String.format("{%s,%s}", mId, mTitle);
		}

	}

}
