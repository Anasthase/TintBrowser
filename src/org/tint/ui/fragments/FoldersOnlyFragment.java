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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentBreadCrumbs;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.model.BookmarkHistoryItem;
import org.tint.model.FoldersOnlyAdapter;
import org.tint.providers.BookmarksProvider;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.managers.UIFactory;
import org.tint.ui.managers.UIManager;
import org.tint.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FoldersOnlyFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String EXTRA_FOLDER_STACK = "EXTRA_FOLDER_STACK";
	public static final String EXTRA_FOLDER_BLACKLIST = "EXTRA_FOLDER_BLACKLIST";

	private static final String STACK_SEPARATOR = "//;//";

	private static final int CONTEXT_MENU_OPEN_IN_TAB = Menu.FIRST;
	private static final int CONTEXT_MENU_EDIT_BOOKMARK = Menu.FIRST + 1;
	private static final int CONTEXT_MENU_COPY_URL = Menu.FIRST + 2;
	private static final int CONTEXT_MENU_SHARE_URL = Menu.FIRST + 3;
	private static final int CONTEXT_MENU_DELETE_BOOKMARK = Menu.FIRST + 4;

	private static final int CONTEXT_MENU_DELETE_FOLDER = Menu.FIRST + 5;
	private static final int CONTEXT_MENU_RENAME_FOLDER = Menu.FIRST + 6;
	private static final int CONTEXT_MENU_MOVE_ITEM = Menu.FIRST + 7;

	private View mContainer = null;

	private UIManager mUIManager;

	private ListView mFoldersList;

	private ProgressBar mProgress;

	private ViewGroup mBreadCrumbGroup;
	private FragmentBreadCrumbs mFoldersBreadCrumb;
	private ImageView mBackBreadCrumb;
	private ImageView mAddFolderBreadCrumb;

	private FoldersOnlyAdapter mAdapter;

	private List<NavigationItem> mNavigationList;

	private boolean mIsTablet;
	private boolean mIsListShown = true;

	private ProgressDialog mProgressDialog;

	private OnSharedPreferenceChangeListener mPreferenceChangeListener;

	private long mBlacklistId = -1;


	public interface FoldersOnlyListener {
		void onSelectFolder(long folder_id, String folder_name);
	}

	public FoldersOnlyFragment() {
		mUIManager = Controller.getInstance().getUIManager();
	}

	public long getCurrentFolderId() {
		return mNavigationList.get(mNavigationList.size() - 1).getId();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (Constants.PREFERENCE_BOOKMARKS_SORT_MODE.equals(key)) {
					getLoaderManager().restartLoader(0, null, FoldersOnlyFragment.this);
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
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		View view = makeView(getActivity().getLayoutInflater(), savedInstanceState);
		final FoldersOnlyListener fol = (FoldersOnlyListener)getActivity();

		return new AlertDialog.Builder(getActivity())
				.setView(view)
				.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						NavigationItem ni = mNavigationList.get(mNavigationList.size() - 1);
						fol.onSelectFolder(ni.getId(), ni.getTitle());
					}
				})
				.setNegativeButton(R.string.Cancel, null)
				.create();
	}


	//@Override
	private View makeView(LayoutInflater inflater, Bundle savedInstanceState) {
		mIsTablet = UIFactory.isTablet(getActivity());

		if (mContainer == null) {

			mContainer = inflater.inflate(R.layout.foldersonly_fragment, null);
			
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

			mAddFolderBreadCrumb = (ImageView) mContainer.findViewById(R.id.BookmarksBreadCrumbAddFolder);
			mAddFolderBreadCrumb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					newFolder();
				}
			});

			mFoldersList = (ListView) mContainer.findViewById(R.id.BookmarksGridView);
			mProgress = (ProgressBar) mContainer.findViewById(R.id.BookmarksProgressBar);

			String[] from = new String[] { BookmarksProvider.Columns.TITLE, BookmarksProvider.Columns.URL };
			int[] to = new int[] { R.id.BookmarkRow_Title, R.id.BookmarkRow_Url };

			mAdapter = new FoldersOnlyAdapter(
					getActivity(),
					R.layout.foldersonly_row,
					null,
					from,
					to,
					0);

			mFoldersList.setAdapter(mAdapter);

			mFoldersList.setOnItemClickListener(new OnItemClickListener() {
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

			mNavigationList = new ArrayList<NavigationItem>();		

			if (getArguments() != null) {
				Bundle args = getArguments();
				if (args.containsKey(EXTRA_FOLDER_STACK)) {
					String folderStack = getArguments().getString(EXTRA_FOLDER_STACK);

					String[] stack = folderStack.split(STACK_SEPARATOR);
					for (int i = 0; i < stack.length; i++) {
						mNavigationList.add(new NavigationItem(stack[i]));
					}
				}

				if (args.containsKey(EXTRA_FOLDER_BLACKLIST)) {
					mBlacklistId = args.getLong(EXTRA_FOLDER_BLACKLIST);
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
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		setListShown(false);
		return BookmarksWrapper.getCursorLoaderForBookmarks(getActivity(),
				mNavigationList.get(mNavigationList.size() - 1).getId(),
				true, mBlacklistId);
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
			mFoldersList.setVisibility(View.VISIBLE);
		} else {
			mProgress.setVisibility(View.VISIBLE);
			mFoldersList.setVisibility(View.GONE);
		}
	}

	private void updateFolderId() {
		if (mAdapter != null) {
			mAdapter.swapCursor(null);
		}

		NavigationItem current = mNavigationList.get(mNavigationList.size() - 1);
		if (current.getId() == -1) {
			mBackBreadCrumb.setVisibility(View.GONE);
			getLoaderManager().restartLoader(0, null, this);

			mFoldersBreadCrumb.setParentTitle(getString(R.string.Bookmarks), null, new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					popNavigation();
				}
			});

		} else {
			mBackBreadCrumb.setVisibility(View.VISIBLE);
			getLoaderManager().restartLoader(0, null, this);

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

	private void newFolder() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final EditText input;

		builder.setCancelable(true);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.NewFolder);
		builder.setMessage(R.string.NewFolderHint);

		input = new EditText(getActivity());
		builder.setView(input);

		builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!TextUtils.isEmpty(input.getText().toString())) {
					new Thread(new NewFolderRunnable(getCurrentFolderId(), input.getText().toString())).start();
				}
			}
		});
		builder.setNegativeButton(R.string.Cancel, null);

		builder.create().show();
	}

	private void doRenameFolder(long id, String newName) {
		new Thread(new RenameFolderRunnable(id, newName)).start();
	}

	private void doDeleteFolder(long folderId) {
		mProgressDialog = ProgressDialog.show(
				getActivity(),
				getString(R.string.DeleteFolderTitle),
				getString(R.string.DeleteFolderMessage));

		new Thread(new DeleteFolderRunnable(folderId)).start();
	}


	@SuppressLint("HandlerLeak")
	private class NewFolderRunnable implements Runnable {
		private long mParentFolderId;
		private String mFolderName;

		public NewFolderRunnable(long id, String name) {
			mParentFolderId = id;
			mFolderName = name;
		}

		private Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				getLoaderManager().restartLoader(0, null, FoldersOnlyFragment.this);
			}
		};

		@Override
		public void run() {
			BookmarksWrapper.createFolder(getActivity().getContentResolver(), mFolderName, mParentFolderId);
			mHandler.sendEmptyMessage(0);
		}
	}

	@SuppressLint("HandlerLeak")
	private class RenameFolderRunnable implements Runnable {
		private long mFolderId;
		private String mFolderName;

		public RenameFolderRunnable(long id, String newName) {
			mFolderId = id;
			mFolderName = newName;
		}

		@Override
		public void run() {
			BookmarksWrapper.renameFolder(getActivity().getContentResolver(), mFolderId, mFolderName);
			mHandler.sendEmptyMessage(0);
		}

		private Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				getLoaderManager().restartLoader(0, null, FoldersOnlyFragment.this);
			}
		};
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
				getLoaderManager().restartLoader(0, null, FoldersOnlyFragment.this);
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
